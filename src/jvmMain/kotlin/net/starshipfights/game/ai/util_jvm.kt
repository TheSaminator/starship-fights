package net.starshipfights.game.ai

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val aiLogger: Logger = LoggerFactory.getLogger("SF_AI")

actual fun logDebug(message: Any?) {
	aiLogger.debug(message.toString())
}

actual fun logInfo(message: Any?) {
	aiLogger.info(message.toString())
}

actual fun logWarning(message: Any?) {
	aiLogger.warn(message.toString())
}

actual fun logError(message: Any?) {
	aiLogger.error(message.toString())
}
