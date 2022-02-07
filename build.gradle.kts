import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.zip.GZIPOutputStream

plugins {
	java
	kotlin("multiplatform") version "1.6.10"
	kotlin("plugin.serialization") version "1.6.10"
	id("com.github.johnrengelman.shadow") version "7.1.2"
	application
}

group = "io.github.thesaminator"
version = "1.0-SNAPSHOT"

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
		browser()
	}
	sourceSets {
		all {
			languageSettings.optIn("kotlin.RequiresOptIn")
		}
		
		val commonMain by getting {
			dependencies {
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
				implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
				
				implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
			}
		}
		
		val jvmMain by getting {
			dependencies {
				implementation("io.ktor:ktor-server-netty:1.6.7")
				implementation("io.ktor:ktor-html-builder:1.6.7")
				implementation("io.ktor:ktor-auth:1.6.7")
				implementation("io.ktor:ktor-serialization:1.6.7")
				implementation("io.ktor:ktor-websockets:1.6.7")
				
				implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
				implementation("org.slf4j:slf4j-api:1.7.31")
				implementation("ch.qos.logback:logback-classic:1.2.5")
				
				implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.3.0") {
					exclude("org.jetbrains.kotlin", "kotlin-reflect")
				}
				
				implementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.0.0")
				
				implementation("com.aventrix.jnanoid:jnanoid:2.0.0")
			}
		}
		
		val jsMain by getting {
			dependencies {
				implementation("io.ktor:ktor-client-js:1.6.7")
				implementation("io.ktor:ktor-client-websockets:1.6.7")
				
				implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
			}
		}
	}
}

application {
	mainClass.set("starshipfights.Server")
}

tasks.named<Copy>("jvmProcessResources") {
	val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
	from(jsBrowserDistribution) {
		into("/static/game")
	}
	
	doLast {
		val pool = Executors.newWorkStealingPool()
		val resourceTree = fileTree(mapOf("dir" to outputs.files.asPath + "/static/", "exclude" to "*.gz"))
		val countDownLatch = CountDownLatch(resourceTree.count())
		
		resourceTree.forEach { file ->
			pool.execute {
				val bytes = file.readBytes()
				val result = File("${file.absolutePath}.gz").outputStream()
				val gzipStream = GZIPOutputStream(result)
				gzipStream.write(bytes)
				gzipStream.close()
				
				println("Done GZipping ${file.name}")
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
