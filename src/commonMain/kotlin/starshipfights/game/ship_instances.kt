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
	
	val armaments: ShipInstanceArmaments = ship.armaments.instantiate(),
	val usedArmaments: Set<Id<ShipWeapon>> = emptySet(),
	
	val fighterWings: List<ShipHangarWing> = emptyList(),
	val bomberWings: List<ShipHangarWing> = emptyList(),
) {
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
	val currentLocation: Position,
	val previousLocation: Position,
	val facingAngle: Double
) {
	val currentVelocity: Distance
		get() = currentLocation - previousLocation
	
	val drift: ShipPosition
		get() = copy(
			currentLocation = currentLocation + currentVelocity,
			previousLocation = currentLocation
		)
}

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

val ShipInstance.movement: ShipMovement
	get() {
		val coefficient = sqrt(powerMode.engines.toDouble() / ship.reactor.subsystemAmount)
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
