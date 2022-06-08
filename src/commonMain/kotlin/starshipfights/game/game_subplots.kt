package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id

@Serializable
data class GameObjective(
	val displayText: String,
	val succeeded: Boolean?
)

fun GameState.objectives(forPlayer: GlobalSide): List<GameObjective> = listOf(
	GameObjective("Destroy or rout the enemy fleet", null)
) + subplots.filter { it.forPlayer == forPlayer }.mapNotNull { it.displayObjective(this) }

@Serializable
data class SubplotKey(
	val type: SubplotType,
	val player: GlobalSide,
)

val Subplot.key: SubplotKey
	get() = SubplotKey(type, forPlayer)

@Serializable
sealed class Subplot {
	abstract val type: SubplotType
	abstract val displayName: String
	abstract val forPlayer: GlobalSide
	
	override fun equals(other: Any?): Boolean {
		return other is Subplot && other.key == key
	}
	
	override fun hashCode(): Int {
		return key.hashCode()
	}
	
	abstract fun displayObjective(gameState: GameState): GameObjective?
	
	abstract fun onAfterDeployShips(gameState: GameState): GameState
	abstract fun onGameStateChanged(gameState: GameState): GameState
	abstract fun getFinalGameResult(gameState: GameState, winner: GlobalSide?): SubplotOutcome
	
	protected fun GameState.modifySubplotData(newSubplot: Subplot) = copy(subplots = (subplots - this@Subplot) + newSubplot)
	
	@Serializable
	class ExtendedDuty(override val forPlayer: GlobalSide) : Subplot() {
		override val type: SubplotType
			get() = SubplotType.EXTENDED_DUTY
		
		override val displayName: String
			get() = "Extended Duty"
		
		override fun displayObjective(gameState: GameState) = GameObjective("Win the battle with your fleet worn out from extended duty", null)
		
		private fun ShipInstance.preBattleDamage(): ShipInstance = when ((0..4).random()) {
			0 -> copy(
				hullAmount = (2..hullAmount).random(),
				troopsAmount = (2..troopsAmount).random(),
				modulesStatus = ShipModulesStatus(
					modulesStatus.statuses.mapValues { (_, status) ->
						if (status != ShipModuleStatus.ABSENT && (1..3).random() == 1)
							ShipModuleStatus.DESTROYED
						else
							status
					}
				)
			)
			1 -> copy(
				hullAmount = (2..hullAmount).random(),
				troopsAmount = (2..troopsAmount).random(),
			)
			2 -> copy(
				troopsAmount = (2..troopsAmount).random(),
			)
			else -> this
		}
		
		override fun onAfterDeployShips(gameState: GameState) = gameState.copy(ships = gameState.ships.mapValues { (_, ship) ->
			if (ship.owner == forPlayer)
				ship.preBattleDamage()
			else ship
		})
		
		override fun onGameStateChanged(gameState: GameState) = gameState
		
		override fun getFinalGameResult(gameState: GameState, winner: GlobalSide?) = SubplotOutcome.fromBattleWinner(winner, forPlayer)
	}
	
	@Serializable
	class NoQuarter(override val forPlayer: GlobalSide) : Subplot() {
		override val type: SubplotType
			get() = SubplotType.NO_QUARTER
		
		override val displayName: String
			get() = "Leave No Quarter!"
		
		override fun displayObjective(gameState: GameState): GameObjective {
			val enemyShips = gameState.ships.values.filter { it.owner == forPlayer.other }
			val enemyWrecks = gameState.destroyedShips.values.filter { it.owner == forPlayer.other }
			
			val totalEnemyShipPointCount = enemyShips.sumOf { it.ship.pointCost } + enemyWrecks.sumOf { it.ship.pointCost }
			val escapedShipPointCount = enemyWrecks.filter { it.isEscape }.sumOf { it.ship.pointCost }
			val destroyedShipPointCount = enemyWrecks.filter { !it.isEscape }.sumOf { it.ship.pointCost }
			
			val success = when {
				destroyedShipPointCount * 2 >= totalEnemyShipPointCount -> true
				escapedShipPointCount * 2 >= totalEnemyShipPointCount -> false
				else -> null
			}
			
			return GameObjective("Destroy at least half of the enemy fleet's point value - do not let them escape!", success)
		}
		
		override fun onAfterDeployShips(gameState: GameState) = gameState
		
