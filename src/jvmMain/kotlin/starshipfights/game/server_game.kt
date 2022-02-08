package starshipfights.game

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.litote.kmongo.`in`
import org.litote.kmongo.setValue
import starshipfights.data.DocumentTable
import starshipfights.data.Id
import starshipfights.data.admiralty.BattleRecord
import starshipfights.data.admiralty.DrydockStatus
import starshipfights.data.admiralty.ShipInDrydock
import starshipfights.data.auth.User
import starshipfights.data.createToken
import java.time.Instant
import java.time.temporal.ChronoUnit

data class GameToken(val hostToken: String, val joinToken: String)

object GameManager {
	private val games = ConcurrentCurator(mutableMapOf<String, GameEntry>())
	
	suspend fun initGame(hostInfo: InGameAdmiral, guestInfo: InGameAdmiral, battleInfo: BattleInfo): GameToken {
		val gameState = GameState(
			generateGameStart(hostInfo, guestInfo, battleInfo),
			hostInfo, guestInfo, battleInfo
		)
		
		val session = GameSession(gameState)
		DocumentTable.launch {
			val end = session.gameEnd.await()
			
			val now = Instant.now()
			val destroyedShipStatus = DrydockStatus.InRepair(now.plus(12, ChronoUnit.HOURS))
			val damagedShipStatus = DrydockStatus.InRepair(now.plus(9, ChronoUnit.HOURS))
			val intactShipStatus = DrydockStatus.InRepair(now.plus(6, ChronoUnit.HOURS))
			val escapedShipStatus = DrydockStatus.InRepair(now.plus(3, ChronoUnit.HOURS))
			
			val shipWrecks = session.state.value.destroyedShips
			val destroyedShips = shipWrecks.filterValues { !it.isEscape }.keys.map { it.reinterpret<ShipInDrydock>() }.toSet()
			val escapedShips = shipWrecks.filterValues { it.isEscape }.keys.map { it.reinterpret<ShipInDrydock>() }.toSet()
			val damagedShips = session.state.value.ships.filterValues { it.hullAmount < it.ship.durability.maxHullPoints }.keys.map { it.reinterpret<ShipInDrydock>() }.toSet()
			val intactShips = session.state.value.ships.keys.map { it.reinterpret<ShipInDrydock>() }.toSet() - damagedShips
			
			launch {
				ShipInDrydock.update(ShipInDrydock::id `in` destroyedShips, setValue(ShipInDrydock::status, destroyedShipStatus))
			}
			launch {
				ShipInDrydock.update(ShipInDrydock::id `in` damagedShips, setValue(ShipInDrydock::status, damagedShipStatus))
			}
			launch {
				ShipInDrydock.update(ShipInDrydock::id `in` intactShips, setValue(ShipInDrydock::status, intactShipStatus))
			}
			launch {
				ShipInDrydock.update(ShipInDrydock::id `in` escapedShips, setValue(ShipInDrydock::status, escapedShipStatus))
			}
			
			val battleRecord = BattleRecord(
				whenEnded = now,
				
				hostUser = hostInfo.user.id.reinterpret(),
				guestUser = guestInfo.user.id.reinterpret(),
				
				hostAdmiral = hostInfo.id.reinterpret(),
				guestAdmiral = guestInfo.id.reinterpret(),
				
				winner = end.winner,
				winMessage = end.message
			)
			
			launch {
				BattleRecord.put(battleRecord)
			}
		}
		
		val hostId = createToken()
		val joinId = createToken()
		games.use {
			it[hostId] = GameEntry(hostInfo.user.id.reinterpret(), GlobalSide.HOST, session)
			it[joinId] = GameEntry(guestInfo.user.id.reinterpret(), GlobalSide.GUEST, session)
		}
		
		return GameToken(hostId, joinId)
	}
	
	suspend fun joinGame(userId: Id<User>, token: String, remove: Boolean): GameEntry? {
		return games.use { if (remove) it.remove(token) else it[token] }?.takeIf { it.userId == userId }
	}
}

