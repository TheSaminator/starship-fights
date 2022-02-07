@file:JvmName("Server")

package starshipfights

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import org.slf4j.event.Level
import starshipfights.auth.AuthProvider
import starshipfights.data.ConnectionHolder
import starshipfights.data.DataRoutines
import starshipfights.game.installGame
import starshipfights.info.*
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.util.concurrent.atomic.AtomicLong

object ResourceLoader {
	fun getResource(resource: String): InputStream? = javaClass.getResourceAsStream(resource)
}

fun main() {
	System.setProperty("logback.statusListenerClass", "ch.qos.logback.core.status.NopStatusListener")
	
	System.setProperty("io.ktor.development", if (CurrentConfiguration.isDevEnv) "true" else "false")
	
	ConnectionHolder.initialize(CurrentConfiguration.dbConn, CurrentConfiguration.dbName)
	
	val dataRoutines = DataRoutines.initializeRoutines()
	
	embeddedServer(Netty, port = CurrentConfiguration.port, host = CurrentConfiguration.host) {
		install(XForwardedHeaderSupport)
		
		install(CallId) {
			val counter = AtomicLong(0)
			generate {
				"call-${counter.incrementAndGet().toULong()}-${System.currentTimeMillis()}"
			}
		}
		
		install(CallLogging) {
			level = Level.INFO
			
			callIdMdc("ktor-call-id")
			
			format { call ->
				"Call #${call.callId} Client ${call.request.origin.remoteHost} `${call.request.userAgent()}` Request ${call.request.httpMethod.value} ${call.request.uri} Response ${call.response.status()}"
			}
		}
		
		install(StatusPages) {
			status(HttpStatusCode.NotFound) {
				call.respondHtml(HttpStatusCode.NotFound, call.error404())
			}
			
			exception<HttpRedirectException> { (url, permanent) ->
				call.respondRedirect(url, permanent)
			}
			exception<MissingRequestParameterException> {
				call.respondHtml(HttpStatusCode.BadRequest, call.error400())
			}
			exception<IllegalArgumentException> {
				call.respondHtml(HttpStatusCode.Forbidden, call.error403())
			}
			exception<NullPointerException> {
				call.respondHtml(HttpStatusCode.NotFound, call.error404())
			}
			exception<Throwable> {
				call.respondHtml(HttpStatusCode.InternalServerError, call.error503())
				throw it
			}
		}
		
		install(WebSockets) {
			pingPeriodMillis = 500L
		}
		
		if (CurrentConfiguration.isDevEnv) {
			install(ShutDownUrl.ApplicationCallFeature) {
				shutDownUrl = "/dev/shutdown"
				exitCodeSupplier = { 0 }
			}
		}
		
		AuthProvider.install(this)
		
		routing {
			installPages()
			installGame()
			
			static("/static") {
				// I HAVE TO DO THIS MANUALLY
				// BECAUSE KTOR DOESN'T SUPPORT
				// PRE-COMPRESSED STATIC JAR RESOURCES
				// FOR SOME UNGODLY REASON
				get("{static-content...}") {
					val staticContentPath = call.parameters.getAll("static-content")?.joinToString("/") ?: return@get
					val contentPath = "/static/$staticContentPath"
					val gzipContentPath = "$contentPath.gz"
					
					val contentType = ContentType.fromFileExtension(contentPath.substringAfterLast('.')).firstOrNull()
					
					val acceptedEncodings = call.request.acceptEncodingItems().map { it.value }.toSet()
					if (CompressedFileType.GZIP.encoding in acceptedEncodings) {
						val gzipContent = ResourceLoader.getResource(gzipContentPath)
						if (gzipContent != null) {
							call.attributes.put(Compression.SuppressionAttribute, true)
							
							call.response.header(HttpHeaders.ContentEncoding, CompressedFileType.GZIP.encoding)
							
							call.respondBytes(gzipContent.readBytes(), contentType)
						} else
							ResourceLoader.getResource(contentPath)?.let { call.respondBytes(it.readBytes(), contentType) }
					} else
						ResourceLoader.getResource(contentPath)?.let { call.respondBytes(it.readBytes(), contentType) }
				}
			}
		}
	}.start(wait = true)
	
	dataRoutines.cancel()
}