		override fun onGameStateChanged(gameState: GameState) = gameState
		
		override fun getFinalGameResult(gameState: GameState, winner: GlobalSide?): SubplotOutcome {
			val enemyShips = gameState.ships.values.filter { it.owner == forPlayer.other }
			val enemyWrecks = gameState.destroyedShips.values.filter { it.owner == forPlayer.other }
			
			val totalEnemyShipPointCount = enemyShips.sumOf { it.ship.pointCost } + enemyWrecks.sumOf { it.ship.pointCost }
			val destroyedShipPointCount = enemyWrecks.filter { !it.isEscape }.sumOf { it.ship.pointCost }
			
			return if (destroyedShipPointCount * 2 >= totalEnemyShipPointCount)
				SubplotOutcome.WON
			else
				SubplotOutcome.LOST
		}
	}
	
	@Serializable
	class Vendetta private constructor(override val forPlayer: GlobalSide, private val againstShip: Id<ShipInstance>?, private val outcome: SubplotOutcome) : Subplot() {
		constructor(forPlayer: GlobalSide) : this(forPlayer, null, SubplotOutcome.UNDECIDED)
		constructor(forPlayer: GlobalSide, againstShip: Id<ShipInstance>) : this(forPlayer, againstShip, SubplotOutcome.UNDECIDED)
		
		override val type: SubplotType
			get() = SubplotType.VENDETTA
		
		override val displayName: String
			get() = "Vendetta!"
		
		override fun displayObjective(gameState: GameState): GameObjective? {
			val shipName = gameState.getShipInfoOrNull(againstShip ?: return null)?.fullName ?: return null
			return GameObjective("Destroy the $shipName", outcome.toSuccess)
		}
		
		override fun onAfterDeployShips(gameState: GameState): GameState {
			if (gameState.ships[againstShip] != null) return gameState
			
			val enemyShips = gameState.ships.values.filter { it.owner == forPlayer.other }
			val highestEnemyShipTier = enemyShips.maxOf { it.ship.shipType.weightClass }
			val enemyShipsOfHighestTier = enemyShips.filter { it.ship.shipType.weightClass == highestEnemyShipTier }
			
			val vendettaShip = enemyShipsOfHighestTier.random().id
			return gameState.modifySubplotData(Vendetta(forPlayer, vendettaShip, SubplotOutcome.UNDECIDED))
		}
		
		override fun onGameStateChanged(gameState: GameState): GameState {
			if (outcome != SubplotOutcome.UNDECIDED) return gameState
			
			val vendettaShipWreck = gameState.destroyedShips[againstShip ?: return gameState] ?: return gameState
			return if (vendettaShipWreck.isEscape)
				gameState.modifySubplotData(Vendetta(forPlayer, againstShip, SubplotOutcome.LOST))
			else
				gameState.modifySubplotData(Vendetta(forPlayer, againstShip, SubplotOutcome.WON))
		}
		
		override fun getFinalGameResult(gameState: GameState, winner: GlobalSide?) = if (outcome == SubplotOutcome.UNDECIDED)
			SubplotOutcome.LOST
		else outcome
	}
	
	@Serializable
	class RecoverInformant private constructor(override val forPlayer: GlobalSide, private val onBoardShip: Id<ShipInstance>?, private val outcome: SubplotOutcome, private val mostRecentChatMessages: Moment?) : Subplot() {
		constructor(forPlayer: GlobalSide) : this(forPlayer, null, SubplotOutcome.UNDECIDED, null)
		constructor(forPlayer: GlobalSide, onBoardShip: Id<ShipInstance>) : this(forPlayer, onBoardShip, SubplotOutcome.UNDECIDED, null)
		
		override val type: SubplotType
			get() = SubplotType.RECOVER_INFORMANT
		
		override val displayName: String
			get() = "Recover Informant"
		
		override fun displayObjective(gameState: GameState): GameObjective? {
			val shipName = gameState.getShipInfoOrNull(onBoardShip ?: return null)?.fullName ?: return null
			return GameObjective("Board the $shipName and recover your informant", outcome.toSuccess)
		}
		
