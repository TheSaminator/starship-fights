package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id
import kotlin.math.abs
import kotlin.math.pow
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
	
	val felinaeShipPowerMode: FelinaeShipPowerMode = FelinaeShipPowerMode.INERTIALESS_DRIVE,
	val currentVelocity: Double = 0.0,
	val usedInertialessDriveShots: Int = 0,
	val usedDisruptionPulseShots: Int = 0,
	val hasUsedDisruptionPulse: Boolean = false,
	val recoalescenceMaxHullDamage: Int = 0,
	
	val armaments: ShipInstanceArmaments = ship.armaments.instantiate(),
	val usedArmaments: Set<Id<ShipWeapon>> = emptySet(),
	
	val fighterWings: Set<ShipHangarWing> = emptySet(),
	val bomberWings: Set<ShipHangarWing> = emptySet(),
) {
	val canUseShields: Boolean
		get() = ship.hasShields && modulesStatus[ShipModule.Shields].canBeUsed
	
	val canUseTurrets: Boolean
		get() = modulesStatus[ShipModule.Turrets].canBeUsed
	
	val canCatchFire: Boolean
		get() = ship.shipType.faction != Faction.FELINAE_FELICES
	
	val canUseInertialessDrive: Boolean
		get() = ship.canUseInertialessDrive && modulesStatus[ShipModule.Engines].canBeUsed && when (val movement = ship.movement) {
			is FelinaeShipMovement -> usedInertialessDriveShots < movement.inertialessDriveShots
			else -> false
		} && felinaeShipPowerMode == FelinaeShipPowerMode.INERTIALESS_DRIVE
	
	val remainingInertialessDriveJumps: Int
		get() = when (val movement = ship.movement) {
			is FelinaeShipMovement -> movement.inertialessDriveShots - usedInertialessDriveShots
			else -> 0
		}
	
	val canUseDisruptionPulse: Boolean
		get() = ship.canUseDisruptionPulse && modulesStatus[ShipModule.Turrets].canBeUsed && when (val durability = ship.durability) {
			is FelinaeShipDurability -> usedDisruptionPulseShots < durability.disruptionPulseShots
			else -> false
		} && felinaeShipPowerMode == FelinaeShipPowerMode.DISRUPTION_PULSE && !hasUsedDisruptionPulse
	
	val remainingDisruptionPulseEmissions: Int
		get() = when (val durability = ship.durability) {
			is FelinaeShipDurability -> durability.disruptionPulseShots - usedDisruptionPulseShots
			else -> 0
		}
	
	val canUseRecoalescence: Boolean
		get() = ship.canUseRecoalescence && felinaeShipPowerMode == FelinaeShipPowerMode.HULL_RECOALESCENSE && !isDoneCurrentPhase && hullAmount < durability.maxHullPoints && recoalescenceMaxHullDamage < (ship.durability.maxHullPoints - 1)
	
	fun canUseWeapon(weaponId: Id<ShipWeapon>): Boolean {
		if (weaponId in usedArmaments)
			return false
		
		if (!modulesStatus[ShipModule.Weapon(weaponId)].canBeUsed)
			return false
		
		val weapon = armaments.weaponInstances[weaponId] ?: return false
		
		return when (weapon) {
			is ShipWeaponInstance.Cannon -> weaponAmount > 0
			is ShipWeaponInstance.Lance -> weapon.numCharges > EPSILON
			is ShipWeaponInstance.Torpedo -> true
			is ShipWeaponInstance.Hangar -> weapon.wingHealth > 0.0
			is ShipWeaponInstance.ParticleClawLauncher -> true
			is ShipWeaponInstance.LightningYarn -> true
			is ShipWeaponInstance.MegaCannon -> weapon.remainingShots > 0
			is ShipWeaponInstance.RevelationGun -> weapon.remainingShots > 0
			is ShipWeaponInstance.EmpAntenna -> weapon.remainingShots > 0
		}
	}
	
	val remainingRepairTokens: Int
		get() = when (val durability = durability) {
			is StandardShipDurability -> durability.repairTokens - usedRepairTokens
			else -> 0
		}
	
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

@Serializable
enum class FelinaeShipPowerMode {
	INERTIALESS_DRIVE,
	DISRUPTION_PULSE,
	HULL_RECOALESCENSE;
	
	val displayName: String
		get() = when (this) {
			INERTIALESS_DRIVE -> "Inertialess Drive"
			DISRUPTION_PULSE -> "Disruption Pulse"
			HULL_RECOALESCENSE -> "Hull Recoalescence"
		}
}

fun ShipInstance.remainingGridEfficiency(newPowerMode: ShipPowerMode) = when (val reactor = ship.reactor) {
	is StandardShipReactor -> (reactor.gridEfficiency * 2 - (newPowerMode distanceTo powerMode)) / 2
	else -> 0
}

fun ShipInstance.validatePowerMode(newPowerMode: ShipPowerMode) = when (val reactor = ship.reactor) {
	is StandardShipReactor -> newPowerMode.total == reactor.powerOutput && ShipSubsystem.values().none { newPowerMode[it] < 0 } && (newPowerMode distanceTo powerMode) <= reactor.gridEfficiency * 2
	else -> true
}

val ShipInstance.movementCoefficient: Double
	get() = when (val reactor = ship.reactor) {
		is StandardShipReactor -> sqrt(powerMode.engines.toDouble() / reactor.subsystemAmount)
		else -> 1.0
	} * if (modulesStatus[ShipModule.Engines].canBeUsed)
		1.0
	else if (ship.movement is FelinaeShipMovement)
		0.75
	else
		0.5

val ShipInstance.movement: ShipMovement
	get() = when (val m = ship.movement) {
		is StandardShipMovement -> {
			val coefficient = movementCoefficient
			with(m) {
				copy(turnAngle = turnAngle * coefficient, moveSpeed = moveSpeed * coefficient)
			}
		}
		is FelinaeShipMovement -> {
			val coefficient = movementCoefficient
			with(m) {
				copy(
					turnAngle = turnAngle * coefficient,
					moveSpeed = moveSpeed * coefficient,
					inertialessDriveRange = inertialessDriveRange * coefficient.pow(2)
				)
			}
		}
	}

val ShipInstance.durability: ShipDurability
	get() = when (val d = ship.durability) {
		is FelinaeShipDurability -> d.copy(
			maxHullPoints = d.maxHullPoints - recoalescenceMaxHullDamage,
		)
		is StandardShipDurability -> d.copy(
			turretDefense = if (canUseTurrets) d.turretDefense else 0.0
		)
	}

val ShipInstance.firepower: ShipFirepower
	get() = ship.firepower

fun Ship.defaultPowerMode(): ShipPowerMode {
	val amount = when (val r = reactor) {
		is StandardShipReactor -> r.subsystemAmount
		else -> 0
	}
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
