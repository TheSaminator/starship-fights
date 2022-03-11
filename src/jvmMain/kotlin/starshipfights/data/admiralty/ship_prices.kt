package starshipfights.data.admiralty

import starshipfights.game.ShipType
import starshipfights.game.pointCost

val ShipType.buyPrice: Int
	get() = pointCost * 28 / 25

val ShipType.sellPrice: Int
	get() = pointCost * 21 / 25
