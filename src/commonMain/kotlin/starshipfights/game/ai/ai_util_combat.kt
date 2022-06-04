package starshipfights.game.ai

import starshipfights.game.*

val combatTargetShipWeight by instinct(0.5..2.5)

val combatAvengeShipwrecks by instinct(0.5..4.5)
val combatAvengeShipWeight by instinct(-0.5..1.5)

val combatPrioritization by instinct(-1.5..2.5)

val combatAvengeAttacks by instinct(0.5..4.5)
val combatForgiveTarget by instinct(-1.5..2.5)
val combatPreyOnTheWeak by instinct(-1.5..2.5)

val combatFrustratedByFailedAttacks by instinct(-2.5..5.5)

fun ShipInstance.calculateSuffering(): Double {
	return (durability.maxHullPoints - hullAmount) + (if (ship.reactor is FelinaeShipReactor)
		0
	else powerMode.shields - shieldAmount) + (numFires * 0.5) + modulesStatus.statuses.values.sumOf { status ->
		when (status) {
			ShipModuleStatus.INTACT -> 0.0
			ShipModuleStatus.DAMAGED -> 0.75
			ShipModuleStatus.DESTROYED -> 1.5
			ShipModuleStatus.ABSENT -> 0.0
		}
	}
}

fun ShipInstance.expectedBoardingSuccess(against: ShipInstance): Double {
	return smoothNegative((assaultModifier - against.defenseModifier).toDouble())
}
