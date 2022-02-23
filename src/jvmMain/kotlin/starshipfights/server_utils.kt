package starshipfights

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ForbiddenException : IllegalArgumentException()
fun forbid(): Nothing = throw ForbiddenException()

data class HttpRedirectException(val url: String, val permanent: Boolean) : RuntimeException()
fun redirect(url: String, permanent: Boolean = false): Nothing = throw HttpRedirectException(url, permanent)

class RateLimitException : RuntimeException()
fun rateLimit(): Nothing = throw RateLimitException()

val sfLogger: Logger = LoggerFactory.getLogger("StarshipFights")
