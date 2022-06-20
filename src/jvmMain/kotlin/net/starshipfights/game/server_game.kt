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
import net.starshipfights.game.ai.AISession
import net.starshipfights.game.ai.aiPlayer
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
	
	suspend fun init1v1Game(hostInfo: InGameAdmiral, guestInfo: InGameAdmiral, battleInfo: BattleInfo): GameToken {
		val gameState = generate1v1GameInitialState(hostInfo, guestInfo, battleInfo)
		
		val session = GameSession1v1(gameState)
		DocumentTable.launch {
			session.gameStart.join()
			val startedAt = Instant.now()
			
			val end = session.gameEnd.await()
			val endedAt = Instant.now()
			
			on1v1GameEnd(session.state.value, end, startedAt, endedAt)
		}
		
		val hostId = createToken()
		val joinId = createToken()
		games.use {
			it[hostId] = GameEntry(hostInfo.user.id.reinterpret(), GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation), session)
			it[joinId] = GameEntry(guestInfo.user.id.reinterpret(), GlobalShipController(GlobalSide.GUEST, GlobalShipController.Player1Disambiguation), session)
		}
		
		return GameToken(hostId, joinId)
	}
	
	suspend fun init2v1Game(hostInfo: InGameAdmiral, guestInfo: InGameAdmiral, enemyFaction: Faction, enemyFlavor: FactionFlavor, battleInfo: BattleInfo): GameToken {
		val gameState = generate2v1GameInitialState(hostInfo, guestInfo, enemyFaction, enemyFlavor, battleInfo)
		
		val session = GameSession2v1(gameState)
		DocumentTable.launch {
			session.gameStart.join()
			val startedAt = Instant.now()
			
			val aiJob = launch {
				session.gameStart.join()
				
				val aiSide = GlobalShipController(GlobalSide.GUEST, GlobalShipController.Player1Disambiguation)
				val aiActions = Channel<PlayerAction>()
				val aiEvents = Channel<GameEvent>()
				val aiSession = AISession(aiSide, aiActions, aiEvents)
				
				listOf(
					launch {
						session.state.collect { state ->
							aiEvents.send(GameEvent.StateChange(state))
						}
					},
					launch {
						for (errorMessage in session.errorMessages(aiSide))
							aiEvents.send(GameEvent.InvalidAction(errorMessage))
					},
					launch {
						for (action in aiActions)
							session.onPacket(aiSide, action)
					},
					launch {
						aiPlayer(aiSession, gameState)
					}
				).joinAll()
			}
			
			val end = session.gameEnd.await()
			val endedAt = Instant.now()
			
			aiJob.cancel()
			
			on2v1GameEnd(session.state.value, end, startedAt, endedAt)
		}
		
		val hostId = createToken()
		val joinId = createToken()
		games.use {
			it[hostId] = GameEntry(hostInfo.user.id.reinterpret(), GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation), session)
			it[joinId] = GameEntry(guestInfo.user.id.reinterpret(), GlobalShipController(GlobalSide.HOST, GlobalShipController.Player2Disambiguation), session)
		}
		
		return GameToken(hostId, joinId)
	}
	
	suspend fun joinGame(userId: Id<User>, token: String, remove: Boolean): GameEntry? {
		return games.use { if (remove) it.remove(token) else it[token] }?.takeIf { it.userId == userId }
	}
}

class GameEntry(val userId: Id<User>, val side: GlobalShipController, val session: GameSession)

sealed class GameSession(gameState: GameState, private val stateInterceptor: GameStateInterceptor = NoopInterceptor) {
	protected val gameStartMutable = Job()
	val gameStart: Job
		get() = gameStartMutable
	
	abstract suspend fun enter(player: GlobalShipController): Boolean
	
	private val stateMutable = MutableStateFlow(gameState)
	private val stateMutex = Mutex()
	
	val state = stateMutable.asStateFlow()
	
	private val errorMessages = mutableMapOf<GlobalShipController, Channel<String>>()
	
	private fun errorMessageChannel(player: GlobalShipController) = errorMessages[player] ?: Channel<String>(Channel.UNLIMITED).also {
		errorMessages[player] = it
	}
	
	fun errorMessages(player: GlobalShipController): ReceiveChannel<String> = errorMessageChannel(player)
	
	private val gameEndMutable = CompletableDeferred<GameEvent.GameEnd>()
	val gameEnd: Deferred<GameEvent.GameEnd>
		get() = gameEndMutable
	
