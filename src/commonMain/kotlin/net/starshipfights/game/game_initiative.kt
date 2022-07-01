package net.starshipfights.game

import net.starshipfights.data.Id

typealias InitiativeMap = Map<GlobalShipController, Double>

fun GameState.calculateMovePhaseInitiative(): InitiativeMap =
	ships
		.values
		.groupBy { it.owner }
		.mapValues { (_, shipList) ->
			shipList
				.filter { !it.isDoneCurrentPhase }
				.sumOf { it.ship.pointCost * it.movementCoefficient }
		}

fun GameState.getValidAttackersWith(target: ShipInstance): Map<Id<ShipInstance>, Set<Id<ShipWeapon>>> {
	return ships.mapValues { (_, ship) -> isValidAttackerWith(ship, target) }
}

fun GameState.isValidAttackerWith(attacker: ShipInstance, target: ShipInstance): Set<Id<ShipWeapon>> {
	return attacker.armaments.filterValues {
		isValidTarget(attacker, it, attacker.getWeaponPickRequest(it.weapon), target)
	}.keys
}

fun GameState.isValidTarget(ship: ShipInstance, weapon: ShipWeaponInstance, pickRequest: PickRequest, target: ShipInstance): Boolean {
	val targetPos = target.position.location
	
	return when (val weaponSpec = weapon.weapon) {
		is AreaWeapon ->
			target.owner.side != ship.owner.side && (targetPos - pickRequest.boundary.closestPointTo(targetPos)).length < weaponSpec.areaRadius
		else ->
			target.owner.side in (pickRequest.type as PickType.Ship).allowSides && isValidPick(pickRequest, PickResponse.Ship(target.id))
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

fun GameState.calculateAttackPhaseInitiative(): InitiativeMap =
	ships
		.values
		.groupBy { it.owner }
		.mapValues { (_, shipList) ->
			shipList
				.filter { !it.isDoneCurrentPhase }
				.sumOf { ship ->
					val allWeapons = ship.armaments
						.filterValues { weapon -> hasValidTargets(ship, weapon) }
					val usableWeapons = allWeapons - ship.usedArmaments
					
					val boardingPartyShot = (if (ship.canSendBoardingParty || ship.hasSentBoardingParty) 1 else 0)
					val usableBoardingPartyShot = (if (ship.canSendBoardingParty) 1 else 0)
					
					val disruptionPulseShot = (if (ship.canUseDisruptionPulse || ship.hasUsedDisruptionPulse) 1 else 0)
					val usableDisruptionPulseShot = (if (ship.canUseDisruptionPulse) 1 else 0)
					
					val allWeaponShots = allWeapons.values.sumOf { it.weapon.numShots * it.weapon.firingArcs.size } + boardingPartyShot + disruptionPulseShot
					val usableWeaponShots = usableWeapons.values.sumOf { it.weapon.numShots * it.weapon.firingArcs.size } + usableBoardingPartyShot + usableDisruptionPulseShot
					
					ship.ship.pointCost * (usableWeaponShots.toDouble() / allWeaponShots)
				}
		}

fun GameState.withRecalculatedInitiative(initiativeMapAccessor: GameState.() -> InitiativeMap): GameState {
	val initiativePair = initiativeMapAccessor()
	
	return copy(
		calculatedInitiative = (initiativePair - doneWithPhase).maxByOrNull { (_, it) -> it }?.key
	)
}

fun GameState.canShipMove(ship: Id<ShipInstance>): Boolean {
	val shipInstance = ships[ship] ?: return false
	return currentInitiative == shipInstance.owner
}

fun GameState.canShipAttack(ship: Id<ShipInstance>): Boolean {
	val shipInstance = ships[ship] ?: return false
	return currentInitiative == shipInstance.owner
}
