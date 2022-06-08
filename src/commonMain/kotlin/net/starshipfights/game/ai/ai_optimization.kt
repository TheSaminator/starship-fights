package net.starshipfights.game.ai

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.starshipfights.data.Id
import net.starshipfights.game.*
import kotlin.math.PI
import kotlin.random.Random

val allInstincts = listOf(
	combatTargetShipWeight,
	combatAvengeShipwrecks,
	combatAvengeShipWeight,
	combatPrioritization,
	combatAvengeAttacks,
	combatForgiveTarget,
	combatPreyOnTheWeak,
	combatFrustratedByFailedAttacks,
	
	deployEscortFocus,
	deployCruiserFocus,
	deployBattleshipFocus,
	
	navAggression,
	navPassivity,
	navLustForBlood,
	navSqueamishness,
	navTunnelVision,
	navOptimality,
)

fun genInstinctCandidates(count: Int): Set<Instincts> {
	return Random.nextOrthonormalBasis(allInstincts.size).take(count).map { vector ->
		Instincts.fromValues((allInstincts zip vector.values).associate { (key, value) ->
			key.key to key.denormalize(value)
		})
	}.toSet()
}

class TestSession(gameState: GameState) {
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

suspend fun performTestSession(gameState: GameState, hostInstincts: Instincts, guestInstincts: Instincts): GlobalSide? {
	val testSession = TestSession(gameState)
	
	val hostActions = Channel<PlayerAction>()
	val hostEvents = Channel<GameEvent>()
	val hostSession = AISession(GlobalSide.HOST, hostActions, hostEvents, hostInstincts)
	
	val guestActions = Channel<PlayerAction>()
	val guestEvents = Channel<GameEvent>()
	val guestSession = AISession(GlobalSide.GUEST, guestActions, guestEvents, guestInstincts)
	
	return coroutineScope {
		val hostHandlingJob = launch {
			launch {
				listOf(
					// Game state changes
					launch {
						testSession.state.collect { state ->
							hostEvents.send(GameEvent.StateChange(state))
						}
					},
					// Invalid action messages
					launch {
						for (errorMessage in testSession.errorMessages(GlobalSide.HOST)) {
							hostEvents.send(GameEvent.InvalidAction(errorMessage))
						}
					}
				).joinAll()
			}
			
			launch {
				for (action in hostActions)
					testSession.onPacket(GlobalSide.HOST, action)
			}
			
			aiPlayer(hostSession, testSession.state.value)
		}
		
		val guestHandlingJob = launch {
			launch {
				listOf(
					// Game state changes
					launch {
						testSession.state.collect { state ->
							guestEvents.send(GameEvent.StateChange(state))
						}
					},
					// Invalid action messages
					launch {
						for (errorMessage in testSession.errorMessages(GlobalSide.GUEST)) {
							guestEvents.send(GameEvent.InvalidAction(errorMessage))
						}
					}
				).joinAll()
			}
			
			launch {
				for (action in guestActions)
					testSession.onPacket(GlobalSide.GUEST, action)
			}
			
			aiPlayer(guestSession, testSession.state.value)
		}
		
		val gameEnd = testSession.gameEnd.await()
		
		hostHandlingJob.cancel()
		guestHandlingJob.cancel()
		
		gameEnd.winner
	}
}

fun generateFleet(faction: Faction, rank: AdmiralRank, side: GlobalSide): Map<Id<Ship>, Ship> = ShipWeightClass.values()
	.flatMap { swc ->
		val shipTypes = ShipType.values().filter { st ->
			st.weightClass == swc && st.faction == faction
		}.shuffled()
		
		if (shipTypes.isEmpty())
			emptyList()
		else
			(0 until ((rank.maxShipTier.ordinal - swc.tier.ordinal + 1) * 2).coerceAtLeast(0)).map { i ->
				shipTypes[i % shipTypes.size]
			}
	}
	.let { shipTypes ->
		var shipCount = 0
		shipTypes.map { st ->
			val name = "${side}_${++shipCount}"
			Ship(
				id = Id(name),
				name = name,
				shipType = st,
			)
		}.associateBy { it.id }
	}

fun generateOptimizationInitialState(hostFaction: Faction, guestFaction: Faction, battleInfo: BattleInfo): GameState {
	val battleWidth = (25..35).random() * 500.0
	val battleLength = (15..45).random() * 500.0
	
	val deployWidth2 = battleWidth / 2
	val deployLength2 = 875.0
	
	val hostDeployCenter = Position(Vec2(0.0, (-battleLength / 2) + deployLength2))
	val guestDeployCenter = Position(Vec2(0.0, (battleLength / 2) - deployLength2))
	
	val rank = battleInfo.size.minRank
	
	return GameState(
		start = GameStart(
			battleWidth, battleLength,
			
			PlayerStart(
				hostDeployCenter,
				PI / 2,
				PickBoundary.Rectangle(hostDeployCenter, deployWidth2, deployLength2),
				PI / 2,
				generateFleet(hostFaction, rank, GlobalSide.HOST)
			),
			
			PlayerStart(
				guestDeployCenter,
				-PI / 2,
				PickBoundary.Rectangle(guestDeployCenter, deployWidth2, deployLength2),
				-PI / 2,
				generateFleet(guestFaction, rank, GlobalSide.GUEST)
			)
		),
		hostInfo = InGameAdmiral(
			id = Id(GlobalSide.HOST.name),
			user = InGameUser(
				id = Id(GlobalSide.HOST.name),
				username = GlobalSide.HOST.name
			),
			name = GlobalSide.HOST.name,
			isFemale = false,
			faction = hostFaction,
			rank = rank
		),
		guestInfo = InGameAdmiral(
			id = Id(GlobalSide.GUEST.name),
			user = InGameUser(
				id = Id(GlobalSide.GUEST.name),
				username = GlobalSide.GUEST.name
			),
			name = GlobalSide.GUEST.name,
			isFemale = false,
			faction = guestFaction,
			rank = rank
		),
		battleInfo = battleInfo,
		subplots = emptySet(),
	)
}

data class InstinctGamePairing(
	val host: Instincts,
	val guest: Instincts
)

suspend fun performTrials(numTrialsPerPairing: Int, instincts: Set<Instincts>, validBattleSizes: Set<BattleSize> = BattleSize.values().toSet(), validFactions: Set<Faction> = Faction.values().toSet(), cancellationJob: Job = Job(), onProgress: suspend () -> Unit = {}): Map<InstinctGamePairing, Int> {
	return coroutineScope {
		instincts.associateWith { host ->
			async {
				instincts.map { guest ->
					async {
						(1..numTrialsPerPairing).map {
							async {
								val battleSize = validBattleSizes.random()
								
								val hostFaction = validFactions.random()
								val guestFaction = validFactions.random()
								
								val gameState = generateOptimizationInitialState(hostFaction, guestFaction, BattleInfo(battleSize, BattleBackground.BLUE_BROWN))
								val winner = withTimeoutOrNull((20_000L * numTrialsPerPairing) + (400L * numTrialsPerPairing * numTrialsPerPairing)) {
									val deferred = async(cancellationJob) {
										performTestSession(gameState, host, guest)
									}
									
									select<GlobalSide?> {
										cancellationJob.onJoin { null }
										deferred.onAwait { it }
									}
								}
								
								logInfo("A trial has ended! Winner: ${winner ?: "NEITHER"}")
								onProgress()
								
								when (winner) {
									GlobalSide.HOST -> 1
									GlobalSide.GUEST -> -1
									else -> 0
								}
							}
						}.map { guest to it.await() }
					}
				}.flatMap { it.await() }.toMap()
			}
		}.mapValues { (_, it) -> it.await() }.flatten().mapKeys { (k, _) ->
			InstinctGamePairing(k.first, k.second)
		}
	}
}

data class InstinctVictoryPairing(
	val winner: Instincts,
	val loser: Instincts
)

fun Map<InstinctGamePairing, Int>.victoriesFor(instincts: Instincts) = filterKeys { (host, _) -> host == instincts }.values.sum() - filterKeys { (_, guest) -> guest == instincts }.values.sum()

fun Map<InstinctGamePairing, Int>.toVictoryMap() = keys.associate { (host, _) -> host to victoriesFor(host) }

fun Map<InstinctGamePairing, Int>.toVictoryPairingMap() = keys.associate { (host, guest) ->
	InstinctVictoryPairing(host, guest) to ((get(InstinctGamePairing(host, guest)) ?: 0) - (get(InstinctGamePairing(guest, host)) ?: 0))
}

fun Map<Instincts, Int>.successHistograms(numSegments: Int) = allInstincts.associateWith { instinct ->
	val ranges = (0..numSegments).map { it.toDouble() / numSegments }.windowed(2) { (begin, end) ->
		val rBegin = instinct.randRange.start + (begin * instinct.randRange.size)
		val rEnd = instinct.randRange.start + (end * instinct.randRange.size)
		rBegin..rEnd
	}
	
	val perRange = ranges.associateWith { range ->
		filterKeys { it[instinct] in range }.values.sum()
	}
	
	perRange
}
