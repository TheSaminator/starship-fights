import com.nixxcode.jvmbrotli.common.BrotliLoader
import com.nixxcode.jvmbrotli.enc.BrotliOutputStream
import com.nixxcode.jvmbrotli.enc.Encoder
import groovy.json.JsonSlurper
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.zip.GZIPOutputStream

buildscript {
	repositories {
		mavenCentral()
	}
	
	dependencies {
		classpath("com.nixxcode.jvmbrotli:jvmbrotli:0.2.0")
		
		// why does this need to be done MANUALLY?!?!
		classpath("com.nixxcode.jvmbrotli:jvmbrotli-win32-x86-amd64:0.2.0")
		classpath("com.nixxcode.jvmbrotli:jvmbrotli-darwin-x86-amd64:0.2.0")
		classpath("com.nixxcode.jvmbrotli:jvmbrotli-linux-x86-amd64:0.2.0")
	}
}

plugins {
	java
	kotlin("multiplatform") version "1.6.21"
	kotlin("plugin.serialization") version "1.6.21"
	id("com.github.johnrengelman.shadow") version "7.1.2"
	application
}

group = "net.starshipfights"

val isDevEnv: Boolean by extra {
	val configFile = file("config.json")
	if (!configFile.isFile)
		true
	else
		((JsonSlurper().parse(configFile) as Map<*, *>)["isDevEnv"] as? Boolean) ?: true
}

repositories {
	mavenCentral()
	maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
	jvm {
		compilations.all {
			kotlinOptions.jvmTarget = "1.8"
		}
		withJava()
	}
	js(IR) {
		binaries.executable()
		browser {
			commonWebpackConfig {
				if (isDevEnv) {
					mode = KotlinWebpackConfig.Mode.DEVELOPMENT
					devtool = "source-map"
				} else {
					mode = KotlinWebpackConfig.Mode.PRODUCTION
					devtool = null
				}
			}
		}
	}
	sourceSets {
		all {
			languageSettings.optIn("kotlin.RequiresOptIn")
		}
		
		val commonMain by getting {
			dependencies {
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
				implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
				implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
				
				implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.5")
			}
		}
		
		val jvmMain by getting {
			dependencies {
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.1")
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.1")
				
				implementation("io.ktor:ktor-server-netty:1.6.8")
				implementation("io.ktor:ktor-html-builder:1.6.8")
				implementation("io.ktor:ktor-auth:1.6.8")
				implementation("io.ktor:ktor-serialization:1.6.8")
				implementation("io.ktor:ktor-websockets:1.6.8")
				implementation("io.ktor:ktor-client-apache:1.6.8")
				
				implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.5")
				implementation("org.slf4j:slf4j-api:1.7.32")
				implementation("ch.qos.logback:logback-classic:1.2.10")
				
				implementation("com.aventrix.jnanoid:jnanoid:2.0.0")
				
				implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.4.0") {
					exclude("org.jetbrains.kotlin", "kotlin-reflect")
					
					exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8")
					exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-reactive")
					exclude("org.jetbrains.kotlinx", "kotlinx-serialization-core-jvm")
				}
				
				// development only
				implementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.0.0")
			}
		}
		
		val jsMain by getting {
			dependencies {
				implementation("io.ktor:ktor-client-js:1.6.8")
				implementation("io.ktor:ktor-client-websockets:1.6.8")
				
				implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.5")
				
				implementation("com.juul.indexeddb:core:0.2.3")
			}
		}
	}
}

application {
	mainClass.set("net.starshipfights.Server")
}

tasks.named<Copy>("jvmProcessResources") {
	val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
	from(jsBrowserDistribution) {
		into("/static/game")
		if (!isDevEnv)
			exclude("starship-fights.js.map")
	}
	
	doLast {
		val pool = Executors.newWorkStealingPool()
		
		val encoderParams = if (BrotliLoader.isBrotliAvailable()) Encoder.Parameters().setQuality(8) else null
		val base64 = Base64.getUrlEncoder()
		val hashDigest = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-256") }
		
		val resourceTree = fileTree(mapOf("dir" to outputs.files.asPath + "/static/", "exclude" to listOf("*.gz", "*.br", "*.sha256")))
		val countDownLatch = CountDownLatch(resourceTree.count())
		
		for (file in resourceTree) {
			pool.execute {
				val bytes = file.readBytes()
				val hashFile = File("${file.absolutePath}.sha256").bufferedWriter()
				hashFile.write(base64.encodeToString(hashDigest.get().digest(bytes)).trimEnd('='))
				hashFile.close()
				
				val result = File("${file.absolutePath}.gz").outputStream()
				val gzipStream = GZIPOutputStream(result)
				gzipStream.write(bytes)
				gzipStream.close()
				
				encoderParams?.let { encParams ->
					val brResult = File("${file.absolutePath}.br").outputStream()
					val brStream = BrotliOutputStream(brResult, encParams)
					brStream.write(bytes)
					brStream.close()
				}
				
				println("Done compressing ${file.name}")
				countDownLatch.countDown()
			}
		}
		
		countDownLatch.await()
	}
}

tasks.named<JavaExec>("run") {
	dependsOn(tasks.named<Jar>("jvmJar"))
	classpath(tasks.named<Jar>("jvmJar"))
}

tasks.create("runAiTest", JavaExec::class.java) {
	group = "test"
	classpath = sourceSets.getByName("test").runtimeClasspath
	mainClass.set("net.starshipfights.game.ai.AITesting")
}

tasks.create("runClusterGenTest", JavaExec::class.java) {
	group = "test"
	classpath = sourceSets.getByName("test").runtimeClasspath
	mainClass.set("net.starshipfights.campaign.ClusterGenTesting")
}
