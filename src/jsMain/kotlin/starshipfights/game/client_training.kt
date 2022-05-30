package starshipfights.game

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import starshipfights.game.ai.AISession
import starshipfights.game.ai.aiPlayer

class GameSession(gameState: GameState) {
	private val stateMutable = MutableStateFlow(gameState)
	private val stateMutex = Mutex()
	
	val state = stateMutable.asStateFlow()
	
	private val hostErrorMessages = Channel<String>(Channel.UNLIMITED)
	private val guestErrorMessages = Channel<String>(Channel.UNLIMITED)
	
	private fun errorMessageChannel(player: GlobalSide) = when (player) {
		GlobalSide.HOST -> hostErrorMessages
		GlobalSide.GUEST -> guestErrorMessages
	}
	
	fun errorMessages(player: GlobalSide): ReceiveChannel<String> = when (player) {
		GlobalSide.HOST -> hostErrorMessages
		GlobalSide.GUEST -> guestErrorMessages
	}
	
	private val gameEndMutable = CompletableDeferred<GameEvent.GameEnd>()
	val gameEnd: Deferred<GameEvent.GameEnd>
		get() = gameEndMutable
	
	suspend fun onPacket(player: GlobalSide, packet: PlayerAction) {
		stateMutex.withLock {
			when (val result = state.value.after(player, packet)) {
				is GameEvent.StateChange -> {
					stateMutable.value = result.newState
					result.newState.checkVictory()?.let { gameEndMutable.complete(it) }
				}
				is GameEvent.InvalidAction -> {
					errorMessageChannel(player).send(result.message)
				}
				is GameEvent.GameEnd -> {
					gameEndMutable.complete(result)
				}
			}
		}
	}
}

private suspend fun GameNetworkInteraction.execute(): Pair<LocalSide?, String> {
	val gameSession = GameSession(gameState.value)
	
	val aiSide = mySide.other
	val aiActions = Channel<PlayerAction>()
	val aiEvents = Channel<GameEvent>()
	val aiSession = AISession(aiSide, aiActions, aiEvents)
	
	return coroutineScope {
		val aiHandlingJob = launch {
			launch {
				listOf(
					// Game state changes
					launch {
						gameSession.state.collect { state ->
							aiEvents.send(GameEvent.StateChange(state))
						}
					},
					// Invalid action messages
					launch {
						for (errorMessage in gameSession.errorMessages(aiSide)) {
							aiEvents.send(GameEvent.InvalidAction(errorMessage))
						}
					}
				).joinAll()
			}
			
			launch {
				for (action in aiActions)
					gameSession.onPacket(aiSide, action)
			}
			
			aiPlayer(aiSession, gameState.value)
		}
		
		val playerHandlingJob = launch {
			launch {
				listOf(
					// Game state changes
					launch {
						gameSession.state.collect { state ->
							gameState.value = state
						}
					},
					// Invalid action messages
					launch {
						for (errorMessage in gameSession.errorMessages(mySide)) {
							errorMessages.send(errorMessage)
						}
					}
				).joinAll()
			}
			
			for (action in playerActions)
				gameSession.onPacket(mySide, action)
		}
		
		val gameEnd = gameSession.gameEnd.await()
		
		aiHandlingJob.cancel()
		playerHandlingJob.cancel()
		
		gameEnd.winner?.relativeTo(mySide) to gameEnd.message
	}
}

suspend fun trainingMain(state: GameState) {
	interruptExit = true
	
	initializePicking()
	
	mySide = GlobalSide.HOST
	
	val gameState = MutableStateFlow(state)
	val playerActions = Channel<PlayerAction>(Channel.UNLIMITED)
	val errorMessages = Channel<String>(Channel.UNLIMITED)
	
	val gameConnection = GameNetworkInteraction(gameState, playerActions, errorMessages)
	val gameRendering = GameRenderInteraction(gameState, playerActions, errorMessages)
	
	coroutineScope {
		val connectionJob = async { gameConnection.execute() }
		val renderingJob = launch { gameRendering.execute(this@coroutineScope) }
		
		val (finalWinner, finalMessage) = connectionJob.await()
		renderingJob.cancel()
		
		interruptExit = false
		Popup.GameOver(finalWinner, finalMessage, gameState.value).display()
	}
}
