package net.starshipfights.data.admiralty

import net.starshipfights.game.Faction
import net.starshipfights.game.ShipType
import net.starshipfights.game.pointCost

val ShipType.buyPrice: Int
	get() = pointCost * 6 / 5

val ShipType.buyWhileDutchPrice: Int
	get() = pointCost * 8 / 5

val ShipType.sellPrice: Int
	get() = pointCost * 4 / 5

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
