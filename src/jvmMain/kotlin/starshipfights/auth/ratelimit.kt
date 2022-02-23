package starshipfights.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import starshipfights.rateLimit
import kotlin.math.roundToLong

class RateLimit(
	val jsonCodec: Json,
	val remainingHeader: String,
	val resetAfterHeader: String,
) {
	class Config {
		var jsonCodec: Json = Json.Default
		var remainingHeader: String = "X-RateLimit-Remaining"
		var resetAfterHeader: String = "X-RateLimit-Reset-After"
	}
	
	private var remainingRequests = -1
	private var resetAfter = 0.0
	
	companion object Feature : HttpClientFeature<Config, RateLimit> {
		override val key: AttributeKey<RateLimit> = AttributeKey("RateLimit")
		override fun prepare(block: Config.() -> Unit): RateLimit = Config().apply(block).run {
			RateLimit(jsonCodec, remainingHeader, resetAfterHeader)
		}
		
		override fun install(feature: RateLimit, scope: HttpClient) {
			scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
				feature.remainingRequests.takeIf { it >= 0 }?.let { remaining ->
					delay((feature.resetAfter * 1000 / (remaining + 1)).roundToLong())
				}
			}
			
			scope.responsePipeline.intercept(HttpResponsePipeline.Receive) {
				if (context.response.status == HttpStatusCode.TooManyRequests) {
					feature.remainingRequests = 0
					val jsonBody = context.response.receive<String>()
					val rateLimitedResponse = feature.jsonCodec.decodeFromString(RateLimitedResponse.serializer(), jsonBody)
					feature.resetAfter = rateLimitedResponse.retryAfter
					
					rateLimit()
				} else {
					context.response.headers[feature.remainingHeader]?.toIntOrNull()?.let {
						feature.remainingRequests = it
					}
					
					context.response.headers[feature.resetAfterHeader]?.toDoubleOrNull()?.let {
						feature.resetAfter = it
					}
				}
			}
		}
	}
}

@Serializable
data class RateLimitedResponse(
	val message: String,
	@SerialName("retry_after")
	val retryAfter: Double,
	val global: Boolean
)
