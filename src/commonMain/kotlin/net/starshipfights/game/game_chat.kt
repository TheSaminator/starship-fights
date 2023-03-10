package net.starshipfights.game

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id

@Serializable
sealed class ChatEntry {
	abstract val sentAt: Moment
	
	@Serializable
	data class AdminAnnouncement(
		override val sentAt: Moment,
		val message: String
	) : ChatEntry()
	
	@Serializable
	data class PlayerMessage(
		val senderSide: GlobalShipController,
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
	data class ShipAttackFailed(
		val ship: Id<ShipInstance>,
		val attacker: ShipAttacker,
		override val sentAt: Moment,
		val weapon: ShipWeapon?,
		val damageIgnoreType: DamageIgnoreType,
	) : ChatEntry()
	
	@Serializable
	data class ShipBoarded(
		val ship: Id<ShipInstance>,
		val boarder: Id<ShipInstance>,
		override val sentAt: Moment,
		val critical: ShipCritical?,
		val damageAmount: Int = 0,
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
	data class TroopsKilled(val number: Int) : ShipCritical()
	
	@Serializable
	data class ModulesHit(val module: Set<ShipModule>) : ShipCritical()
}

fun CritResult.report(): ShipCritical? = when (this) {
	CritResult.NoEffect -> null
	is CritResult.FireStarted -> ShipCritical.Fire
	is CritResult.ModulesDisabled -> ShipCritical.ModulesHit(modules)
	is CritResult.TroopsKilled -> ShipCritical.TroopsKilled(amount)
	is CritResult.HullDamaged -> ShipCritical.ExtraDamage
	is CritResult.Destroyed -> null
}
