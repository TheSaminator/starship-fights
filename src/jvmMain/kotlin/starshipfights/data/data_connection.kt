package starshipfights.data

import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongoCmdOptions
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.config.Storage
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.serialization.changeIdController
import org.litote.kmongo.serialization.registerSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.ServerSocket
import kotlin.system.exitProcess

@Serializable
sealed class ConnectionType {
	abstract fun createUrl(): String
	
	@Serializable
	@SerialName("embedded")
	data class Embedded(val dataDir: String = "mongodb") : ConnectionType() {
		private fun getFreePort() = ServerSocket(0).use { it.localPort }
		
		@Transient
		val log: Logger = LoggerFactory.getLogger(javaClass)
		
		override fun createUrl(): String {
			val dataDirPath = File(dataDir).apply { mkdirs() }.absolutePath
			
			val starter = MongodStarter.getDefaultInstance()
			
			val port = getFreePort()
			log.info("Running embedded MongoDB on port $port")
			
			val config = MongodConfig.builder()
				.version(Version.Main.PRODUCTION)
				.net(Net(port, Network.localhostIsIPv6()))
				.replication(Storage(dataDirPath, null, 1024))
				.cmdOptions(MongoCmdOptions.builder().useNoJournal(false).build())
				.build()
			
			var executable: MongodExecutable? = null
			Runtime.getRuntime().addShutdownHook(
				Thread(
					{ executable?.stop() },
					"Shutdown Thread"
				)
			)
			
			try {
				executable = starter.prepare(config).apply { start() }
			} catch (ex: Exception) {
				log.error("Exception from starting embedded MongoDB!", ex)
				log.error("Shutting down")
				exitProcess(-1)
			}
			
			return "mongodb://localhost:$port"
		}
	}
	
	@Serializable
	@SerialName("external")
	data class External(val url: String) : ConnectionType() {
		override fun createUrl() = url
	}
}

object ConnectionHolder {
	private lateinit var databaseName: String
	
	private val clientDeferred = CompletableDeferred<CoroutineClient>()
	
	suspend fun getDatabase() = clientDeferred.await().getDatabase(databaseName)
	
	fun initialize(conn: ConnectionType, db: String) {
		if (clientDeferred.isCompleted)
			error("Cannot initialize database twice!")
		
		changeIdController(DocumentIdController)
		registerSerializer(IdSerializer)
		
		databaseName = db
		clientDeferred.complete(KMongo.createClient(conn.createUrl()).coroutine)
	}
}
