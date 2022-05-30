package starshipfights.game.ai

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import starshipfights.game.GameEvent
import starshipfights.game.GameState
import starshipfights.game.GlobalSide
import starshipfights.game.PlayerAction

data class AISession(
	val mySide: GlobalSide,
	val actions: SendChannel<PlayerAction>,
	val events: ReceiveChannel<GameEvent>,
	val instincts: Instincts = Instincts(),
)

suspend fun aiPlayer(session: AISession, initialState: GameState) = coroutineScope {
	val gameDone = Job()
	
	val errors = Channel<String>()
	val gameStateFlow = MutableStateFlow(initialState)
	val aiPlayer = AIPlayer(
		gameStateFlow,
		session.actions,
		errors
	)
	
	val behavingJob = launch {
		aiPlayer.behave(session.instincts, session.mySide)
	}
	
	val handlingJob = launch {
		for (event in session.events) {
			when (event) {
				is GameEvent.GameEnd -> gameDone.complete()
				is GameEvent.InvalidAction -> launch { errors.send(event.message) }
				is GameEvent.StateChange -> gameStateFlow.value = event.newState
			}
		}
	}
	
	gameDone.join()
	
	behavingJob.cancelAndJoin()
	handlingJob.cancelAndJoin()
	
	session.actions.close()
}
