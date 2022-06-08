package net.starshipfights

import io.ktor.util.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.starshipfights.data.ConnectionType
import java.io.File
import java.security.SecureRandom

@Serializable
data class Configuration(
	val isDevEnv: Boolean = true,
	
	val host: String = "127.0.0.1",
	val port: Int = 8080,
	
	val dbConn: ConnectionType = ConnectionType.Embedded(),
	val dbName: String = "sf",
	
	val secretHashingKey: String = hex(
		ByteArray(16).also { SecureRandom.getInstanceStrong().nextBytes(it) }
	),
	val discordClient: DiscordLogin? = null
)

@Serializable
data class DiscordLogin(
	val userAgent: String,
	
	val clientId: String,
	val clientSecret: String,
	
	val ownerId: String,
	val serverInvite: String,
)

private val DEFAULT_CONFIG = Configuration()

private var currentConfig: Configuration? = null

val CurrentConfiguration: Configuration
	get() {
		currentConfig?.let { return it }
		
		val file = File(System.getProperty("starshipfights.configpath", "./config.json"))
		if (!file.isFile) {
			if (file.exists())
				file.deleteRecursively()
			
			val json = JsonConfigCodec.encodeToString(Configuration.serializer(), DEFAULT_CONFIG)
			file.writeText(json, Charsets.UTF_8)
			return DEFAULT_CONFIG
		}
		
		val json = file.readText()
		return JsonConfigCodec.decodeFromString(Configuration.serializer(), json).also { currentConfig = it }
	}

@OptIn(ExperimentalSerializationApi::class)
val JsonConfigCodec = Json {
	prettyPrint = true
	prettyPrintIndent = "\t"
	
	useAlternativeNames = false
}
