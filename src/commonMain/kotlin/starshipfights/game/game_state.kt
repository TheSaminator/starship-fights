package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id
import kotlin.math.abs
import kotlin.random.Random
import kotlin.random.nextInt

@Serializable
data class GameState(
	val start: GameStart,
	
	val hostInfo: InGameAdmiral,
	val guestInfo: InGameAdmiral,
	val battleInfo: BattleInfo,
	
	val phase: GamePhase = GamePhase.Deploy,
	val ready: GlobalSide? = null,
	
	val ships: Map<Id<ShipInstance>, ShipInstance> = emptyMap(),
	val destroyedShips: Map<Id<ShipInstance>, ShipWreck> = emptyMap(),
	
	val chatBox: List<ChatEntry> = emptyList(),
) {
	fun getShipInfo(id: Id<ShipInstance>) = destroyedShips[id]?.ship ?: ships.getValue(id).ship
	fun getShipOwner(id: Id<ShipInstance>) = destroyedShips[id]?.owner ?: ships.getValue(id).owner
}

fun GameState.canFinishPhase(side: GlobalSide): Boolean {
	return when (phase) {
		GamePhase.Deploy -> {
			val usedPoints = ships.values
				.filter { it.owner == side }
				.sumOf { it.ship.pointCost }
			
			start.playerStart(side).deployableFleet.values.none { usedPoints + it.pointCost <= battleInfo.size.numPoints }
		}
		is GamePhase.Move -> ships.values.filter { it.owner == side }.all { it.isDoneCurrentPhase }
		else -> true
	}
}

private fun GameState.afterPhase(): GameState {
	var newShips = ships
	val newWrecks = destroyedShips.toMutableMap()
	val newChatEntries = mutableListOf<ChatEntry>()
	
	when (phase) {
		is GamePhase.Move -> {
			// Ships that move off the battlefield are considered to disengage
			newShips = newShips.mapNotNull fleeingShips@{ (id, ship) ->
				val r = ship.position.location.vector
				val mx = start.battlefieldWidth / 2
				val my = start.battlefieldLength / 2
				
				if (abs(r.x) > mx || abs(r.y) > my) {
					newWrecks[id] = ShipWreck(ship.ship, ship.owner, true)
					newChatEntries += ChatEntry.ShipEscaped(id, Moment.now)
					return@fleeingShips null
				}
				
				id to ship
			}.toMap()
			
			// Identify enemy ships
			newShips = newShips.mapValues { (_, ship) ->
				if (ship.isIdentified) ship
				else if (newShips.values.any { it.owner != ship.owner && (it.position.location - ship.position.location).length <= SHIP_SENSOR_RANGE })
					ship.copy(isIdentified = true).also {
						newChatEntries += ChatEntry.ShipIdentified(it.id, Moment.now)
					}
				else ship
			}
		}
		is GamePhase.Attack -> {
			val strikeWingDamage = mutableMapOf<ShipHangarWing, Double>()
			
			// Apply damage to ships from strike craft
			newShips = newShips.mapNotNull strikeBombard@{ (id, ship) ->
				if (ship.bomberWings.isEmpty())
					return@strikeBombard id to ship
				
				val totalFighterHealth = ship.fighterWings.sumOf { (carrierId, wingId) ->
					(newShips[carrierId]?.armaments?.weaponInstances?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
				} + (if (ship.canUseTurrets) ship.ship.durability.turretDefense else 0.0)
				
				val totalBomberHealth = ship.bomberWings.sumOf { (carrierId, wingId) ->
					(newShips[carrierId]?.armaments?.weaponInstances?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
				}
				
				val maxBomberWingOutput = smoothNegative(totalBomberHealth - totalFighterHealth)
				val maxFighterWingOutput = smoothNegative(totalFighterHealth - totalBomberHealth)
				
				ship.fighterWings.forEach { strikeWingDamage[it] = Random.nextDouble() * maxBomberWingOutput }
				ship.bomberWings.forEach { strikeWingDamage[it] = Random.nextDouble() * maxFighterWingOutput }
				
				var hits = 0
				var chanceOfShipDamage = smoothNegative(maxBomberWingOutput - maxFighterWingOutput)
				while (chanceOfShipDamage >= 1.0) {
					hits++
					chanceOfShipDamage -= 1.0
				}
				if (Random.nextDouble() < chanceOfShipDamage)
					hits++
				
				when (val impactResult = ship.impact(hits)) {
					is ImpactResult.Damaged -> {
						newChatEntries += ChatEntry.ShipAttacked(
							ship = id,
							attacker = ShipAttacker.Bombers,
							sentAt = Moment.now,
							damageInflicted = hits,
							weapon = null,
							critical = null
						)
						id to impactResult.ship
					}
					is ImpactResult.Destroyed -> {
						newWrecks[id] = impactResult.ship
						newChatEntries += ChatEntry.ShipDestroyed(id, Moment.now, ShipAttacker.Bombers)
						null
					}
				}
			}.toMap()
			
			// Apply damage to strike craft wings
			newShips = newShips.mapValues { (shipId, ship) ->
				val newArmaments = ship.armaments.weaponInstances.mapValues { (weaponId, weapon) ->
					if (weapon is ShipWeaponInstance.Hangar)
						weapon.copy(wingHealth = weapon.wingHealth - (strikeWingDamage[ShipHangarWing(shipId, weaponId)] ?: 0.0))
					else weapon
				}.filterValues { it !is ShipWeaponInstance.Hangar || it.wingHealth > 0.0 }
				
				ship.copy(
					armaments = ShipInstanceArmaments(newArmaments)
				)
			}
			
			// Recall strike craft and regenerate weapon power
			newShips = newShips.mapValues { (_, ship) ->
				ship.copy(
					weaponAmount = ship.powerMode.weapons,
					
					fighterWings = emptySet(),
					bomberWings = emptySet(),
					usedArmaments = emptySet(),
				)
			}
		}
		is GamePhase.Repair -> {
			// Deal fire damage
			newShips = newShips.mapNotNull fireDamage@{ (id, ship) ->
				if (ship.numFires <= 0)
					return@fireDamage id to ship
				
				val hits = Random.nextInt(0..ship.numFires)
				
				when (val impactResult = ship.impact(hits)) {
					is ImpactResult.Damaged -> {
						newChatEntries += ChatEntry.ShipAttacked(
							ship = id,
							attacker = ShipAttacker.Fire,
							sentAt = Moment.now,
							damageInflicted = hits,
							weapon = null,
							critical = null
						)
						id to impactResult.ship
					}
					is ImpactResult.Destroyed -> {
						newWrecks[id] = impactResult.ship
						newChatEntries += ChatEntry.ShipDestroyed(id, Moment.now, ShipAttacker.Fire)
						null
					}
				}
			}.toMap()
			
			// Replenish repair tokens and regenerate shield power
			newShips = newShips.mapValues { (_, ship) ->
				ship.copy(
					shieldAmount = if (ship.canUseShields) (ship.shieldAmount..ship.powerMode.shields).random() else 0,
					usedRepairTokens = 0
				)
			}
		}
		else -> {
			// do nothing
		}
	}
	
	return copy(phase = phase.next(), ships = newShips.mapValues { (_, ship) -> ship.copy(isDoneCurrentPhase = false) }, destroyedShips = newWrecks, chatBox = chatBox + newChatEntries)
}

fun GameState.afterPlayerReady(playerSide: GlobalSide) = if (ready == playerSide.other) {
	afterPhase().copy(ready = null)
} else
	copy(ready = playerSide)

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
