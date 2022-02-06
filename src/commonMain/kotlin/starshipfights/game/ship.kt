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
		get() = shipType.weightClass.basePointCost
	
	val reactor: ShipReactor
		get() = shipType.weightClass.reactor
	
	val movement: ShipMovement
		get() = shipType.weightClass.movement
	
	val durability: ShipDurability
		get() = shipType.weightClass.durability
	
	val armaments: ShipArmaments
		get() = shipType.armaments
}

@Serializable
data class ShipReactor(
	val powerOutput: Int,
	val gridEfficiency: Int
) {
	val subsystemAmount: Int
		get() = powerOutput / 4
}

val ShipWeightClass.reactor: ShipReactor
	get() = when (this) {
		ShipWeightClass.ESCORT -> ShipReactor(8, 1)
		ShipWeightClass.DESTROYER -> ShipReactor(12, 2)
		ShipWeightClass.CRUISER -> ShipReactor(16, 3)
		ShipWeightClass.BATTLECRUISER -> ShipReactor(16, 4)
		ShipWeightClass.BATTLESHIP -> ShipReactor(20, 4)
		
		ShipWeightClass.GRAND_CRUISER -> ShipReactor(20, 3)
		ShipWeightClass.COLOSSUS -> ShipReactor(36, 7)
		
		ShipWeightClass.HEAVY_CRUISER -> ShipReactor(24, 3)
		
		ShipWeightClass.FRIGATE -> ShipReactor(12, 1)
		ShipWeightClass.LINE_SHIP -> ShipReactor(20, 3)
		ShipWeightClass.DREADNOUGHT -> ShipReactor(28, 5)
	}

@Serializable
data class ShipMovement(
	val turnAngle: Double,
	val moveSpeed: Double,
)

val ShipWeightClass.movement: ShipMovement
	get() = when (this) {
		ShipWeightClass.ESCORT -> ShipMovement(PI / 2, 800.0)
		ShipWeightClass.DESTROYER -> ShipMovement(PI / 2, 700.0)
		ShipWeightClass.CRUISER -> ShipMovement(PI / 3, 600.0)
		ShipWeightClass.BATTLECRUISER -> ShipMovement(PI / 3, 600.0)
		ShipWeightClass.BATTLESHIP -> ShipMovement(PI / 4, 500.0)
		
		ShipWeightClass.GRAND_CRUISER -> ShipMovement(PI / 4, 600.0)
		ShipWeightClass.COLOSSUS -> ShipMovement(PI / 6, 400.0)
		
		ShipWeightClass.HEAVY_CRUISER -> ShipMovement(PI / 3, 500.0)
		
		ShipWeightClass.FRIGATE -> ShipMovement(PI * 2 / 3, 1000.0)
		ShipWeightClass.LINE_SHIP -> ShipMovement(PI / 2, 800.0)
		ShipWeightClass.DREADNOUGHT -> ShipMovement(PI / 3, 600.0)
	}

@Serializable
data class ShipDurability(
	val maxHullPoints: Int,
)

val ShipWeightClass.durability: ShipDurability
	get() = when (this) {
		ShipWeightClass.ESCORT -> ShipDurability(2)
		ShipWeightClass.DESTROYER -> ShipDurability(4)
		ShipWeightClass.CRUISER -> ShipDurability(6)
		ShipWeightClass.BATTLECRUISER -> ShipDurability(7)
		ShipWeightClass.BATTLESHIP -> ShipDurability(9)
		
		ShipWeightClass.GRAND_CRUISER -> ShipDurability(8)
		ShipWeightClass.COLOSSUS -> ShipDurability(13)
		
		ShipWeightClass.HEAVY_CRUISER -> ShipDurability(8)
		
		ShipWeightClass.FRIGATE -> ShipDurability(4)
		ShipWeightClass.LINE_SHIP -> ShipDurability(7)
		ShipWeightClass.DREADNOUGHT -> ShipDurability(10)
	}
