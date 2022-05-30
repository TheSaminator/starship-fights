package starshipfights.game.ai

actual fun logInfo(message: Any?) {
	console.log(message)
}

actual fun logWarning(message: Any?) {
	console.warn(message)
}

actual fun logError(message: Any?) {
	console.error(message)
}
