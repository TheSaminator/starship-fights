package starshipfights

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import starshipfights.data.ConnectionType
import java.io.File

@Serializable
data class Configuration(
	val isDevEnv: Boolean,
	
	val host: String,
	val port: Int,
	
	val dbConn: ConnectionType,
	val dbName: String,
	
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

private val DEFAULT_CONFIG = Configuration(
	isDevEnv = true,
	host = "127.0.0.1",
	port = 8080,
	dbConn = ConnectionType.Embedded(),
	dbName = "sf",
	discordClient = null
)

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
