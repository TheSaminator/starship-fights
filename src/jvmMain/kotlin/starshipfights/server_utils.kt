package starshipfights

import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class HttpRedirectException(val url: String, val permanent: Boolean) : RuntimeException()
fun redirect(url: String, permanent: Boolean = false): Nothing = throw HttpRedirectException(url, permanent)

val sfLogger: Logger = LoggerFactory.getLogger("StarshipFights")
