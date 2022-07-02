@file:JvmName("Server")

package net.starshipfights

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
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import net.starshipfights.admin.awaitShutDown
import net.starshipfights.admin.installAdmin
import net.starshipfights.auth.AuthProvider
import net.starshipfights.campaign.installCampaign
import net.starshipfights.data.ConnectionHolder
import net.starshipfights.data.DataRoutines
import net.starshipfights.game.installGame
import net.starshipfights.info.*
import net.starshipfights.labs.installLabs
import org.slf4j.event.Level
import java.io.InputStream
import java.util.concurrent.atomic.AtomicLong

object ResourceLoader {
	fun getResource(resource: String): InputStream? = javaClass.getResourceAsStream(resource)
	
	val SHA256AttributeKey = AttributeKey<String>("SHA256Hash")
}

fun main() {
	System.setProperty("logback.statusListenerClass", "ch.qos.logback.core.status.NopStatusListener")
	
	System.setProperty("io.ktor.development", if (CurrentConfiguration.isDevEnv) "true" else "false")
	
	ConnectionHolder.initialize(CurrentConfiguration.dbConn, CurrentConfiguration.dbName)
	
	val dataRoutines = DataRoutines.initializeRoutines()
	
	val server = embeddedServer(Netty, port = CurrentConfiguration.port, host = CurrentConfiguration.host) {
		install(IgnoreTrailingSlash)
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
		
		install(ConditionalHeaders) {
			version { outgoingContent ->
				outgoingContent.getProperty(ResourceLoader.SHA256AttributeKey)?.let { hash ->
					listOf(EntityTagVersion(hash))
				}.orEmpty()
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
			exception<InvalidCsrfTokenException> {
				call.respondHtml(HttpStatusCode.Forbidden, call.error403InvalidCsrf())
			}
			exception<NullPointerException> {
				call.respondHtml(HttpStatusCode.NotFound, call.error404())
			}
			exception<RateLimitException> {
				call.respondHtml(HttpStatusCode.TooManyRequests, call.error429())
			}
			
			exception<Throwable> {
				call.respondHtml(HttpStatusCode.InternalServerError, call.error503())
				throw it
			}
		}
		
		install(WebSockets) {
			pingPeriodMillis = 500L
		}
		
		AuthProvider.install(this)
		
		routing {
			installPages()
			installGame()
			installCampaign()
			installAdmin()
			installLabs()
			
			static("/static") {
				// I HAVE TO DO THIS MANUALLY
				// BECAUSE KTOR DOESN'T SUPPORT
				// PRE-COMPRESSED STATIC JAR RESOURCES
				// FOR SOME UNGODLY REASON
				get("{static-content...}") {
					val staticContentPath = call.parameters.getAll("static-content")?.joinToString("/") ?: return@get
					val contentPath = "/static/$staticContentPath"
					
					val hashContentPath = "$contentPath.sha256"
					val sha256Hash = ResourceLoader.getResource(hashContentPath)?.reader()?.readText()
					val configureContent: OutgoingContent.() -> Unit = { setProperty(ResourceLoader.SHA256AttributeKey, sha256Hash) }
					
					val brContentPath = "$contentPath.br"
					val gzContentPath = "$contentPath.gz"
					
					val contentType = ContentType.fromFileExtension(contentPath.substringAfterLast('.')).firstOrNull()
					
					val acceptedEncodings = call.request.acceptEncodingItems().map { it.value }.toSet()
					
					if (CompressedFileType.BROTLI.encoding in acceptedEncodings) {
						val brContent = ResourceLoader.getResource(brContentPath)
						if (brContent != null) {
							call.attributes.put(Compression.SuppressionAttribute, true)
							
							call.response.header(HttpHeaders.ContentEncoding, CompressedFileType.BROTLI.encoding)
							
							call.respondBytes(brContent.readBytes(), contentType, configure = configureContent)
							
							return@get
						}
					}
					
					if (CompressedFileType.GZIP.encoding in acceptedEncodings) {
						val gzContent = ResourceLoader.getResource(gzContentPath)
						if (gzContent != null) {
							call.attributes.put(Compression.SuppressionAttribute, true)
							
							call.response.header(HttpHeaders.ContentEncoding, CompressedFileType.GZIP.encoding)
							
							call.respondBytes(gzContent.readBytes(), contentType, configure = configureContent)
							
							return@get
						}
					}
					
					ResourceLoader.getResource(contentPath)?.let { call.respondBytes(it.readBytes(), contentType, configure = configureContent) }
				}
			}
		}
	}.start(wait = false)
	
	runBlocking { awaitShutDown() }
	
	server.stop(1_000L, 1_000L)
	
	dataRoutines.cancel()
	
	CurrentConfiguration.dbConn.shutdown()
}
