package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id
import kotlin.math.abs
import kotlin.math.exp
import kotlin.random.Random

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

fun GameState.afterPlayerReady(playerSide: GlobalSide) = if (ready == playerSide.other) {
	var newShips = ships
	val newWrecks = destroyedShips.toMutableMap()
	val newChatEntries = mutableListOf<ChatEntry>()
	
	when (phase) {
		is GamePhase.Move -> {
			// Auto-move drifting ships
			newShips = newShips.mapValues { (_, ship) ->
				if (ship.isDoneCurrentPhase) ship
				else ship.copy(position = ship.position.drift)
			}
			
			// Ships that move off the battlefield are considered to disengage
			newShips = newShips.mapNotNull fleeingShips@{ (id, ship) ->
				val r = ship.position.currentLocation.vector
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
				else if (newShips.values.any { it.owner != ship.owner && (it.position.currentLocation - ship.position.currentLocation).length <= SHIP_SENSOR_RANGE })
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
				
				if (ship.strikeCraftDisrupted) {
					(ship.fighterWings + ship.bomberWings).forEach {
						val (carrierId, wingId) = it
						val maxDamage = (newShips[carrierId]?.armaments?.weaponInstances?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
						strikeWingDamage[it] = Random.nextDouble() * maxDamage
					}
					return@strikeBombard id to ship
				}
				
				val totalFighterHealth = ship.fighterWings.sumOf { (carrierId, wingId) ->
					(newShips[carrierId]?.armaments?.weaponInstances?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
				}
				
				val totalBomberHealth = ship.bomberWings.sumOf { (carrierId, wingId) ->
					(newShips[carrierId]?.armaments?.weaponInstances?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
				}
				
				val maxBomberWingOutput = exp(totalBomberHealth - totalFighterHealth)
				val maxFighterWingOutput = exp(totalFighterHealth - totalBomberHealth)
				
				ship.fighterWings.forEach { strikeWingDamage[it] = Random.nextDouble() * maxBomberWingOutput }
				ship.bomberWings.forEach { strikeWingDamage[it] = Random.nextDouble() * maxFighterWingOutput }
				
				var hits = 0
				var chanceOfShipDamage = (maxBomberWingOutput - maxFighterWingOutput).coerceAtLeast(0.0) / 2
				while (chanceOfShipDamage >= 1.0) {
					hits++
					chanceOfShipDamage -= 1.0
				}
				if (Random.nextDouble() < chanceOfShipDamage)
					hits++
				
				when (val impactResult = ship.impact(hits)) {
					is ImpactResult.Damaged -> id to impactResult.ship
					is ImpactResult.Destroyed -> {
						newWrecks[id] = impactResult.ship
						newChatEntries += ChatEntry.ShipDestroyed(id, Moment.now, ShipDestructionType.Bombers)
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
			
			// Recall strike craft and regenerate weapon and shield powers
			newShips = newShips.mapValues { (_, ship) ->
				ship.copy(
					weaponAmount = ship.powerMode.weapons,
					shieldAmount = (ship.shieldAmount..ship.powerMode.shields).random(),
					
					fighterWings = emptyList(),
					bomberWings = emptyList(),
					usedArmaments = emptySet(),
				)
			}
		}
		else -> {
			// do nothing
		}
	}
	
	copy(phase = phase.next(), ready = null, ships = newShips.mapValues { (_, ship) -> ship.copy(isDoneCurrentPhase = false) }, chatBox = chatBox + newChatEntries)
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

fun GlobalSide.relativeTo(me: GlobalSide) = if (this == me) LocalSide.BLUE else LocalSide.RED

enum class LocalSide {
	BLUE, RED
}

val LocalSide.htmlColor: String
	get() = when (this) {
		LocalSide.BLUE -> "#3399FF"
		LocalSide.RED -> "#FF6666"
	}
