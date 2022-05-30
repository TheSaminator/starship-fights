package starshipfights.data.admiralty

import starshipfights.game.Faction
import starshipfights.game.ShipType
import starshipfights.game.pointCost

val ShipType.buyPrice: Int
	get() = pointCost * 28 / 25

val ShipType.buyWhileDutchPrice: Int
	get() = pointCost * 36 / 25

val ShipType.sellPrice: Int
	get() = pointCost * 21 / 25

fun ShipType.buyPriceChecked(admiral: Admiral, ownedShips: List<ShipInDrydock>): Int? {
	return buyPrice(admiral, ownedShips)?.takeIf { it <= admiral.money }
}

fun ShipType.buyPrice(admiral: Admiral, ownedShips: List<ShipInDrydock>): Int? {
	if (weightClass.tier > admiral.rank.maxShipWeightClass.tier) return null
	if (weightClass.isUnique && ownedShips.any { it.shipType.weightClass == weightClass }) return null
	return when {
		admiral.faction == faction -> buyPrice
		admiral.faction == Faction.NDRC && !weightClass.isUnique -> buyWhileDutchPrice
		else -> null
	}
}
