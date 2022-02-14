package starshipfights.game

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.features.websocket.*
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromDynamic

val rootPath = window.location.origin
val rootPathWs = "ws" + rootPath.removePrefix("http")

@OptIn(ExperimentalSerializationApi::class)
val clientMode: ClientMode = try {
	jsonSerializer.decodeFromDynamic(ClientMode.serializer(), window.asDynamic().sfClientMode)
} catch (_: Exception) {
	ClientMode.Error("Invalid client mode sent from server")
}

val AppScope = MainScope() + CoroutineExceptionHandler { _, error ->
	console.error("Unhandled coroutine exception", error.stackTraceToString())
}

val httpClient = HttpClient(Js) {
	install(WebSockets) {
		pingInterval = 500L
	}
}

fun main() {
	AppScope.launch {
		Popup.LoadingScreen("Loading resources...") {
			RenderResources.load(clientMode !is ClientMode.InGame)
		}.display()
		
		window.addEventListener("beforeunload", { e ->
			e.preventDefault()
			e.asDynamic().returnValue = ""
		})
		
		when (clientMode) {
			is ClientMode.MatchmakingMenu -> matchmakingMain(clientMode.admirals)
			is ClientMode.InGame -> gameMain(clientMode.playerSide, clientMode.connectToken, clientMode.initialState)
			is ClientMode.Error -> errorMain(clientMode.message)
		}
	}
}
