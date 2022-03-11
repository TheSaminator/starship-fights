package starshipfights.game

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import starshipfights.data.Id
import kotlin.random.Random
import kotlin.random.nextInt

@Serializable
sealed class ShipModule {
	abstract fun getDisplayName(ship: Ship): String
	
	@Serializable
	data class Weapon(val weaponId: Id<ShipWeapon>) : ShipModule() {
		override fun getDisplayName(ship: Ship): String {
			return ship.armaments.weapons[weaponId]?.displayName ?: ""
		}
	}
	
	@Serializable
	object Shields : ShipModule() {
		override fun getDisplayName(ship: Ship): String {
			return "Shield Generators"
		}
	}
	
	@Serializable
	object Engines : ShipModule() {
		override fun getDisplayName(ship: Ship): String {
			return "Mach-Effect Thrusters"
		}
	}
	
	@Serializable
	object Turrets : ShipModule() {
		override fun getDisplayName(ship: Ship): String {
			return "Point Defense Turrets"
		}
	}
}

@Serializable
enum class ShipModuleStatus(val canBeUsed: Boolean, val canBeRepaired: Boolean) {
	INTACT(true, false),
	DAMAGED(false, true),
	DESTROYED(false, false)
}

@Serializable(with = ShipModulesStatusSerializer::class)
data class ShipModulesStatus(val statuses: Map<ShipModule, ShipModuleStatus>) {
	operator fun get(module: ShipModule) = statuses[module] ?: ShipModuleStatus.INTACT
	
	fun repair(module: ShipModule) = ShipModulesStatus(
		statuses + if (this[module].canBeRepaired)
			mapOf(module to ShipModuleStatus.INTACT)
		else emptyMap()
	)
	
	fun damage(module: ShipModule) = ShipModulesStatus(
		statuses + mapOf(
			module to when (this[module]) {
				ShipModuleStatus.INTACT -> ShipModuleStatus.DAMAGED
				ShipModuleStatus.DAMAGED -> ShipModuleStatus.DESTROYED
				ShipModuleStatus.DESTROYED -> ShipModuleStatus.DESTROYED
			}
		)
	)
	
	fun damageMany(modules: Iterable<ShipModule>) = ShipModulesStatus(
		statuses + modules.associateWith { module ->
			when (this[module]) {
				ShipModuleStatus.INTACT -> ShipModuleStatus.DAMAGED
				ShipModuleStatus.DAMAGED -> ShipModuleStatus.DESTROYED
				ShipModuleStatus.DESTROYED -> ShipModuleStatus.DESTROYED
			}
		}
	)
	
	companion object {
		fun forShip(ship: Ship) = ShipModulesStatus(
			mapOf(
				ShipModule.Shields to ShipModuleStatus.INTACT,
				ShipModule.Engines to ShipModuleStatus.INTACT,
				ShipModule.Turrets to ShipModuleStatus.INTACT,
			) + ship.armaments.weapons.keys.associate {
				ShipModule.Weapon(it) to ShipModuleStatus.INTACT
			}
		)
	}
}

object ShipModulesStatusSerializer : KSerializer<ShipModulesStatus> {
	private val inner = ListSerializer(PairSerializer(ShipModule.serializer(), ShipModuleStatus.serializer()))
	
	override val descriptor: SerialDescriptor
		get() = inner.descriptor
	
	override fun serialize(encoder: Encoder, value: ShipModulesStatus) {
		inner.serialize(encoder, value.statuses.toList())
	}
	
	override fun deserialize(decoder: Decoder): ShipModulesStatus {
		return ShipModulesStatus(inner.deserialize(decoder).toMap())
	}
}

sealed class CritResult {
	object NoEffect : CritResult()
	data class FireStarted(val ship: ShipInstance) : CritResult()
	data class ModulesDisabled(val ship: ShipInstance, val modules: Set<ShipModule>) : CritResult()
	data class HullDamaged(val ship: ShipInstance, val amount: Int) : CritResult()
	data class Destroyed(val ship: ShipWreck) : CritResult()
	
	companion object {
		fun fromImpactResult(impactResult: ImpactResult) = when (impactResult) {
			is ImpactResult.Damaged -> impactResult.amount?.let { HullDamaged(impactResult.ship, it) } ?: NoEffect
			is ImpactResult.Destroyed -> Destroyed(impactResult.ship)
		}
	}
}

fun ShipInstance.doCriticalDamage(): CritResult {
	return when (Random.nextInt(0..4) + Random.nextInt(0..4)) { // Ranges in 0..8, probability density peaks at 4
		0 -> {
			// Damage 3 weapons
			val modulesDamaged = armaments.weaponInstances.keys.shuffled().take(3).map { ShipModule.Weapon(it) }
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		1 -> {
			// Damage 1 weapon
			val modulesDamaged = armaments.weaponInstances.keys.shuffled().take(1).map { ShipModule.Weapon(it) }
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		2 -> {
			// Damage 2 weapons
			val modulesDamaged = armaments.weaponInstances.keys.shuffled().take(2).map { ShipModule.Weapon(it) }
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		3 -> {
			// Damage engines
			val moduleDamaged = ShipModule.Engines
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damage(moduleDamaged)),
				setOf(moduleDamaged)
			)
		}
		4 -> {
			// Fire!
			CritResult.FireStarted(
				copy(numFires = numFires + 1)
			)
		}
		5 -> {
			// Damage turrets
			val moduleDamaged = ShipModule.Turrets
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damage(moduleDamaged)),
				setOf(moduleDamaged)
			)
		}
		6 -> {
			// Damage shields
			val moduleDamaged = ShipModule.Shields
			CritResult.ModulesDisabled(
				copy(
					shieldAmount = 0,
					modulesStatus = modulesStatus.damage(moduleDamaged)
				),
				setOf(moduleDamaged)
			)
		}
		7 -> {
			// Hull breach
			val damage = Random.nextInt(0, 2) + Random.nextInt(0, 2)
			CritResult.fromImpactResult(impact(damage))
		}
		8 -> {
			// Bulkhead collapse
			val damage = Random.nextInt(0, 5) + Random.nextInt(0, 5)
			CritResult.fromImpactResult(impact(damage))
		}
		else -> CritResult.NoEffect
	}
}
