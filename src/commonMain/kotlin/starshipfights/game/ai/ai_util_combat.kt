package starshipfights.game.ai

import starshipfights.game.FelinaeShipReactor
import starshipfights.game.ShipInstance
import starshipfights.game.ShipModuleStatus
import starshipfights.game.durability
import kotlin.random.Random

val combatTargetShipWeight by instinct { Random.nextDouble(0.5, 2.5) }

val combatAvengeShipwrecks by instinct { Random.nextDouble(0.5, 4.5) }
val combatAvengeShipWeight by instinct { Random.nextDouble(-0.5, 1.5) }

val combatPrioritization by instinct { Random.nextDouble(-1.0, 1.0) }

val combatAvengeAttacks by instinct { Random.nextDouble(0.5, 4.5) }
val combatForgiveTarget by instinct { Random.nextDouble(-1.5, 2.5) }

val combatFrustratedByFailedAttacks by instinct { Random.nextDouble(-2.5, 5.5) }

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
