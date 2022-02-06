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
	data class ShipDestroyed(
		val ship: Id<ShipInstance>,
		override val sentAt: Moment,
		val destroyedBy: ShipDestructionType
	) : ChatEntry()
}

@Serializable
sealed class ShipDestructionType {
	@Serializable
	data class EnemyShip(val id: Id<ShipInstance>) : ShipDestructionType()
	
	@Serializable
	object Bombers : ShipDestructionType()
}