class GameEntry(val userId: Id<User>, val side: GlobalSide, val session: GameSession)

class GameSession(gameState: GameState) {
	private val hostEnter = Job()
	private val guestEnter = Job()
	
	suspend fun enter(player: GlobalSide) = when (player) {
		GlobalSide.HOST -> {
			hostEnter.complete()
			withTimeoutOrNull(30_000L) {
				guestEnter.join()
				true
			} ?: false
		}
		GlobalSide.GUEST -> {
			guestEnter.complete()
			withTimeoutOrNull(30_000L) {
				hostEnter.join()
				true
			} ?: false
		}
	}.also { if (!it) onPacket(player.other, PlayerAction.TimeOut) }
	
	private val stateMutable = MutableStateFlow(gameState)
	private val stateMutex = Mutex()
	
	val state = stateMutable.asStateFlow()
	
	private val hostErrorMessages = Channel<String>()
	private val guestErrorMessages = Channel<String>()
	
	private fun errorMessageChannel(player: GlobalSide) = when (player) {
		GlobalSide.HOST -> hostErrorMessages
		GlobalSide.GUEST -> guestErrorMessages
	}
	
	fun errorMessages(player: GlobalSide): ReceiveChannel<String> = when (player) {
		GlobalSide.HOST -> hostErrorMessages
		GlobalSide.GUEST -> guestErrorMessages
	}
	
	private val _gameEnd = CompletableDeferred<GameEvent.GameEnd>()
	val gameEnd: Deferred<GameEvent.GameEnd>
		get() = _gameEnd
	
	suspend fun onPacket(player: GlobalSide, packet: PlayerAction) {
		stateMutex.withLock {
			when (val result = state.value.after(player, packet)) {
				is GameEvent.StateChange -> {
					stateMutable.value = result.newState
					result.newState.checkVictory()?.let { _gameEnd.complete(it) }
				}
				is GameEvent.InvalidAction -> {
					errorMessageChannel(player).send(result.message)
				}
				is GameEvent.GameEnd -> {
					_gameEnd.complete(result)
				}
			}
		}
	}
	
	suspend fun onClose(player: GlobalSide) {
		if (gameEnd.isCompleted) return
		
		onPacket(player, PlayerAction.Disconnect)
	}
}

suspend fun DefaultWebSocketServerSession.gameEndpoint(user: User, token: String) {
	val gameEntry = GameManager.joinGame(user.id, token, true) ?: closeAndReturn("That battle is not available") { return }
	val playerSide = gameEntry.side
	val gameSession = gameEntry.session
	
	val opponentEntered = gameSession.enter(playerSide)
	sendObject(GameBeginning.serializer(), GameBeginning(opponentEntered))
	if (!opponentEntered) return
	
	val sendEventsJob = launch {
		// Game state changes
		launch {
			gameSession.state.collect { state ->
				sendObject(GameEvent.serializer(), GameEvent.StateChange(state))
			}
		}
		
		// Invalid action messages
		launch {
			for (errorMessage in gameSession.errorMessages(playerSide)) {
				sendObject(GameEvent.serializer(), GameEvent.InvalidAction(errorMessage))
			}
		}
	}
	
	val receiveActionsJob = launch {
		while (true) {
			val packet = receiveObject(PlayerAction.serializer()) {
				closeAndReturn {
					gameSession.onClose(playerSide)
					return@launch
				}
			}
			
			if (isInternalPlayerAction(packet))
				sendObject(GameEvent.serializer(), GameEvent.InvalidAction("Invalid packet sent over wire - packet type is for internal use only"))
			else
				launch {
					gameSession.onPacket(playerSide, packet)
				}
		}
	}
	
	val gameEnd = gameSession.gameEnd.await()
	sendObject(GameEvent.serializer(), gameEnd)
	
	sendEventsJob.cancelAndJoin()
	receiveActionsJob.cancelAndJoin()
}
