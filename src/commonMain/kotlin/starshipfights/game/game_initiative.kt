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

fun GameState.getValidAttackersWith(target: ShipInstance): Map<Id<ShipInstance>, Set<Id<ShipWeapon>>> {
	return ships.mapValues { (_, ship) -> isValidAttackerWith(ship, target) }
}

fun GameState.isValidAttackerWith(attacker: ShipInstance, target: ShipInstance): Set<Id<ShipWeapon>> {
	return attacker.armaments.weaponInstances.filterValues {
		isValidTarget(attacker, it, attacker.getWeaponPickRequest(it.weapon), target)
	}.keys
}

fun GameState.isValidTarget(ship: ShipInstance, weapon: ShipWeaponInstance, pickRequest: PickRequest, target: ShipInstance): Boolean {
	val targetPos = target.position.location
	
	return when (val weaponSpec = weapon.weapon) {
		is AreaWeapon ->
			target.owner != ship.owner && (targetPos - pickRequest.boundary.closestPointTo(targetPos)).length < weaponSpec.areaRadius
		else ->
			target.owner in (pickRequest.type as PickType.Ship).allowSides && isValidPick(pickRequest, PickResponse.Ship(target.id))
	}
}

inline fun <T> GameState.aggregateValidTargets(ship: ShipInstance, weapon: ShipWeaponInstance, aggregate: Iterable<ShipInstance>.((ShipInstance) -> Boolean) -> T): T {
	val pickRequest = ship.getWeaponPickRequest(weapon.weapon)
	return ships.values.aggregate { target -> isValidTarget(ship, weapon, pickRequest, target) }
}

fun GameState.hasValidTargets(ship: ShipInstance, weapon: ShipWeaponInstance): Boolean {
	return aggregateValidTargets(ship, weapon) { any(it) }
}

fun GameState.getValidTargets(ship: ShipInstance, weapon: ShipWeaponInstance): List<ShipInstance> {
	return aggregateValidTargets(ship, weapon) { filter(it) }
}

fun GameState.calculateAttackPhaseInitiative(): InitiativePair = InitiativePair(
	ships
		.values
		.groupBy { it.owner }
		.mapValues { (_, shipList) ->
			shipList
				.filter { !it.isDoneCurrentPhase }
				.sumOf { ship ->
					val allWeapons = ship.armaments.weaponInstances
						.filterValues { weapon -> hasValidTargets(ship, weapon) }
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
