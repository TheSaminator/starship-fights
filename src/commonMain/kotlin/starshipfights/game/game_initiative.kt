package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id

@Serializable
data class InitiativePair(
	val hostSide: Double,
	val guestSide: Double
) {
	constructor(map: Map<GlobalSide, Double>) : this(
		map[GlobalSide.HOST] ?: 0.0,
		map[GlobalSide.GUEST] ?: 0.0,
	)
	
	operator fun get(side: GlobalSide) = when (side) {
		GlobalSide.HOST -> hostSide
		GlobalSide.GUEST -> guestSide
	}
	
	fun copy(map: Map<GlobalSide, Double>) = copy(
		hostSide = map[GlobalSide.HOST] ?: hostSide,
		guestSide = map[GlobalSide.GUEST] ?: guestSide,
	)
}

fun GameState.calculateMovePhaseInitiative(): InitiativePair = InitiativePair(
	ships
		.values
		.groupBy { it.owner }
		.mapValues { (_, shipList) ->
			shipList
				.filter { !it.isDoneCurrentPhase }
				.sumOf { it.ship.pointCost * it.movementCoefficient }
		}
)

fun GameState.calculateAttackPhaseInitiative(): InitiativePair = InitiativePair(
	ships
		.values
		.groupBy { it.owner }
		.mapValues { (_, shipList) ->
			shipList
				.filter { !it.isDoneCurrentPhase }
				.sumOf { ship ->
					val allWeapons = ship.armaments.weaponInstances
						.filterValues { weaponInstance ->
							ships.values.any { target ->
								target.position.location in ship.getWeaponPickRequest(weaponInstance.weapon).boundary
							}
						}
					val usableWeapons = allWeapons - ship.usedArmaments
					
					val allWeaponShots = allWeapons.values.sumOf { it.weapon.numShots }
					val usableWeaponShots = usableWeapons.values.sumOf { it.weapon.numShots }
					
					ship.ship.pointCost * (usableWeaponShots.toDouble() / allWeaponShots)
				}
		}
)

fun GameState.withRecalculatedInitiative(initiativePairAccessor: GameState.() -> InitiativePair): GameState {
	val initiativePair = initiativePairAccessor()
	
	return copy(
		calculatedInitiative = when {
			initiativePair.hostSide > initiativePair.guestSide -> GlobalSide.HOST
			initiativePair.hostSide < initiativePair.guestSide -> GlobalSide.GUEST
			else -> calculatedInitiative?.other
		}
	)
}

fun GameState.canShipMove(ship: Id<ShipInstance>): Boolean {
	val shipInstance = ships[ship] ?: return false
	return currentInitiative != shipInstance.owner.other
}

fun GameState.canShipAttack(ship: Id<ShipInstance>): Boolean {
	val shipInstance = ships[ship] ?: return false
	return currentInitiative != shipInstance.owner.other
}
