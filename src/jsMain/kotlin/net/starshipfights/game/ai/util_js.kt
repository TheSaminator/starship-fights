package net.starshipfights.game.ai

actual fun logDebug(message: Any?) {
	console.log(message)
}

actual fun logInfo(message: Any?) {
	console.info(message)
}

actual fun logWarning(message: Any?) {
	console.warn(message)
}

actual fun logError(message: Any?) {
	console.error(message)
}
