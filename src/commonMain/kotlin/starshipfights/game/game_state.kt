package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id

@Serializable
data class GameState(
	val start: GameStart,
	
	val hostInfo: InGameAdmiral,
	val guestInfo: InGameAdmiral,
	val battleInfo: BattleInfo,
	
	val phase: GamePhase = GamePhase.Deploy,
	val doneWithPhase: GlobalSide? = null,
	val calculatedInitiative: GlobalSide? = null,
	
	val ships: Map<Id<ShipInstance>, ShipInstance> = emptyMap(),
	val destroyedShips: Map<Id<ShipInstance>, ShipWreck> = emptyMap(),
	
	val chatBox: List<ChatEntry> = emptyList(),
) {
	fun getShipInfo(id: Id<ShipInstance>) = destroyedShips[id]?.ship ?: ships.getValue(id).ship
	fun getShipOwner(id: Id<ShipInstance>) = destroyedShips[id]?.owner ?: ships.getValue(id).owner
}

val GameState.currentInitiative: GlobalSide?
	get() = calculatedInitiative?.takeIf { it != doneWithPhase }

fun GameState.canFinishPhase(side: GlobalSide): Boolean {
	return when (phase) {
		GamePhase.Deploy -> {
			val usedPoints = ships.values
				.filter { it.owner == side }
				.sumOf { it.ship.pointCost }
			
			start.playerStart(side).deployableFleet.values.none { usedPoints + it.pointCost <= battleInfo.size.numPoints }
		}
		else -> true
	}
}

private fun GameState.afterPhase(): GameState {
	var newShips = ships
	val newWrecks = destroyedShips.toMutableMap()
	val newChatEntries = mutableListOf<ChatEntry>()
	var newInitiative: GameState.() -> InitiativePair = { InitiativePair(emptyMap()) }
	
	when (phase) {
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

fun GameState.afterPlayerReady(playerSide: GlobalSide) = if (doneWithPhase == playerSide.other) {
	afterPhase().copy(doneWithPhase = null)
} else
	copy(doneWithPhase = playerSide)

private fun GameState.victoryMessage(winner: GlobalSide): String {
	val winnerName = admiralInfo(winner).fullName
	val loserName = admiralInfo(winner.other).fullName
	
	return "$winnerName has won the battle by destroying the fleet of $loserName!"
}

fun GameState.checkVictory(): GameEvent.GameEnd? {
	if (phase == GamePhase.Deploy) return null
	
	val hostDefeated = ships.none { (_, it) -> it.owner == GlobalSide.HOST }
	val guestDefeated = ships.none { (_, it) -> it.owner == GlobalSide.GUEST }
	
	return if (hostDefeated && guestDefeated)
		GameEvent.GameEnd(null, "Stalemate: both sides have been completely destroyed!")
	else if (hostDefeated)
		GameEvent.GameEnd(GlobalSide.GUEST, victoryMessage(GlobalSide.GUEST))
	else if (guestDefeated)
		GameEvent.GameEnd(GlobalSide.HOST, victoryMessage(GlobalSide.HOST))
	else
		null
}

fun GameState.admiralInfo(side: GlobalSide) = when (side) {
	GlobalSide.HOST -> hostInfo
	GlobalSide.GUEST -> guestInfo
}

enum class GlobalSide {
	HOST, GUEST;
	
	val other: GlobalSide
		get() = when (this) {
			HOST -> GUEST
			GUEST -> HOST
		}
}

fun GlobalSide.relativeTo(me: GlobalSide) = if (this == me) LocalSide.GREEN else LocalSide.RED

enum class LocalSide {
	GREEN, RED;
	
	val other: LocalSide
		get() = when (this) {
			GREEN -> RED
			RED -> GREEN
		}
}

val LocalSide.htmlColor: String
	get() = when (this) {
		LocalSide.GREEN -> "#55FF55"
		LocalSide.RED -> "#FF5555"
	}
