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
		ShipWeightClass.BATTLECRUISER -> ShipReactor(5, 3)
		ShipWeightClass.BATTLESHIP -> ShipReactor(7, 4)
		
		ShipWeightClass.GRAND_CRUISER -> ShipReactor(6, 4)
		ShipWeightClass.COLOSSUS -> ShipReactor(9, 6)
		
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