		override fun onAfterDeployShips(gameState: GameState): GameState {
			if (gameState.ships[onBoardShip] != null) return gameState
			
			val enemyShips = gameState.ships.values.filter { it.owner == forPlayer.other }
			val lowestEnemyShipTier = enemyShips.minOf { it.ship.shipType.weightClass }
			val enemyShipsNotOfLowestTier = enemyShips.filter { it.ship.shipType.weightClass != lowestEnemyShipTier }.ifEmpty { enemyShips }
			
			val informantShip = enemyShipsNotOfLowestTier.random().id
			return gameState.modifySubplotData(RecoverInformant(forPlayer, informantShip, SubplotOutcome.UNDECIDED, null))
		}
		
		private fun GameState.getNewMessages(readTime: Moment?) = if (readTime == null)
			chatBox
		else
			chatBox.filter { it.sentAt > readTime }
		
		override fun onGameStateChanged(gameState: GameState): GameState {
			if (outcome != SubplotOutcome.UNDECIDED) return gameState
			
			var readTime = mostRecentChatMessages
			for (message in gameState.getNewMessages(mostRecentChatMessages)) {
				when (message) {
					is ChatEntry.ShipEscaped -> if (message.ship == onBoardShip)
						return gameState.modifySubplotData(RecoverInformant(forPlayer, onBoardShip, SubplotOutcome.LOST, null))
					is ChatEntry.ShipDestroyed -> if (message.ship == onBoardShip)
						return gameState.modifySubplotData(RecoverInformant(forPlayer, onBoardShip, SubplotOutcome.LOST, null))
					is ChatEntry.ShipBoarded -> if (message.ship == onBoardShip && (1..3).random() == 1)
						return gameState.modifySubplotData(RecoverInformant(forPlayer, onBoardShip, SubplotOutcome.WON, null))
					else -> {
						// do nothing
					}
				}
				readTime = if (readTime == null || readTime < message.sentAt) message.sentAt else readTime
			}
			
			return gameState.modifySubplotData(RecoverInformant(forPlayer, onBoardShip, SubplotOutcome.UNDECIDED, readTime))
		}
		
		override fun getFinalGameResult(gameState: GameState, winner: GlobalSide?) = if (outcome == SubplotOutcome.UNDECIDED)
			SubplotOutcome.LOST
		else outcome
	}
}

enum class SubplotType(val factory: (GlobalSide) -> Subplot) {
	EXTENDED_DUTY(Subplot::ExtendedDuty),
	NO_QUARTER(Subplot::NoQuarter),
	VENDETTA(Subplot::Vendetta),
	RECOVER_INFORMANT(Subplot::RecoverInformant),
}

fun generateSubplots(battleSize: BattleSize, forPlayer: GlobalSide): Set<Subplot> =
	(1..battleSize.numSubplotsPerPlayer).map {
		SubplotType.values().random().factory(forPlayer)
	}.toSet()

@Serializable
enum class SubplotOutcome {
	UNDECIDED, WON, LOST;
	
	val toSuccess: Boolean?
		get() = when (this) {
			UNDECIDED -> null
			WON -> true
			LOST -> false
		}
	
	companion object {
		fun fromBattleWinner(winner: GlobalSide?, subplotForPlayer: GlobalSide) = when (winner) {
			subplotForPlayer -> WON
			subplotForPlayer.other -> LOST
			else -> UNDECIDED
		}
	}
}

fun victoryTitle(player: GlobalSide, winner: GlobalSide?, subplotOutcomes: Map<SubplotKey, SubplotOutcome>): String {
	val myOutcomes = subplotOutcomes.filterKeys { it.player == player }
	
	return when (winner) {
		player -> {
			val isGlorious = myOutcomes.all { (_, outcome) -> outcome == SubplotOutcome.WON }
			val isPyrrhic = myOutcomes.size >= 2 && myOutcomes.none { (_, outcome) -> outcome == SubplotOutcome.WON }
			
			if (isGlorious)
				"Glorious Victory"
			else if (isPyrrhic)
				"Pyrrhic Victory"
			else
				"Victory"
		}
		player.other -> {
			val isHeroic = myOutcomes.all { (_, outcome) -> outcome == SubplotOutcome.WON }
			val isHumiliating = myOutcomes.size >= 2 && myOutcomes.none { (_, outcome) -> outcome == SubplotOutcome.WON }
			
			if (isHeroic)
				"Heroic Defeat"
			else if (isHumiliating)
				"Humiliating Defeat"
			else
				"Defeat"
		}
		else -> "Stalemate"
	}
}
