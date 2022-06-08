package net.starshipfights.game

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id
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
	
	val hasShields: Boolean
		get() = shipType.faction != Faction.FELINAE_FELICES
	
	val canUseInertialessDrive: Boolean
		get() = shipType.faction == Faction.FELINAE_FELICES
	
	val canUseDisruptionPulse: Boolean
		get() = shipType.faction == Faction.FELINAE_FELICES
	
	val canUseRecoalescence: Boolean
		get() = shipType.faction == Faction.FELINAE_FELICES
}

@Serializable
sealed class ShipReactor

@Serializable
data class StandardShipReactor(
	val subsystemAmount: Int,
	val gridEfficiency: Int
) : ShipReactor() {
	val powerOutput: Int
		get() = subsystemAmount * 3
}

@Serializable
object FelinaeShipReactor : ShipReactor()

val ShipWeightClass.reactor: ShipReactor
	get() = when (this) {
		ShipWeightClass.ESCORT -> StandardShipReactor(2, 1)
		ShipWeightClass.DESTROYER -> StandardShipReactor(3, 1)
		ShipWeightClass.CRUISER -> StandardShipReactor(4, 2)
		ShipWeightClass.BATTLECRUISER -> StandardShipReactor(6, 3)
		ShipWeightClass.BATTLESHIP -> StandardShipReactor(7, 4)
		
		ShipWeightClass.BATTLE_BARGE -> StandardShipReactor(5, 3)
		
		ShipWeightClass.GRAND_CRUISER -> StandardShipReactor(6, 4)
		ShipWeightClass.COLOSSUS -> StandardShipReactor(9, 6)
		
		ShipWeightClass.FF_ESCORT -> FelinaeShipReactor
		ShipWeightClass.FF_DESTROYER -> FelinaeShipReactor
		ShipWeightClass.FF_CRUISER -> FelinaeShipReactor
		ShipWeightClass.FF_BATTLECRUISER -> FelinaeShipReactor
		ShipWeightClass.FF_BATTLESHIP -> FelinaeShipReactor
		
		ShipWeightClass.AUXILIARY_SHIP -> StandardShipReactor(2, 1)
		ShipWeightClass.LIGHT_CRUISER -> StandardShipReactor(3, 1)
		ShipWeightClass.MEDIUM_CRUISER -> StandardShipReactor(4, 2)
		ShipWeightClass.HEAVY_CRUISER -> StandardShipReactor(6, 3)
		
		ShipWeightClass.FRIGATE -> StandardShipReactor(4, 1)
		ShipWeightClass.LINE_SHIP -> StandardShipReactor(6, 3)
		ShipWeightClass.DREADNOUGHT -> StandardShipReactor(8, 5)
	}

@Serializable
sealed class ShipMovement {
	abstract val turnAngle: Double
	abstract val moveSpeed: Double
}

@Serializable
data class StandardShipMovement(
	override val turnAngle: Double,
	override val moveSpeed: Double,
) : ShipMovement()

@Serializable
data class FelinaeShipMovement(
	override val turnAngle: Double,
	override val moveSpeed: Double,
	val inertialessDriveRange: Double,
	val inertialessDriveShots: Int
) : ShipMovement()

val ShipWeightClass.movement: ShipMovement
	get() = when (this) {
		ShipWeightClass.ESCORT -> StandardShipMovement(PI / 2, 2500.0)
		ShipWeightClass.DESTROYER -> StandardShipMovement(PI / 2, 2200.0)
		ShipWeightClass.CRUISER -> StandardShipMovement(PI / 3, 1900.0)
		ShipWeightClass.BATTLECRUISER -> StandardShipMovement(PI / 3, 1900.0)
		ShipWeightClass.BATTLESHIP -> StandardShipMovement(PI / 4, 1600.0)
		
		ShipWeightClass.BATTLE_BARGE -> StandardShipMovement(PI / 4, 1600.0)
		
		ShipWeightClass.GRAND_CRUISER -> StandardShipMovement(PI / 4, 1750.0)
		ShipWeightClass.COLOSSUS -> StandardShipMovement(PI / 6, 1300.0)
		
		ShipWeightClass.FF_ESCORT -> FelinaeShipMovement(PI / 3, 1600.0, 4000.0, 1)
		ShipWeightClass.FF_DESTROYER -> FelinaeShipMovement(PI / 4, 1400.0, 3750.0, 1)
		ShipWeightClass.FF_CRUISER -> FelinaeShipMovement(PI / 6, 1200.0, 3250.0, 1)
		ShipWeightClass.FF_BATTLECRUISER -> FelinaeShipMovement(PI / 6, 1200.0, 3000.0, 2)
		ShipWeightClass.FF_BATTLESHIP -> FelinaeShipMovement(PI / 8, 800.0, 2500.0, 2)
		
		ShipWeightClass.AUXILIARY_SHIP -> StandardShipMovement(PI / 2, 2500.0)
		ShipWeightClass.LIGHT_CRUISER -> StandardShipMovement(PI / 2, 2250.0)
		ShipWeightClass.MEDIUM_CRUISER -> StandardShipMovement(PI / 3, 2000.0)
		ShipWeightClass.HEAVY_CRUISER -> StandardShipMovement(PI / 3, 1750.0)
		
		ShipWeightClass.FRIGATE -> StandardShipMovement(PI * 2 / 3, 2750.0)
		ShipWeightClass.LINE_SHIP -> StandardShipMovement(PI / 2, 2250.0)
		ShipWeightClass.DREADNOUGHT -> StandardShipMovement(PI / 3, 1750.0)
	}

