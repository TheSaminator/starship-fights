package net.starshipfights.game

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.starshipfights.data.Id
import kotlin.jvm.JvmInline

@Serializable
sealed class ShipModule {
	abstract fun getDisplayName(ship: Ship): String
	
	@Serializable
	data class Weapon(val weaponId: Id<ShipWeapon>) : ShipModule() {
		override fun getDisplayName(ship: Ship): String {
			return ship.armaments[weaponId]?.displayName ?: ""
		}
	}
	
	@Serializable
	object Assault : ShipModule() {
		override fun getDisplayName(ship: Ship): String {
			return "Boarding Transportarium"
		}
	}
	
	@Serializable
	object Defense : ShipModule() {
		override fun getDisplayName(ship: Ship): String {
			return "Internal Defenses"
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
	DESTROYED(false, false),
	ABSENT(false, false)
}

@JvmInline
@Serializable(with = ShipModulesStatusSerializer::class)
value class ShipModulesStatus(val statuses: Map<ShipModule, ShipModuleStatus>) {
	operator fun get(module: ShipModule) = statuses[module] ?: ShipModuleStatus.ABSENT
	
	fun repair(module: ShipModule, repairUnrepairable: Boolean = false) = ShipModulesStatus(
		statuses + if (this[module].canBeRepaired || (repairUnrepairable && this[module] in ShipModuleStatus.DAMAGED..ShipModuleStatus.DESTROYED))
			mapOf(module to ShipModuleStatus.values()[this[module].ordinal - 1])
		else emptyMap()
	)
	
	fun damage(module: ShipModule) = ShipModulesStatus(
		statuses + mapOf(
			module to when (this[module]) {
				ShipModuleStatus.INTACT -> ShipModuleStatus.DAMAGED
				ShipModuleStatus.DAMAGED -> ShipModuleStatus.DESTROYED
				ShipModuleStatus.DESTROYED -> ShipModuleStatus.DESTROYED
				ShipModuleStatus.ABSENT -> ShipModuleStatus.ABSENT
			}
		)
	)
	
	fun damageMany(modules: Iterable<ShipModule>) = ShipModulesStatus(
		statuses + modules.associateWith { module ->
			when (this[module]) {
				ShipModuleStatus.INTACT -> ShipModuleStatus.DAMAGED
				ShipModuleStatus.DAMAGED -> ShipModuleStatus.DESTROYED
				ShipModuleStatus.DESTROYED -> ShipModuleStatus.DESTROYED
				ShipModuleStatus.ABSENT -> ShipModuleStatus.ABSENT
			}
		}
	)
	
	companion object {
		fun forShip(ship: Ship) = ShipModulesStatus(
			mapOf(
				ShipModule.Assault to ShipModuleStatus.INTACT,
				ShipModule.Defense to ShipModuleStatus.INTACT,
				ShipModule.Shields to if (ship.hasShields) ShipModuleStatus.INTACT else ShipModuleStatus.ABSENT,
				ShipModule.Engines to ShipModuleStatus.INTACT,
				ShipModule.Turrets to ShipModuleStatus.INTACT,
			) + ship.armaments.keys.associate {
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
	data class TroopsKilled(val ship: ShipInstance, val amount: Int) : CritResult()
	data class HullDamaged(val ship: ShipInstance, val amount: Int) : CritResult()
	data class Destroyed(val ship: ShipWreck) : CritResult()
	
	companion object {
		fun fromImpactResult(impactResult: ImpactResult) = when (impactResult) {
			is ImpactResult.Damaged -> impactResult.damage.amount.takeIf { it > 0 }?.let { HullDamaged(impactResult.ship, it) } ?: NoEffect
			is ImpactResult.Destroyed -> Destroyed(impactResult.ship)
		}
	}
}

fun ShipInstance.doCriticalDamage(): CritResult {
	if (ship.shipType.faction == Faction.FELINAE_FELICES)
		return doCriticalDamageFelinae()
	
	return when ((0..8).random() + (0..8).random()) { // Ranges in 0..16, probability density peaks at 8
		0 -> {
			// Damage ALL the modules!
			val modulesDamaged = modulesStatus.statuses.filter { (k, v) -> k !is ShipModule.Weapon && v != ShipModuleStatus.ABSENT }.keys
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		1 -> {
			// Damage 3 weapons
			val modulesDamaged = armaments.keys.shuffled().take(3).map { ShipModule.Weapon(it) }
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		2 -> {
			// Damage 2 weapons
			val modulesDamaged = armaments.keys.shuffled().take(2).map { ShipModule.Weapon(it) }
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		3 -> {
			// Damage 2 random modules
			val modulesDamaged = modulesStatus.statuses.filter { (k, v) -> k !is ShipModule.Weapon && v != ShipModuleStatus.ABSENT }.keys.shuffled().take(2)
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		4 -> {
			// Damage 1 weapon
			val modulesDamaged = armaments.keys.shuffled().take(1).map { ShipModule.Weapon(it) }
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		5 -> {
			// Damage engines
			val moduleDamaged = ShipModule.Engines
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damage(moduleDamaged)),
				setOf(moduleDamaged)
			)
		}
		6 -> {
			// Damage transportarium
			val moduleDamaged = ShipModule.Assault
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damage(moduleDamaged)),
				setOf(moduleDamaged)
			)
		}
		7 -> {
			// Lose a few troops
			val deaths = (1..2).random()
			killTroops(deaths)
		}
		8 -> {
			// Fire!
			CritResult.FireStarted(
				copy(numFires = numFires + 1)
			)
		}
		9 -> {
			// Two fires!
			CritResult.FireStarted(
				copy(numFires = numFires + 2)
			)
		}
		10 -> {
			// Damage turrets
			val moduleDamaged = ShipModule.Turrets
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damage(moduleDamaged)),
				setOf(moduleDamaged)
			)
		}
		11 -> {
			// Lose many troops
			val deaths = (1..2).random() + (1..2).random()
			killTroops(deaths)
		}
		12 -> {
			// Damage security system
			val moduleDamaged = ShipModule.Defense
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damage(moduleDamaged)),
				setOf(moduleDamaged)
			)
		}
		13 -> {
			// Damage random module
			val moduleDamaged = modulesStatus.statuses.keys.filter { it !is ShipModule.Weapon }.random()
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damage(moduleDamaged)),
				setOf(moduleDamaged)
			)
		}
		14 -> {
			// Damage shields
			val moduleDamaged = ShipModule.Shields
			if (ship.hasShields)
				CritResult.ModulesDisabled(
					copy(
						shieldAmount = 0,
						modulesStatus = modulesStatus.damage(moduleDamaged)
					),
					setOf(moduleDamaged)
				)
			else
				CritResult.NoEffect
		}
		15 -> {
			// Hull breach
			val damage = (0..2).random() + (1..3).random()
			CritResult.fromImpactResult(impact(damage, true))
		}
		16 -> {
			// Bulkhead collapse
			val damage = (2..4).random() + (3..5).random()
			CritResult.fromImpactResult(impact(damage, true))
		}
		else -> CritResult.NoEffect
	}
}

