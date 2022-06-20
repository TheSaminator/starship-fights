package net.starshipfights.game

import externals.threejs.PerspectiveCamera
import externals.threejs.Scene
import externals.threejs.WebGLRenderer
import io.ktor.client.features.websocket.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.html.FormEncType
import kotlinx.html.FormMethod
import kotlinx.html.dom.append
import kotlinx.html.hiddenInput
import kotlinx.html.js.form
import kotlinx.html.style
import net.starshipfights.data.Id

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

private suspend fun enterTraining(admiral: Id<InGameAdmiral>, battleInfo: BattleInfo, opponent: TrainingOpponent): Nothing {
	interruptExit = false
	
	document.body!!.append.form(action = "/train", method = FormMethod.post, encType = FormEncType.applicationXWwwFormUrlEncoded) {
		style = "display:none"
		hiddenInput {
			name = "admiral"
			value = admiral.toString()
		}
		hiddenInput {
			name = "battle-size"
			value = battleInfo.size.toUrlSlug()
		}
		hiddenInput {
			name = "battle-bg"
			value = battleInfo.bg.toUrlSlug()
		}
		hiddenInput {
			name = "enemy-faction"
			value = opponent.faction?.toUrlSlug() ?: "-random"
		}
		hiddenInput {
			name = "enemy-flavor"
			value = opponent.flavor?.toUrlSlug() ?: "-random"
		}
	}.submit()
	awaitCancellation()
}

private suspend fun enterGame(connectToken: String): Nothing {
	interruptExit = false
	
	document.body!!.append.form(action = "/play", method = FormMethod.post, encType = FormEncType.applicationXWwwFormUrlEncoded) {
		style = "display:none"
		hiddenInput {
			name = "token"
			value = connectToken
		}
	}.submit()
	awaitCancellation()
}

private suspend fun usePlayerLogin(admirals: List<InGameAdmiral>) {
	val playerLogin = Popup.getPlayerLogin(admirals)
	val playerLoginSide = playerLogin.login
	
	if (playerLoginSide is LoginMode.Train) {
		val (battleInfo, enemyFaction) = playerLoginSide
		enterTraining(playerLogin.admiral, battleInfo, enemyFaction)
	}
	
	val admiral = admirals.single { it.id == playerLogin.admiral }
	
	try {
		httpClient.webSocket("$rootPathWs/matchmaking") {
			sendObject(PlayerLogin.serializer(), playerLogin)
			
			when (playerLoginSide) {
				is LoginMode.Host1v1 -> {
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
					enterGame(connectToken)
				}
				is LoginMode.Join1v1 -> {
					val listOfHosts = receiveObject(JoinListing.serializer()) { closeAndReturn { return@webSocket } }.openGames
					
					do {
						val selectedHost = Popup.Host1v1SelectScreen(listOfHosts).display() ?: closeAndReturn("Battle joining cancelled") { return@webSocket }
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
					enterGame(connectToken)
				}
				is LoginMode.Host2v1 -> {
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
					enterGame(connectToken)
				}
				is LoginMode.Join2v1 -> {
					val listOfHosts = receiveObject(JoinListing.serializer()) { closeAndReturn { return@webSocket } }.openGames
					
					do {
						val selectedHost = Popup.Host2v1SelectScreen(listOfHosts).display() ?: closeAndReturn("Battle joining cancelled") { return@webSocket }
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
					enterGame(connectToken)
				}
				else -> {
					closeAndReturn { return@webSocket }
				}
			}
		}
	} catch (ex: WebSocketException) {
		Popup.Error("Server abruptly closed connection").display()
	}
	
	usePlayerLogin(admirals)
}

suspend fun matchmakingMain(admirals: List<InGameAdmiral>) {
	interruptExit = true
	
	coroutineScope {
		launch { setupBackground() }
		launch { usePlayerLogin(admirals) }
	}
}