	suspend fun onPacket(player: GlobalShipController, packet: PlayerAction) {
		if (gameEnd.isCompleted) return
		
		stateMutex.withLock {
			when (val result = state.value.after(player, packet)) {
				is GameEvent.StateChange -> {
					stateMutable.value = stateInterceptor.onStateChange(stateMutable.value, result.newState)
					result.newState.checkVictory()?.let { gameEndMutable.complete(it) }
				}
				is GameEvent.InvalidAction -> {
					errorMessageChannel(player).send(result.message)
				}
				is GameEvent.GameEnd -> {
					gameEndMutable.complete(result)
					gameStartMutable.complete()
				}
			}
		}
	}
	
	suspend fun onClose(player: GlobalShipController) {
		onPacket(player, PlayerAction.Disconnect)
	}
}

class GameSession1v1(gameState: GameState, stateInterceptor: GameStateInterceptor = NoopInterceptor) : GameSession(gameState, stateInterceptor) {
	private val hostEnter = Job()
	private val guestEnter = Job()
	
	override suspend fun enter(player: GlobalShipController) = when (player.side) {
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
			onPacket(GlobalShipController(player.side.other, GlobalShipController.Player1Disambiguation), PlayerAction.TimeOut)
	}
}

class GameSession2v1(gameState: GameState, stateInterceptor: GameStateInterceptor = NoopInterceptor) : GameSession(gameState, stateInterceptor) {
	private val player1Enter = Job()
	private val player2Enter = Job()
	
	override suspend fun enter(player: GlobalShipController) = when (player.disambiguation) {
		GlobalShipController.Player1Disambiguation -> {
			player1Enter.complete()
			withTimeoutOrNull(30_000L) {
				player2Enter.join()
				true
			} ?: false
		}
		GlobalShipController.Player2Disambiguation -> {
			player2Enter.complete()
			withTimeoutOrNull(30_000L) {
				player1Enter.join()
				true
			} ?: false
		}
		else -> null
	}?.also {
		if (it)
			gameStartMutable.complete()
		else if (player.disambiguation == GlobalShipController.Player1Disambiguation)
			onPacket(GlobalShipController(player.side, GlobalShipController.Player2Disambiguation), PlayerAction.TimeOut)
		else
			onPacket(GlobalShipController(player.side, GlobalShipController.Player1Disambiguation), PlayerAction.TimeOut)
	} ?: false
}

suspend fun DefaultWebSocketServerSession.gameEndpoint(user: User, token: String) {
	val gameEntry = GameManager.joinGame(user.id, token, true) ?: closeAndReturn("That battle is not available") { return }
	val playerSide = gameEntry.side
	val gameSession = gameEntry.session
	
	val opponentEntered = gameSession.enter(playerSide)
	sendObject(GameBeginning.serializer(), GameBeginning(opponentEntered))
	if (!opponentEntered) return
	
	val closeHandler = closeReason.invokeOnCompletion {
		DocumentTable.launch {
			gameSession.onClose(playerSide)
		}
	}
	
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
			val packet = receiveObject(PlayerAction.serializer()) { closeAndReturn { return@launch } }
			
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
	
	closeHandler.dispose()
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

private suspend fun on1v1GameEnd(gameState: GameState, gameEnd: GameEvent.GameEnd, startedAt: Instant, endedAt: Instant) {
	val damagedShipReadyAt = endedAt.plus(6, ChronoUnit.HOURS)
	val escapedShipReadyAt = endedAt.plus(4, ChronoUnit.HOURS)
	
	val shipWrecks = gameState.destroyedShips
	val ships = gameState.ships
	
	val hostInfo = gameState.hostInfo.values.single()
	val guestInfo = gameState.guestInfo.values.single()
	
	val hostAdmiralId = hostInfo.id.reinterpret<Admiral>()
	val guestAdmiralId = guestInfo.id.reinterpret<Admiral>()
	
	val battleRecord = BattleRecord(
		battleInfo = gameState.battleInfo,
		
		whenStarted = startedAt,
		whenEnded = endedAt,
		
		hostUser = hostInfo.user.id.reinterpret(),
		guestUser = guestInfo.user.id.reinterpret(),
		
		hostAdmiral = hostAdmiralId,
		guestAdmiral = guestAdmiralId,
		
		hostEndingMessage = victoryTitle(GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation), gameEnd.winner, gameEnd.subplotOutcomes),
		guestEndingMessage = victoryTitle(GlobalShipController(GlobalSide.GUEST, GlobalShipController.Player1Disambiguation), gameEnd.winner, gameEnd.subplotOutcomes),
		
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
			owningAdmiral = when (wreck.owner.side) {
				GlobalSide.HOST -> hostAdmiralId
				GlobalSide.GUEST -> guestAdmiralId
			},
			destroyedIn = battleRecord.id
		)
	}
	
	val escapedShips = shipWrecks.filterValues { it.isEscape }.keys.map { it.reinterpret<ShipInDrydock>() }.toSet()
	val damagedShips = ships.filterValues { it.hullAmount < it.durability.maxHullPoints || it.troopsAmount < it.durability.troopsDefense }.keys.map { it.reinterpret<ShipInDrydock>() }.toSet()
	
	val battleSize = gameState.battleInfo.size
	
	val hostAcumenGainFromShips = shipWrecks.values.filter { it.owner.side == GlobalSide.GUEST && !it.isEscape }.sumOf { it.ship.pointCost / battleSize.shipPointsPerAcumen }
	val hostAcumenGainFromSubplots = gameEnd.subplotOutcomes.filterKeys { it.player.side == GlobalSide.HOST }.count { (_, outcome) -> outcome == SubplotOutcome.WON } * battleSize.acumenPerSubplotWon
	val hostAcumenGain = hostAcumenGainFromShips + hostAcumenGainFromSubplots
	val hostPayment = hostAcumenGain * 2
	
	val guestAcumenGainFromShips = shipWrecks.values.filter { it.owner.side == GlobalSide.HOST && !it.isEscape }.sumOf { it.ship.pointCost / battleSize.shipPointsPerAcumen }
	val guestAcumenGainFromSubplots = gameEnd.subplotOutcomes.filterKeys { it.player.side == GlobalSide.GUEST }.count { (_, outcome) -> outcome == SubplotOutcome.WON } * battleSize.acumenPerSubplotWon
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

