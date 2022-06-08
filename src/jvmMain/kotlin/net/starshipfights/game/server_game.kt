package net.starshipfights.game

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.starshipfights.data.DocumentTable
import net.starshipfights.data.Id
import net.starshipfights.data.admiralty.Admiral
import net.starshipfights.data.admiralty.BattleRecord
import net.starshipfights.data.admiralty.ShipInDrydock
import net.starshipfights.data.admiralty.ShipMemorial
import net.starshipfights.data.auth.User
import net.starshipfights.data.createToken
import org.litote.kmongo.combine
import org.litote.kmongo.`in`
import org.litote.kmongo.inc
import org.litote.kmongo.setValue
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.collections.*

data class GameToken(val hostToken: String, val joinToken: String)

object GameManager {
	private val games = ConcurrentCurator(mutableMapOf<String, GameEntry>())
	
	suspend fun initGame(hostInfo: InGameAdmiral, guestInfo: InGameAdmiral, battleInfo: BattleInfo): GameToken {
		val gameState = GameState(
			start = generateGameStart(hostInfo, guestInfo, battleInfo),
			hostInfo = hostInfo,
			guestInfo = guestInfo,
			battleInfo = battleInfo,
			subplots = generateSubplots(battleInfo.size, GlobalSide.HOST) + generateSubplots(battleInfo.size, GlobalSide.GUEST)
		)
		
		val session = GameSession(gameState)
		DocumentTable.launch {
			session.gameStart.join()
			val startedAt = Instant.now()
			
			val end = session.gameEnd.await()
			val endedAt = Instant.now()
			
			onGameEnd(session.state.value, end, startedAt, endedAt)
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
	}.also {
		if (it)
			gameStartMutable.complete()
		else
			onPacket(player.other, PlayerAction.TimeOut)
	}
	
	private val gameStartMutable = Job()
	val gameStart: Job
		get() = gameStartMutable
	
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
					if (gameStartMutable.isActive)
						gameStartMutable.cancel()
					gameEndMutable.complete(result)
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
		listOf(
			// Game state changes
			launch {
				gameSession.state.collect { state ->
					sendObject(GameEvent.serializer(), GameEvent.StateChange(state))
				}
			},
			// Invalid action messages
			launch {
				for (errorMessage in gameSession.errorMessages(playerSide)) {
					sendObject(GameEvent.serializer(), GameEvent.InvalidAction(errorMessage))
				}
			}
		).joinAll()
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
				gameSession.onPacket(playerSide, packet)
		}
	}
	
	val gameEnd = gameSession.gameEnd.await()
	sendObject(GameEvent.serializer(), gameEnd)
	
	sendEventsJob.cancelAndJoin()
	receiveActionsJob.cancelAndJoin()
}

private val BattleSize.shipPointsPerAcumen: Int
	get() = when (this) {
		BattleSize.SKIRMISH -> 5
		BattleSize.RAID -> 5
		BattleSize.FIREFIGHT -> 5
		BattleSize.BATTLE -> 5
		BattleSize.GRAND_CLASH -> 10
		BattleSize.APOCALYPSE -> 10
		BattleSize.LEGENDARY_STRUGGLE -> 10
		BattleSize.CRUCIBLE_OF_HISTORY -> 10
	}

private val BattleSize.acumenPerSubplotWon: Int
	get() = numPoints / 100

