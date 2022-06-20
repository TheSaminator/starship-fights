package net.starshipfights.game

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id
import kotlin.math.roundToInt

@Serializable
data class GameState(
	val start: GameStart,
	
	val hostInfo: Map<String, InGameAdmiral>,
	val guestInfo: Map<String, InGameAdmiral>,
	val battleInfo: BattleInfo,
	
	val subplots: Set<Subplot>,
	
	val phase: GamePhase = GamePhase.Deploy,
	val doneWithPhase: Set<GlobalShipController> = emptySet(),
	val calculatedInitiative: GlobalShipController? = null,
	
	val ships: Map<Id<ShipInstance>, ShipInstance> = emptyMap(),
	val destroyedShips: Map<Id<ShipInstance>, ShipWreck> = emptyMap(),
	
	val chatBox: List<ChatEntry> = emptyList(),
) {
	fun getShipInfo(id: Id<ShipInstance>) = destroyedShips[id]?.ship ?: ships.getValue(id).ship
	fun getShipInfoOrNull(id: Id<ShipInstance>) = destroyedShips[id]?.ship ?: ships[id]?.ship
	
	fun getShipOwner(id: Id<ShipInstance>) = destroyedShips[id]?.owner ?: ships.getValue(id).owner
	fun getShipOwnerOrNull(id: Id<ShipInstance>) = destroyedShips[id]?.owner ?: ships[id]?.owner
	
	fun getUsablePoints(side: GlobalShipController) = (battleInfo.size.numPoints * start.playerStart(side).deployPointsFactor).roundToInt()
}

val GameState.allShipControllers: Set<GlobalShipController>
	get() = (hostInfo.keys.map { GlobalShipController(GlobalSide.HOST, it) } + guestInfo.keys.map { GlobalShipController(GlobalSide.GUEST, it) }).toSet()

fun GameState.allShipControllersOnSide(side: GlobalSide): Map<GlobalShipController, InGameAdmiral> = when (side) {
	GlobalSide.HOST -> hostInfo
	GlobalSide.GUEST -> guestInfo
}.mapKeys { (it, _) -> GlobalShipController(side, it) }

val GameState.currentInitiative: GlobalShipController?
	get() = calculatedInitiative?.takeIf { it !in doneWithPhase }

fun GameState.canFinishPhase(side: GlobalShipController): Boolean {
	return when (phase) {
		GamePhase.Deploy -> {
			val usedPoints = ships.values
				.filter { it.owner == side }
				.sumOf { it.ship.pointCost }
			
			start.playerStart(side).deployableFleet.values.none { usedPoints + it.pointCost <= getUsablePoints(side) }
		}
		else -> true
	}
}

private fun GameState.afterPhase(): GameState {
	var newShips = ships
	val newWrecks = destroyedShips.toMutableMap()
	val newChatEntries = mutableListOf<ChatEntry>()
	var newInitiative: GameState.() -> InitiativeMap = { emptyMap() }
	
	when (phase) {
		GamePhase.Deploy -> {
			return subplots.map { it.key }.fold(this) { newState, key ->
				val subplot = newState.subplots.single { it.key == key }
				subplot.onAfterDeployShips(newState)
			}.copy(
				phase = phase.next(),
				ships = ships.mapValues { (_, ship) ->
					ship.copy(isDoneCurrentPhase = false)
				},
			)
		}
		is GamePhase.Power -> {
			// Prepare for move phase
			newInitiative = { calculateMovePhaseInitiative() }
		}
		is GamePhase.Move -> {
			// Set velocity to 0 for halted ships
			newShips = newShips.mapValues { (_, ship) ->
				if (ship.ship.shipType.faction == Faction.FELINAE_FELICES && !ship.isDoneCurrentPhase)
					ship.copy(currentVelocity = 0.0)
				else ship
			}
			
			// Recharge inertialess drive
			newShips = newShips.mapValues { (_, ship) ->
				if (ship.ship.canUseInertialessDrive && ship.usedInertialessDriveShots > 0 && ship.felinaeShipPowerMode != FelinaeShipPowerMode.INERTIALESS_DRIVE)
					ship.copy(usedInertialessDriveShots = ship.usedInertialessDriveShots - 1)
				else ship
			}
			
			// Prepare for attack phase
			newInitiative = { calculateAttackPhaseInitiative() }
		}
		is GamePhase.Attack -> {
			val strikeWingDamage = mutableMapOf<ShipHangarWing, Double>()
			
			// Apply damage to ships from strike craft
			newShips = newShips.mapNotNull strikeBombard@{ (id, ship) ->
				val impact = ship.afterBombed(newShips, strikeWingDamage)
				newChatEntries += listOfNotNull(impact.toChatEntry(ShipAttacker.Bombers, null))
				when (impact) {
					is ImpactResult.Damaged -> {
						id to impact.ship
					}
					is ImpactResult.Destroyed -> {
						newWrecks[id] = impact.ship
						null
					}
				}
			}.toMap()
			
			// Apply damage to strike craft wings
			newShips = newShips.mapValues { (_, ship) ->
				ship.afterBombing(strikeWingDamage)
			}
			
			// Deal fire damage
			newShips = newShips.mapNotNull fireDamage@{ (id, ship) ->
				if (ship.numFires <= 0)
					return@fireDamage id to ship
				
				val hits = (0..ship.numFires).random()
				
				val impactResult = ship.impact(hits, true)
				newChatEntries += listOfNotNull(impactResult.toChatEntry(ShipAttacker.Fire, null))
				when (impactResult) {
					is ImpactResult.Damaged -> {
						id to impactResult.ship
					}
					is ImpactResult.Destroyed -> {
						newWrecks[id] = impactResult.ship
						null
					}
				}
			}.toMap()
			
			// Replenish repair tokens, recall strike craft, and regenerate weapons and shields power
			newShips = newShips.mapValues { (_, ship) ->
				ship.copy(
					weaponAmount = ship.powerMode.weapons,
					shieldAmount = if (ship.canUseShields) (ship.shieldAmount..ship.powerMode.shields).random() else 0,
					usedRepairTokens = 0,
					
					hasUsedDisruptionPulse = false,
					
					fighterWings = emptySet(),
					bomberWings = emptySet(),
					usedArmaments = emptySet(),
					
					hasSentBoardingParty = false,
				)
			}
		}
		else -> {
			// do nothing
		}
	}
	
	return copy(
		phase = phase.next(),
		ships = newShips.mapValues { (_, ship) ->
			ship.copy(isDoneCurrentPhase = false)
		},
		destroyedShips = newWrecks,
		chatBox = chatBox + newChatEntries
	).withRecalculatedInitiative(newInitiative)
}

