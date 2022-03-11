package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id
import kotlin.math.abs
import kotlin.math.sqrt

@Serializable
data class ShipInstance(
	val ship: Ship,
	val owner: GlobalSide,
	val position: ShipPosition,
	
	val isIdentified: Boolean = false,
	
	val isDoneCurrentPhase: Boolean = true,
	
	val powerMode: ShipPowerMode = ship.defaultPowerMode(),
	
	val weaponAmount: Int = powerMode.weapons,
	val shieldAmount: Int = powerMode.shields,
	val hullAmount: Int = ship.durability.maxHullPoints,
	
	val modulesStatus: ShipModulesStatus = ShipModulesStatus.forShip(ship),
	val numFires: Int = 0,
	val usedRepairTokens: Int = 0,
	
	val armaments: ShipInstanceArmaments = ship.armaments.instantiate(),
	val usedArmaments: Set<Id<ShipWeapon>> = emptySet(),
	
	val fighterWings: Set<ShipHangarWing> = emptySet(),
	val bomberWings: Set<ShipHangarWing> = emptySet(),
) {
	val canUseShields: Boolean
		get() = modulesStatus[ShipModule.Shields].canBeUsed
	
	val canUseTurrets: Boolean
		get() = modulesStatus[ShipModule.Turrets].canBeUsed
	
	fun canUseWeapon(weaponId: Id<ShipWeapon>): Boolean {
		if (weaponId in usedArmaments)
			return false
		
		if (!modulesStatus[ShipModule.Weapon(weaponId)].canBeUsed)
			return false
		
		val weapon = armaments.weaponInstances[weaponId] ?: return false
		
		return when (weapon) {
			is ShipWeaponInstance.Cannon -> weaponAmount > 0
			is ShipWeaponInstance.Hangar -> weapon.wingHealth > 0.0
			is ShipWeaponInstance.Lance -> weapon.numCharges > 0
			is ShipWeaponInstance.Torpedo -> true
			is ShipWeaponInstance.MegaCannon -> weapon.remainingShots > 0
			is ShipWeaponInstance.RevelationGun -> weapon.remainingShots > 0
			is ShipWeaponInstance.EmpAntenna -> weapon.remainingShots > 0
		}
	}
	
	val remainingRepairTokens: Int
		get() = ship.durability.repairTokens - usedRepairTokens
	
	val id: Id<ShipInstance>
		get() = ship.id.reinterpret()
}

@Serializable
data class ShipWreck(
	val ship: Ship,
	val owner: GlobalSide,
	val isEscape: Boolean = false,
) {
	val id: Id<ShipInstance>
		get() = ship.id.reinterpret()
}

@Serializable
data class ShipPosition(
	val location: Position,
	val facing: Double
)

enum class ShipSubsystem {
	WEAPONS, SHIELDS, ENGINES;
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
	
	val htmlColor: String
		get() = when (this) {
			WEAPONS -> "#FF6633"
			SHIELDS -> "#6699FF"
			ENGINES -> "#FFCC33"
		}
	
	val imageUrl: String
		get() = "/static/game/images/subsystem-${name.lowercase()}.svg"
	
	companion object {
		val transferImageUrl: String
			get() = "/static/game/images/subsystems-power-transfer.svg"
	}
}

@Serializable
data class ShipPowerMode(
	val weapons: Int,
	val shields: Int,
	val engines: Int,
) {
	operator fun plus(delta: Map<ShipSubsystem, Int>) = copy(
		weapons = weapons + (delta[ShipSubsystem.WEAPONS] ?: 0),
		shields = shields + (delta[ShipSubsystem.SHIELDS] ?: 0),
		engines = engines + (delta[ShipSubsystem.ENGINES] ?: 0),
	)
	
	operator fun minus(delta: Map<ShipSubsystem, Int>) = this + delta.mapValues { (_, d) -> -d }
	
	operator fun get(key: ShipSubsystem): Int = when (key) {
		ShipSubsystem.WEAPONS -> weapons
		ShipSubsystem.SHIELDS -> shields
		ShipSubsystem.ENGINES -> engines
	}
	
	val total: Int
		get() = weapons + shields + engines
	
	infix fun distanceTo(other: ShipPowerMode) = ShipSubsystem.values().sumOf { subsystem -> abs(this[subsystem] - other[subsystem]) }
}

fun ShipInstance.remainingGridEfficiency(newPowerMode: ShipPowerMode) = (ship.reactor.gridEfficiency * 2 - (newPowerMode distanceTo powerMode)) / 2
fun ShipInstance.validatePowerMode(newPowerMode: ShipPowerMode) = newPowerMode.total == ship.reactor.powerOutput && ShipSubsystem.values().none { newPowerMode[it] < 0 } && (newPowerMode distanceTo powerMode) <= ship.reactor.gridEfficiency * 2

val ShipInstance.movementCoefficient: Double
	get() = sqrt(powerMode.engines.toDouble() / ship.reactor.subsystemAmount) *
			if (modulesStatus[ShipModule.Engines].canBeUsed)
				1.0
			else
				0.5

val ShipInstance.movement: ShipMovement
	get() {
		val coefficient = movementCoefficient
		return with(ship.movement) {
			copy(turnAngle = turnAngle * coefficient, moveSpeed = moveSpeed * coefficient)
		}
	}

fun Ship.defaultPowerMode(): ShipPowerMode {
	val amount = reactor.subsystemAmount
	return ShipPowerMode(amount, amount, amount)
}

enum class ShipRenderMode {
	NONE,
	SIGNAL,
	FULL;
}

fun GameState.renderShipAs(ship: ShipInstance, forPlayer: GlobalSide) = if (ship.owner == forPlayer)
	ShipRenderMode.FULL
else if (phase == GamePhase.Deploy)
	ShipRenderMode.NONE
else if (ship.isIdentified)
	ShipRenderMode.FULL
else
	ShipRenderMode.SIGNAL

const val SHIP_BASE_SIZE = 250.0

const val SHIP_TORPEDO_RANGE = 2_000.0
const val SHIP_CANNON_RANGE = 2_500.0
const val SHIP_LANCE_RANGE = 3_000.0
const val SHIP_HANGAR_RANGE = 3_500.0

const val SHIP_SENSOR_RANGE = 4_000.0
