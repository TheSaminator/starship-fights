package net.starshipfights.game

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

suspend inline fun <T> DefaultWebSocketSession.receiveObject(serializer: DeserializationStrategy<T>, exitOnError: () -> Nothing): T {
	val text = incoming.receiveAsFlow().filterIsInstance<Frame.Text>().firstOrNull()?.readText() ?: exitOnError()
	return jsonSerializer.decodeFromString(serializer, text)
}

suspend inline fun <T> DefaultWebSocketSession.sendObject(serializer: SerializationStrategy<T>, value: T) {
	outgoing.send(Frame.Text(jsonSerializer.encodeToString(serializer, value)))
	flush()
}

suspend inline fun DefaultWebSocketSession.closeAndReturn(closeMessage: String = "", exitFunction: () -> Nothing): Nothing {
	close(CloseReason(CloseReason.Codes.NORMAL, closeMessage))
	exitFunction()
}
