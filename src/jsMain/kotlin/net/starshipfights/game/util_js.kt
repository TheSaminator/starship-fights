package net.starshipfights.game

import io.ktor.http.cio.websocket.*
import kotlinx.browser.window
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.EventTarget
import kotlin.coroutines.resume

inline fun <T> configure(builder: T.() -> Unit) = js("{}").unsafeCast<T>().apply(builder)

val Window.aspectRatio: Double
	get() = innerWidth.toDouble() / innerHeight

suspend fun EventTarget.awaitEvent(eventName: String, shouldPreventDefault: Boolean = false): Event = suspendCancellableCoroutine { continuation ->
	val listener = object : EventListener {
		override fun handleEvent(event: Event) {
			if (shouldPreventDefault)
				event.preventDefault()
			
			removeEventListener(eventName, this)
			continuation.resume(event)
		}
	}
	
	continuation.invokeOnCancellation {
		removeEventListener(eventName, listener)
	}
	
	addEventListener(eventName, listener)
}

suspend fun awaitAnimationFrame(): Double = suspendCancellableCoroutine { continuation ->
	val handle = window.requestAnimationFrame { t ->
		continuation.resume(t)
	}
	
	continuation.invokeOnCancellation {
		window.cancelAnimationFrame(handle)
	}
}

val deltaTimeFlow: Flow<Double>
	get() = flow {
		var prevTime = awaitAnimationFrame()
		while (currentCoroutineContext().isActive) {
			val currTime = awaitAnimationFrame()
			emit((currTime - prevTime) / 1000.0)
			prevTime = currTime
		}
	}

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
