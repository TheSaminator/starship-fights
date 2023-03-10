package net.starshipfights.game

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.features.websocket.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.starshipfights.campaign.campaignMain
import org.w3c.dom.HTMLScriptElement

val rootPathWs = "ws" + window.location.origin.removePrefix("http")

val clientMode: ClientMode = try {
	jsonSerializer.decodeFromString(
		ClientMode.serializer(),
		document.getElementById("sf-client-mode").unsafeCast<HTMLScriptElement>().text
	)
} catch (ex: Exception) {
	ex.printStackTrace()
	ClientMode.Error("Invalid client mode sent from server")
} catch (dyn: dynamic) {
	console.error(dyn)
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
	window.addEventListener("beforeunload", { e ->
		if (interruptExit) {
			e.preventDefault()
			e.asDynamic().returnValue = ""
		}
	})
	
	AppScope.launch {
		if (!clientMode.usesOwnLoad)
			Popup.LoadingScreen("Loading resources...") {
				RenderResources.load(clientMode.isSmallLoad)
			}.display()
		
		when (clientMode) {
			is ClientMode.CampaignMap -> campaignMain(clientMode.playingAs, clientMode.admirals, clientMode.clusterToken, clientMode.clusterView)
			is ClientMode.MatchmakingMenu -> matchmakingMain(clientMode.admirals)
			is ClientMode.InTrainingGame -> trainingMain(clientMode.initialState)
			is ClientMode.InGame -> gameMain(clientMode.playerSide, clientMode.connectToken, clientMode.initialState)
			is ClientMode.Error -> errorMain(clientMode.message)
		}
	}
}

var interruptExit: Boolean = false