private suspend fun onGameEnd(gameState: GameState, gameEnd: GameEvent.GameEnd, startedAt: Instant, endedAt: Instant) {
	val damagedShipReadyAt = endedAt.plus(6, ChronoUnit.HOURS)
	val intactShipReadyAt = endedAt.plus(4, ChronoUnit.HOURS)
	val escapedShipReadyAt = endedAt.plus(4, ChronoUnit.HOURS)
	
	val shipWrecks = gameState.destroyedShips
	val ships = gameState.ships
	
	val hostAdmiralId = gameState.hostInfo.id.reinterpret<Admiral>()
	val guestAdmiralId = gameState.guestInfo.id.reinterpret<Admiral>()
	
	val battleRecord = BattleRecord(
		battleInfo = gameState.battleInfo,
		
		whenStarted = startedAt,
		whenEnded = endedAt,
		
		hostUser = gameState.hostInfo.user.id.reinterpret(),
		guestUser = gameState.guestInfo.user.id.reinterpret(),
		
		hostAdmiral = hostAdmiralId,
		guestAdmiral = guestAdmiralId,
		
		hostEndingMessage = victoryTitle(GlobalSide.HOST, gameEnd.winner, gameEnd.subplotOutcomes),
		guestEndingMessage = victoryTitle(GlobalSide.GUEST, gameEnd.winner, gameEnd.subplotOutcomes),
		
		winner = gameEnd.winner,
		winMessage = gameEnd.message
	)
	
	val destructions = shipWrecks.filterValues { !it.isEscape }
	val destroyedShips = destructions.keys.map { it.reinterpret<ShipInDrydock>() }.toSet()
	val rememberedShips = destructions.values.map { wreck ->
		ShipMemorial(
			id = Id("RIP_${wreck.id.id}"),
			name = wreck.ship.name,
			shipType = wreck.ship.shipType,
			destroyedAt = wreck.wreckedAt.instant,
			owningAdmiral = when (wreck.owner) {
				GlobalSide.HOST -> hostAdmiralId
				GlobalSide.GUEST -> guestAdmiralId
			},
			destroyedIn = battleRecord.id
		)
	}
	
	val escapedShips = shipWrecks.filterValues { it.isEscape }.keys.map { it.reinterpret<ShipInDrydock>() }.toSet()
	val damagedShips = ships.filterValues { it.hullAmount < it.durability.maxHullPoints }.keys.map { it.reinterpret<ShipInDrydock>() }.toSet()
	val intactShips = ships.keys.map { it.reinterpret<ShipInDrydock>() }.toSet() - damagedShips
	
	val battleSize = gameState.battleInfo.size
	
	val hostAcumenGainFromShips = shipWrecks.values.filter { it.owner == GlobalSide.GUEST && !it.isEscape }.sumOf { it.ship.pointCost / battleSize.shipPointsPerAcumen }
	val hostAcumenGainFromSubplots = gameEnd.subplotOutcomes.filterKeys { it.player == GlobalSide.HOST }.count { (_, outcome) -> outcome == SubplotOutcome.WON } * battleSize.acumenPerSubplotWon
	val hostAcumenGain = hostAcumenGainFromShips + hostAcumenGainFromSubplots
	val hostPayment = hostAcumenGain * 2
	
	val guestAcumenGainFromShips = shipWrecks.values.filter { it.owner == GlobalSide.HOST && !it.isEscape }.sumOf { it.ship.pointCost / battleSize.shipPointsPerAcumen }
	val guestAcumenGainFromSubplots = gameEnd.subplotOutcomes.filterKeys { it.player == GlobalSide.GUEST }.count { (_, outcome) -> outcome == SubplotOutcome.WON } * battleSize.acumenPerSubplotWon
	val guestAcumenGain = guestAcumenGainFromShips + guestAcumenGainFromSubplots
	val guestPayment = guestAcumenGain * 2
	
	coroutineScope {
		launch {
			ShipMemorial.put(rememberedShips)
		}
		launch {
			ShipInDrydock.remove(ShipInDrydock::id `in` destroyedShips)
		}
		launch {
			ShipInDrydock.update(ShipInDrydock::id `in` damagedShips, setValue(ShipInDrydock::readyAt, damagedShipReadyAt))
		}
		launch {
			ShipInDrydock.update(ShipInDrydock::id `in` intactShips, setValue(ShipInDrydock::readyAt, intactShipReadyAt))
		}
		launch {
			ShipInDrydock.update(ShipInDrydock::id `in` escapedShips, setValue(ShipInDrydock::readyAt, escapedShipReadyAt))
		}
		
		launch {
			Admiral.set(
				hostAdmiralId, combine(
					inc(Admiral::acumen, hostAcumenGain),
					inc(Admiral::money, hostPayment),
				)
			)
		}
		launch {
			Admiral.set(
				guestAdmiralId, combine(
					inc(Admiral::acumen, guestAcumenGain),
					inc(Admiral::money, guestPayment),
				)
			)
		}
		
		launch {
			BattleRecord.put(battleRecord)
		}
	}
}