fun GameState.afterPlayerReady(playerSide: GlobalShipController) = if ((doneWithPhase + playerSide) == allShipControllers) {
	afterPhase().copy(doneWithPhase = emptySet())
} else if (phase.usesInitiative)
	copy(doneWithPhase = doneWithPhase + playerSide).withRecalculatedInitiative(
		when (phase) {
			is GamePhase.Move -> ({ calculateMovePhaseInitiative() })
			is GamePhase.Attack -> ({ calculateAttackPhaseInitiative() })
			else -> ({ emptyMap() })
		}
	)
else
	copy(doneWithPhase = doneWithPhase + playerSide)

private fun GameState.victoryMessage(winner: GlobalSide): String {
	val winnerName = allShipControllersOnSide(winner).mapValues { (_, it) -> it.fullName }.values
	val loserName = allShipControllersOnSide(winner.other).mapValues { (_, it) -> it.fullName }.values
	
	val winnerIsPlural = winnerName.size != 1
	val loserIsPlural = loserName.size != 1
	
	return "${winnerName.joinToDisplayString()} ${if (winnerIsPlural) "have" else "has"} won the battle by destroying the fleet${if (loserIsPlural) "s" else ""} of ${loserName.joinToDisplayString()}!"
}

fun GameState.checkVictory(): GameEvent.GameEnd? {
	if (phase == GamePhase.Deploy) return null
	
	val hostDefeated = ships.none { (_, it) -> it.owner.side == GlobalSide.HOST }
	val guestDefeated = ships.none { (_, it) -> it.owner.side == GlobalSide.GUEST }
	
	val winner = if (hostDefeated && guestDefeated)
		null
	else if (hostDefeated)
		GlobalSide.GUEST
	else if (guestDefeated)
		GlobalSide.HOST
	else return null
	
	val subplotsOutcomes = subplots.associate { subplot ->
		subplot.key to subplot.getFinalGameResult(this, winner)
	}
	
	return if (hostDefeated && guestDefeated)
		GameEvent.GameEnd(null, "Both sides have been completely destroyed!", subplotsOutcomes)
	else if (hostDefeated)
		GameEvent.GameEnd(GlobalSide.GUEST, victoryMessage(GlobalSide.GUEST), subplotsOutcomes)
	else if (guestDefeated)
		GameEvent.GameEnd(GlobalSide.HOST, victoryMessage(GlobalSide.HOST), subplotsOutcomes)
	else
		null
}

fun GameState.admiralInfo(side: GlobalShipController) = when (side.side) {
	GlobalSide.HOST -> hostInfo.getValue(side.disambiguation)
	GlobalSide.GUEST -> guestInfo.getValue(side.disambiguation)
}

enum class GlobalSide {
	HOST, GUEST;
	
	val other: GlobalSide
		get() = when (this) {
			HOST -> GUEST
			GUEST -> HOST
		}
}

@Serializable
data class GlobalShipController(val side: GlobalSide, val disambiguation: String) {
	companion object {
		val Player1Disambiguation = "PLAYER 1"
		val Player2Disambiguation = "PLAYER 2"
	}
}

fun GlobalShipController.relativeTo(me: GlobalShipController) = if (this == me)
	LocalSide.GREEN
else if (side == me.side)
	LocalSide.BLUE
else
	LocalSide.RED

enum class LocalSide {
	GREEN, BLUE, RED
}

val LocalSide.htmlColor: String
	get() = when (this) {
		LocalSide.GREEN -> "#55FF55"
		LocalSide.BLUE -> "#5555FF"
		LocalSide.RED -> "#FF5555"
	}