@Serializable
sealed class ShipDurability {
	abstract val maxHullPoints: Int
	abstract val turretDefense: Double
	abstract val troopsDefense: Int
}

@Serializable
data class StandardShipDurability(
	override val maxHullPoints: Int,
	override val turretDefense: Double,
	override val troopsDefense: Int,
	val repairTokens: Int,
) : ShipDurability()

@Serializable
data class FelinaeShipDurability(
	override val maxHullPoints: Int,
	override val troopsDefense: Int,
	val disruptionPulseRange: Double,
	val disruptionPulseShots: Int
) : ShipDurability() {
	override val turretDefense: Double
		get() = 0.0
}

val ShipWeightClass.durability: ShipDurability
	get() = when (this) {
		ShipWeightClass.ESCORT -> StandardShipDurability(4, 0.5, 5, 1)
		ShipWeightClass.DESTROYER -> StandardShipDurability(8, 0.5, 7, 1)
		ShipWeightClass.CRUISER -> StandardShipDurability(12, 1.0, 10, 2)
		ShipWeightClass.BATTLECRUISER -> StandardShipDurability(14, 1.5, 10, 2)
		ShipWeightClass.BATTLESHIP -> StandardShipDurability(16, 2.0, 15, 3)
		
		ShipWeightClass.BATTLE_BARGE -> StandardShipDurability(16, 1.5, 15, 3)
		
		ShipWeightClass.GRAND_CRUISER -> StandardShipDurability(15, 1.75, 12, 3)
		ShipWeightClass.COLOSSUS -> StandardShipDurability(27, 3.0, 25, 4)
		
		ShipWeightClass.FF_ESCORT -> FelinaeShipDurability(6, 3, 1000.0, 3)
		ShipWeightClass.FF_DESTROYER -> FelinaeShipDurability(9, 4, 1000.0, 4)
		ShipWeightClass.FF_CRUISER -> FelinaeShipDurability(12, 5, 750.0, 2)
		ShipWeightClass.FF_BATTLECRUISER -> FelinaeShipDurability(15, 6, 875.0, 2)
		ShipWeightClass.FF_BATTLESHIP -> FelinaeShipDurability(18, 7, 1250.0, 3)
		
		ShipWeightClass.AUXILIARY_SHIP -> StandardShipDurability(4, 2.0, 6, 1)
		ShipWeightClass.LIGHT_CRUISER -> StandardShipDurability(8, 3.0, 9, 2)
		ShipWeightClass.MEDIUM_CRUISER -> StandardShipDurability(12, 3.5, 12, 2)
		ShipWeightClass.HEAVY_CRUISER -> StandardShipDurability(16, 4.0, 15, 3)
		
		ShipWeightClass.FRIGATE -> StandardShipDurability(10, 1.5, 7, 1)
		ShipWeightClass.LINE_SHIP -> StandardShipDurability(15, 2.0, 9, 1)
		ShipWeightClass.DREADNOUGHT -> StandardShipDurability(20, 2.5, 11, 1)
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
		
		ShipWeightClass.BATTLE_BARGE -> ShipFirepower(1.25, 1.25, 1.25, 1.25)
		
		ShipWeightClass.GRAND_CRUISER -> ShipFirepower(1.25, 1.25, 1.25, 1.25)
		ShipWeightClass.COLOSSUS -> ShipFirepower(1.5, 1.5, 1.5, 1.5)
		
		ShipWeightClass.FF_ESCORT -> ShipFirepower(1.0, 0.6, 0.5, -1.0)
		ShipWeightClass.FF_DESTROYER -> ShipFirepower(1.0, 0.8, 0.625, -1.0)
		ShipWeightClass.FF_CRUISER -> ShipFirepower(1.0, 1.0, 0.75, -1.0)
		ShipWeightClass.FF_BATTLECRUISER -> ShipFirepower(1.0, 1.0, 0.875, -1.0)
		ShipWeightClass.FF_BATTLESHIP -> ShipFirepower(1.5, 1.2, 1.0, -1.0)
		
		ShipWeightClass.AUXILIARY_SHIP -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		ShipWeightClass.LIGHT_CRUISER -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		ShipWeightClass.MEDIUM_CRUISER -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		ShipWeightClass.HEAVY_CRUISER -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		
		ShipWeightClass.FRIGATE -> ShipFirepower(0.8, 0.8, 1.0, 1.0)
		ShipWeightClass.LINE_SHIP -> ShipFirepower(1.0, 1.0, 1.0, 1.0)
		ShipWeightClass.DREADNOUGHT -> ShipFirepower(1.2, 1.2, 1.0, 1.0)
	}
