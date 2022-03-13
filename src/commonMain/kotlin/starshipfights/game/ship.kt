package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id
import kotlin.math.PI

@Serializable
data class Ship(
	val id: Id<Ship>,
	
	val name: String,
	val shipType: ShipType
) {
	val fullName: String
		get() = "${shipType.faction.shipPrefix}$name"
	
	val pointCost: Int
		get() = shipType.pointCost
	
	val reactor: ShipReactor
		get() = shipType.weightClass.reactor
	
	val movement: ShipMovement
		get() = shipType.weightClass.movement
	
	val durability: ShipDurability
		get() = shipType.weightClass.durability
	
	val firepower: ShipFirepower
		get() = shipType.weightClass.firepower
	
	val armaments: ShipArmaments
		get() = shipType.armaments
}

@Serializable
data class ShipReactor(
	val subsystemAmount: Int,
	val gridEfficiency: Int
) {
	val powerOutput: Int
		get() = subsystemAmount * 3
}

val ShipWeightClass.reactor: ShipReactor
	get() = when (this) {
		ShipWeightClass.ESCORT -> ShipReactor(2, 1)
		ShipWeightClass.DESTROYER -> ShipReactor(3, 1)
		ShipWeightClass.CRUISER -> ShipReactor(4, 2)
		ShipWeightClass.BATTLECRUISER -> ShipReactor(6, 3)
		ShipWeightClass.BATTLESHIP -> ShipReactor(7, 4)
		
		ShipWeightClass.GRAND_CRUISER -> ShipReactor(6, 4)
		ShipWeightClass.COLOSSUS -> ShipReactor(9, 6)
		
		ShipWeightClass.AUXILIARY_SHIP -> ShipReactor(2, 1)
		ShipWeightClass.LIGHT_CRUISER -> ShipReactor(3, 1)
		ShipWeightClass.MEDIUM_CRUISER -> ShipReactor(4, 2)
		ShipWeightClass.HEAVY_CRUISER -> ShipReactor(6, 3)
		
		ShipWeightClass.FRIGATE -> ShipReactor(4, 1)
		ShipWeightClass.LINE_SHIP -> ShipReactor(6, 3)
		ShipWeightClass.DREADNOUGHT -> ShipReactor(8, 5)
	}

@Serializable
data class ShipMovement(
	val turnAngle: Double,
	val moveSpeed: Double,
)

val ShipWeightClass.movement: ShipMovement
	get() = when (this) {
		ShipWeightClass.ESCORT -> ShipMovement(PI / 2, 2500.0)
		ShipWeightClass.DESTROYER -> ShipMovement(PI / 2, 2200.0)
		ShipWeightClass.CRUISER -> ShipMovement(PI / 3, 1900.0)
		ShipWeightClass.BATTLECRUISER -> ShipMovement(PI / 3, 1900.0)
		ShipWeightClass.BATTLESHIP -> ShipMovement(PI / 4, 1600.0)
		
		ShipWeightClass.GRAND_CRUISER -> ShipMovement(PI / 4, 1750.0)
		ShipWeightClass.COLOSSUS -> ShipMovement(PI / 6, 1300.0)
		
		ShipWeightClass.AUXILIARY_SHIP -> ShipMovement(PI / 2, 2500.0)
		ShipWeightClass.LIGHT_CRUISER -> ShipMovement(PI / 2, 2250.0)
		ShipWeightClass.MEDIUM_CRUISER -> ShipMovement(PI / 3, 2000.0)
		ShipWeightClass.HEAVY_CRUISER -> ShipMovement(PI / 3, 1750.0)
		
		ShipWeightClass.FRIGATE -> ShipMovement(PI * 2 / 3, 2750.0)
		ShipWeightClass.LINE_SHIP -> ShipMovement(PI / 2, 2250.0)
		ShipWeightClass.DREADNOUGHT -> ShipMovement(PI / 3, 1750.0)
	}

@Serializable
data class ShipDurability(
	val maxHullPoints: Int,
	val turretDefense: Double,
	val repairTokens: Int,
)

val ShipWeightClass.durability: ShipDurability
	get() = when (this) {
		ShipWeightClass.ESCORT -> ShipDurability(4, 0.5, 1)
		ShipWeightClass.DESTROYER -> ShipDurability(8, 0.5, 1)
		ShipWeightClass.CRUISER -> ShipDurability(12, 1.0, 2)
		ShipWeightClass.BATTLECRUISER -> ShipDurability(14, 1.5, 2)
		ShipWeightClass.BATTLESHIP -> ShipDurability(16, 2.0, 3)
		
		ShipWeightClass.GRAND_CRUISER -> ShipDurability(15, 1.75, 3)
		ShipWeightClass.COLOSSUS -> ShipDurability(27, 3.0, 4)
		
		ShipWeightClass.AUXILIARY_SHIP -> ShipDurability(4, 2.0, 1)
		ShipWeightClass.LIGHT_CRUISER -> ShipDurability(8, 3.0, 2)
		ShipWeightClass.MEDIUM_CRUISER -> ShipDurability(12, 3.5, 2)
		ShipWeightClass.HEAVY_CRUISER -> ShipDurability(16, 4.0, 3)
		
		ShipWeightClass.FRIGATE -> ShipDurability(10, 1.5, 1)
		ShipWeightClass.LINE_SHIP -> ShipDurability(15, 2.0, 1)
		ShipWeightClass.DREADNOUGHT -> ShipDurability(20, 2.5, 1)
	}

@Serializable
data class ShipFirepower(
	val rangeMultiplier: Double,
	val criticalChance: Double,
	val cannonAccuracy: Double,
	val lanceCharging: Double,
)

val ShipWeightClass.firepower: ShipFirepower
	get() = when (this) {
		ShipWeightClass.ESCORT -> ShipFirepower(0.75, 0.75, 0.875, 0.875)
		ShipWeightClass.DESTROYER -> ShipFirepower(0.75, 0.75, 1.0, 1.0)
		ShipWeightClass.CRUISER -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		ShipWeightClass.BATTLECRUISER -> ShipFirepower(1.25, 1.25, 1.25, 1.25)
		ShipWeightClass.BATTLESHIP -> ShipFirepower(1.25, 1.25, 1.25, 1.25)
		
		ShipWeightClass.GRAND_CRUISER -> ShipFirepower(1.25, 1.25, 1.25, 1.25)
		ShipWeightClass.COLOSSUS -> ShipFirepower(1.5, 1.5, 1.5, 1.5)
		
		ShipWeightClass.AUXILIARY_SHIP -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		ShipWeightClass.LIGHT_CRUISER -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		ShipWeightClass.MEDIUM_CRUISER -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		ShipWeightClass.HEAVY_CRUISER -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		
		ShipWeightClass.FRIGATE -> ShipFirepower(0.8, 0.8, 1.0, 1.0)
		ShipWeightClass.LINE_SHIP -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		ShipWeightClass.DREADNOUGHT -> ShipFirepower(1.2, 1.2, 1.0, 1.0)
	}
