package starshipfights.data.admiralty

import starshipfights.game.ShipWeightClass

val ShipWeightClass.buyPrice: Int
	get() = basePointCost * 28 / 25

val ShipWeightClass.sellPrice: Int
	get() = basePointCost * 21 / 25
