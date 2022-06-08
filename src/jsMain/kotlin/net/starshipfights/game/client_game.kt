package net.starshipfights.game

import externals.threejs.*
import io.ktor.client.features.websocket.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.PI

class GameNetworkInteraction(
	val gameState: MutableStateFlow<GameState>,
	val playerActions: ReceiveChannel<PlayerAction>,
	val errorMessages: SendChannel<String>
)

class GameRenderInteraction(
	val gameState: StateFlow<GameState>,
	val playerActions: SendChannel<PlayerAction>,
	val errorMessages: ReceiveChannel<String>
)

lateinit var mySide: GlobalSide

private val pickContextDeferred = CompletableDeferred<PickContext>()

suspend fun GameRenderInteraction.execute(scope: CoroutineScope) {
	GameUI.initGameUI(scope.uiResponder(playerActions))
	
	GameUI.drawGameUI(gameState.value)
	
	val gameStart = gameState.value.start
	val playerStart = gameStart.playerStart(mySide)
	
	val camera = PerspectiveCamera(69, window.aspectRatio, 0.01, 1_000)
	
	camera.rotateX(PI / 4)
	RenderScaling.toWorldRotation(playerStart.cameraFacing, camera)
	
	val renderer = WebGLRenderer(configure {
		canvas = document.getElementById("three-canvas")
		antialias = true
	})
	
	renderer.setPixelRatio(window.devicePixelRatio)
	renderer.setSize(window.innerWidth, window.innerHeight)
	
	renderer.shadowMap.enabled = true
	renderer.shadowMap.type = VSMShadowMap
	
	val scene = Scene()
	val battleGrid = RenderResources.battleGrid.generate(gameStart.battlefieldWidth to gameStart.battlefieldLength)
	scene.add(battleGrid)
	scene.add(camera)
	
	val cameraControls = CameraControls(camera, configure {
		domElement = renderer.domElement
		keyDomElement = window
		
		cameraXBound = RenderScaling.toWorldLength(gameStart.battlefieldLength / 2)
		cameraZBound = RenderScaling.toWorldLength(gameStart.battlefieldWidth / 2)
	})
	
	cameraControls.cameraParent.position.copy(RenderScaling.toWorldPosition(playerStart.cameraPosition))
	
	cameraControls.camera.add(PointLight("#ffffff", 0.3, 60, 1.5))
	cameraControls.camera.add(DirectionalLight("#ffffff", 0.45).apply {
		position.setScalar(0)
		target = cameraControls.cameraParent
	})
	
	GameRender.renderGameState(scene, gameState.value)
	
	window.addEventListener("resize", {
		camera.aspect = window.aspectRatio
		camera.updateProjectionMatrix()
		
		renderer.setSize(window.innerWidth, window.innerHeight)
	})
	
	pickContextDeferred.complete(PickContext(scene, camera) { gameState.value })
	
	coroutineScope {
		launch {
			deltaTimeFlow.collect { dt ->
				cameraControls.update(dt)
				renderer.render(scene, camera)
				GameUI.updateGameUI(cameraControls)
			}
		}
		
		launch {
			for (errorMessage in errorMessages)
				GameUI.displayErrorMessage(errorMessage)
		}
		
		launch {
			gameState.collect { state ->
				GameRender.renderGameState(scene, state)
				GameUI.drawGameUI(state)
				
				if (state.phase != GamePhase.Deploy)
					launch {
						val pickContext = pickContextDeferred.await()
						beginSelecting(pickContext)
						handleSelections(pickContext)
					}
			}
		}
	}
}

private suspend fun GameNetworkInteraction.execute(token: String): GameEvent.GameEnd {
	val gameEnd = CompletableDeferred<GameEvent.GameEnd>()
	
	try {
		httpClient.webSocket("$rootPathWs/game/$token") {
			val opponentJoined = Popup.LoadingScreen("Waiting for opponent to enter...") {
				receiveObject(GameBeginning.serializer()) { closeAndReturn { return@LoadingScreen false } }.opponentJoined
			}.display()
			
			if (!opponentJoined)
				Popup.GameOver(mySide, "Unfortunately, your opponent never entered the battle.", emptyMap(), gameState.value).display()
			
			val sendActionsJob = launch {
				for (action in playerActions)
					launch {
						sendObject(PlayerAction.serializer(), action)
					}
			}
			
			while (true) {
				when (val event = receiveObject(GameEvent.serializer()) { closeAndReturn { return@webSocket sendActionsJob.cancel() } }) {
					is GameEvent.StateChange -> {
						gameState.value = event.newState
					}
					is GameEvent.InvalidAction -> {
						errorMessages.send(event.message)
					}
					is GameEvent.GameEnd -> {
						gameEnd.complete(event)
						closeAndReturn { return@webSocket sendActionsJob.cancel() }
					}
				}
			}
		}
	} catch (ex: WebSocketException) {
		gameEnd.complete(GameEvent.GameEnd(null, "Server closed connection abruptly", emptyMap()))
	}
	
	if (gameEnd.isActive)
		gameEnd.complete(GameEvent.GameEnd(null, "Connection closed", emptyMap()))
	
	return gameEnd.await()
}

private class GameUIResponderImpl(scope: CoroutineScope, private val actions: SendChannel<PlayerAction>) : GameUIResponder, CoroutineScope by scope {
	override fun doAction(action: PlayerAction) {
		launch {
			actions.send(action)
		}
	}
	
	override fun useAbility(ability: PlayerAbilityType) {
		launch {
			val ctx = pickContextDeferred.await()
			val abilityData = ability.beginOnClient(ctx.getGameState(), mySide) { it.pick(ctx) } ?: return@launch
			val action = PlayerAction.UseAbility(ability, abilityData)
			actions.send(action)
		}
	}
}

private fun CoroutineScope.uiResponder(actions: SendChannel<PlayerAction>) = GameUIResponderImpl(this, actions)

suspend fun gameMain(side: GlobalSide, token: String, state: GameState) {
	interruptExit = true
	
	initializePicking()
	
	mySide = side
	
	val gameState = MutableStateFlow(state)
	val playerActions = Channel<PlayerAction>(Channel.UNLIMITED)
	val errorMessages = Channel<String>(Channel.UNLIMITED)
	
	val gameConnection = GameNetworkInteraction(gameState, playerActions, errorMessages)
	val gameRendering = GameRenderInteraction(gameState, playerActions, errorMessages)
	
	coroutineScope {
		val connectionJob = async { gameConnection.execute(token) }
		val renderingJob = launch { gameRendering.execute(this@coroutineScope) }
		
		val (finalWinner, finalMessage, finalSubplots) = connectionJob.await()
		renderingJob.cancel()
		
		interruptExit = false
		Popup.GameOver(finalWinner, finalMessage, finalSubplots, gameState.value).display()
	}
}
