package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id

@Serializable
sealed class ChatEntry {
	abstract val sentAt: Moment
	
	@Serializable
	data class PlayerMessage(
		val senderSide: GlobalSide,
		override val sentAt: Moment,
		val message: String
	) : ChatEntry()
	
	@Serializable
	data class ShipIdentified(
		val ship: Id<ShipInstance>,
		override val sentAt: Moment,
	) : ChatEntry()
	
	@Serializable
	data class ShipEscaped(
		val ship: Id<ShipInstance>,
		override val sentAt: Moment
	) : ChatEntry()
	
	@Serializable
	data class ShipAttacked(
		val ship: Id<ShipInstance>,
		val attacker: ShipAttacker,
		override val sentAt: Moment,
		val damageInflicted: Int,
		val weapon: ShipWeapon?,
		val critical: ShipCritical?,
	) : ChatEntry()
	
	@Serializable
	data class ShipDestroyed(
		val ship: Id<ShipInstance>,
		override val sentAt: Moment,
		val destroyedBy: ShipAttacker
	) : ChatEntry()
}

@Serializable
sealed class ShipAttacker {
	@Serializable
	data class EnemyShip(val id: Id<ShipInstance>) : ShipAttacker()
	
	@Serializable
	object Bombers : ShipAttacker()
	
	@Serializable
	object Fire : ShipAttacker()
}

@Serializable
sealed class ShipCritical {
	@Serializable
	object ExtraDamage : ShipCritical()
	
	@Serializable
	object Fire : ShipCritical()
	
	@Serializable
	data class ModulesHit(val module: Set<ShipModule>) : ShipCritical()
}

fun CritResult.report(): ShipCritical? = when (this) {
	CritResult.NoEffect -> null
	is CritResult.FireStarted -> ShipCritical.Fire
	is CritResult.ModulesDisabled -> ShipCritical.ModulesHit(modules)
	is CritResult.HullDamaged -> ShipCritical.ExtraDamage
	is CritResult.Destroyed -> null
}
