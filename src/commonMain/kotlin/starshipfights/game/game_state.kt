package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id
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
		is GamePhase.Attack -> {
			val strikeWingDamage = mutableMapOf<ShipHangarWing, Double>()
			
			// Apply damage to ships from strike craft
			newShips = newShips.mapNotNull strikeBombard@{ (id, ship) ->
				when (val impact = ship.afterBombed(newShips, strikeWingDamage)) {
					is ImpactResult.Damaged -> {
						impact.amount?.let { damage ->
							newChatEntries += ChatEntry.ShipAttacked(
								ship = id,
								attacker = ShipAttacker.Bombers,
								sentAt = Moment.now,
								damageInflicted = damage,
								weapon = null,
								critical = impact.critical.report()
							)
						}
						id to impact.ship
					}
					is ImpactResult.Destroyed -> {
						newWrecks[id] = impact.ship
						newChatEntries += ChatEntry.ShipDestroyed(id, Moment.now, ShipAttacker.Bombers)
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
			
			// Replenish repair tokens, recall strike craft, and regenerate weapons and shields power
			newShips = newShips.mapValues { (_, ship) ->
				ship.copy(
					weaponAmount = ship.powerMode.weapons,
					shieldAmount = if (ship.canUseShields) (ship.shieldAmount..ship.powerMode.shields).random() else 0,
					usedRepairTokens = 0,
					
					fighterWings = emptySet(),
					bomberWings = emptySet(),
					usedArmaments = emptySet(),
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
