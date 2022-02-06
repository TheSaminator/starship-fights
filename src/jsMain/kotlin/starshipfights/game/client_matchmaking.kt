package starshipfights.game

import externals.threejs.PerspectiveCamera
import externals.threejs.Scene
import externals.threejs.WebGLRenderer
import io.ktor.client.features.websocket.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun setupBackground() {
	val camera = PerspectiveCamera(69, window.aspectRatio, 0.01, 1_000)
	
	val renderer = WebGLRenderer(configure {
		canvas = document.getElementById("three-canvas")
		antialias = true
	})
	
	renderer.setPixelRatio(window.devicePixelRatio)
	renderer.setSize(window.innerWidth, window.innerHeight)
	
	val scene = Scene()
	scene.background = RenderResources.spaceboxes.values.random()
	scene.add(camera)
	
	window.addEventListener("resize", {
		camera.aspect = window.aspectRatio
		camera.updateProjectionMatrix()
		
		renderer.setSize(window.innerWidth, window.innerHeight)
	})
	
	deltaTimeFlow.collect { dt ->
		renderer.render(scene, camera)
		camera.rotateY(dt * 0.25)
	}
}

private suspend fun usePlayerLogin(admirals: List<InGameAdmiral>) {
	val playerLogin = Popup.getPlayerLogin(admirals)
	val admiral = admirals.single { it.id == playerLogin.admiral }
	
	try {
		httpClient.webSocket("$rootPathWs/matchmaking") {
			sendObject(PlayerLogin.serializer(), playerLogin)
			
			when (playerLogin.login.globalSide) {
				GlobalSide.HOST -> {
					var loadingText = "Awaiting join request..."
					
					do {
						val joinRequest = Popup.CancellableLoadingScreen(loadingText) {
							receiveObject(JoinRequest.serializer()) { closeAndReturn { return@CancellableLoadingScreen null } }
						}.display() ?: closeAndReturn("Battle hosting cancelled") { return@webSocket }
						
						val joinAcceptance = Popup.GuestRequestScreen(admiral, joinRequest.joiner).display() ?: closeAndReturn("Battle hosting cancelled") { return@webSocket }
						sendObject(JoinResponse.serializer(), JoinResponse(joinAcceptance))
						
						val joinConnected = joinAcceptance && receiveObject(JoinResponseResponse.serializer()) { closeAndReturn { return@webSocket } }.connected
						
						loadingText = if (joinAcceptance)
							"${joinRequest.joiner.name} cancelled join. Awaiting join request..."
						else
							"Awaiting join request..."
					} while (!joinConnected)
					
					val connectToken = receiveObject(GameReady.serializer()) { closeAndReturn { return@webSocket } }.connectToken
					Popup.GameReadyScreen(connectToken).display()
				}
				GlobalSide.GUEST -> {
					val listOfHosts = receiveObject(JoinListing.serializer()) { closeAndReturn { return@webSocket } }.openGames
					
					do {
						val selectedHost = Popup.HostSelectScreen(listOfHosts).display() ?: closeAndReturn("Battle joining cancelled") { return@webSocket }
						sendObject(JoinSelection.serializer(), JoinSelection(selectedHost))
						
						val joinAcceptance = Popup.CancellableLoadingScreen("Awaiting join response...") {
							receiveObject(JoinResponse.serializer()) { closeAndReturn { return@CancellableLoadingScreen null } }.accepted
						}.display() ?: closeAndReturn("Battle joining cancelled") { return@webSocket }
						
						if (!joinAcceptance) {
							val hostInfo = listOfHosts.getValue(selectedHost).admiral
							Popup.JoinRejectedScreen(hostInfo).display()
						}
					} while (!joinAcceptance)
					
					val connectToken = receiveObject(GameReady.serializer()) { closeAndReturn { return@webSocket } }.connectToken
					Popup.GameReadyScreen(connectToken).display()
				}
			}
		}
	} catch (ex: WebSocketException) {
		Popup.Error("Server abruptly closed connection").display()
	}
	
	usePlayerLogin(admirals)
}

suspend fun matchmakingMain(admirals: List<InGameAdmiral>) {
	coroutineScope {
		launch { setupBackground() }
		launch { usePlayerLogin(admirals) }
	}
}