private suspend fun on2v1GameEnd(gameState: GameState, gameEnd: GameEvent.GameEnd, startedAt: Instant, endedAt: Instant) {
	val damagedShipReadyAt = endedAt.plus(6, ChronoUnit.HOURS)
	val escapedShipReadyAt = endedAt.plus(4, ChronoUnit.HOURS)
	
	val shipWrecks = gameState.destroyedShips
	val ships = gameState.ships
	
	val hostInfo = gameState.hostInfo.getValue(GlobalShipController.Player1Disambiguation)
	val guestInfo = gameState.hostInfo.getValue(GlobalShipController.Player2Disambiguation)
	
	val hostAdmiralId = hostInfo.id.reinterpret<Admiral>()
	val guestAdmiralId = guestInfo.id.reinterpret<Admiral>()
	
	val battleRecord = BattleRecord(
		battleInfo = gameState.battleInfo,
		
		whenStarted = startedAt,
		whenEnded = endedAt,
		
		hostUser = hostInfo.user.id.reinterpret(),
		guestUser = guestInfo.user.id.reinterpret(),
		
		hostAdmiral = hostAdmiralId,
		guestAdmiral = guestAdmiralId,
		
		hostEndingMessage = victoryTitle(GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation), gameEnd.winner, gameEnd.subplotOutcomes),
		guestEndingMessage = victoryTitle(GlobalShipController(GlobalSide.HOST, GlobalShipController.Player2Disambiguation), gameEnd.winner, gameEnd.subplotOutcomes),
		
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
			owningAdmiral = when (wreck.owner.side) {
				GlobalSide.HOST -> hostAdmiralId
				GlobalSide.GUEST -> guestAdmiralId
			},
			destroyedIn = battleRecord.id
		)
	}
	
	val escapedShips = shipWrecks.filterValues { it.isEscape }.keys.map { it.reinterpret<ShipInDrydock>() }.toSet()
	val damagedShips = ships.filterValues { it.hullAmount < it.durability.maxHullPoints || it.troopsAmount < it.durability.troopsDefense }.keys.map { it.reinterpret<ShipInDrydock>() }.toSet()
	
	val battleSize = gameState.battleInfo.size
	
	val playersAcumenGainFromShips = shipWrecks.values.filter { it.owner.side == GlobalSide.GUEST && !it.isEscape }.sumOf { it.ship.pointCost / battleSize.shipPointsPerAcumen }
	val playersAcumenGainFromSubplots = gameEnd.subplotOutcomes.filterKeys { it.player.side == GlobalSide.HOST }.count { (_, outcome) -> outcome == SubplotOutcome.WON } * battleSize.acumenPerSubplotWon
	val playersAcumenGain = playersAcumenGainFromShips + playersAcumenGainFromSubplots
	val playersPayment = playersAcumenGain * 2
	
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
			ShipInDrydock.update(ShipInDrydock::id `in` escapedShips, setValue(ShipInDrydock::readyAt, escapedShipReadyAt))
		}
		
		launch {
			Admiral.set(
				hostAdmiralId, combine(
					inc(Admiral::acumen, playersAcumenGain),
					inc(Admiral::money, playersPayment),
				)
			)
		}
		launch {
			Admiral.set(
				guestAdmiralId, combine(
					inc(Admiral::acumen, playersAcumenGain),
					inc(Admiral::money, playersPayment),
				)
			)
		}
		
		launch {
			BattleRecord.put(battleRecord)
		}
	}
}