private fun ShipInstance.doCriticalDamageFelinae(): CritResult {
	return when ((0..5).random() + (0..5).random()) {
		0 -> {
			// Damage ALL the modules!
			val modulesDamaged = modulesStatus.statuses.filter { (k, v) -> k !is ShipModule.Weapon && v != ShipModuleStatus.ABSENT }.keys
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		1 -> {
			// Damage 3 weapons
			val modulesDamaged = armaments.keys.shuffled().take(3).map { ShipModule.Weapon(it) }
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		2 -> {
			// Damage 2 weapons
			val modulesDamaged = armaments.keys.shuffled().take(2).map { ShipModule.Weapon(it) }
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		3 -> {
			// Damage 2 random modules
			val modulesDamaged = modulesStatus.statuses.filter { (k, v) -> k !is ShipModule.Weapon && v != ShipModuleStatus.ABSENT }.keys.shuffled().take(2)
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		4 -> {
			// Damage 1 weapon
			val modulesDamaged = armaments.keys.shuffled().take(1).map { ShipModule.Weapon(it) }
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damageMany(modulesDamaged)),
				modulesDamaged.toSet()
			)
		}
		5 -> {
			// Damage engines
			val moduleDamaged = ShipModule.Engines
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damage(moduleDamaged)),
				setOf(moduleDamaged)
			)
		}
		6 -> {
			// Damage turrets
			val moduleDamaged = ShipModule.Turrets
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damage(moduleDamaged)),
				setOf(moduleDamaged)
			)
		}
		7 -> {
			// Damage random module
			val moduleDamaged = modulesStatus.statuses.keys.filter { it !is ShipModule.Weapon }.random()
			CritResult.ModulesDisabled(
				copy(modulesStatus = modulesStatus.damage(moduleDamaged)),
				setOf(moduleDamaged)
			)
		}
		8 -> {
			// Lose some troops
			val deaths = (1..3).random()
			killTroops(deaths)
		}
		9 -> {
			// Hull breach
			val damage = (0..2).random() + (1..3).random()
			CritResult.fromImpactResult(impact(damage))
		}
		10 -> {
			// Bulkhead collapse
			val damage = (2..4).random() + (3..5).random()
			CritResult.fromImpactResult(impact(damage))
		}
		else -> CritResult.NoEffect
	}
}
