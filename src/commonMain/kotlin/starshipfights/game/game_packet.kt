package starshipfights.game

import kotlinx.serialization.Serializable

@Serializable
sealed class PlayerAction {
	@Serializable
	data class SendChatMessage(val message: String) : PlayerAction()
	
	@Serializable
	data class UseAbility(val type: PlayerAbilityType, val data: PlayerAbilityData) : PlayerAction()
	
	@Serializable
	object TimeOut : PlayerAction()
	
	@Serializable
	object Disconnect : PlayerAction()
}

fun isInternalPlayerAction(playerAction: PlayerAction) = playerAction in setOf(PlayerAction.TimeOut, PlayerAction.Disconnect)

@Serializable
data class GameBeginning(val opponentJoined: Boolean)

@Serializable
sealed class GameEvent {
	@Serializable
	data class StateChange(val newState: GameState) : GameEvent()
	
	@Serializable
	data class InvalidAction(val message: String) : GameEvent()
	
	@Serializable
	data class GameEnd(val winner: GlobalSide?, val message: String) : GameEvent()
}

fun GameState.after(player: GlobalSide, packet: PlayerAction): GameEvent = when (packet) {
	is PlayerAction.SendChatMessage -> {
		GameEvent.StateChange(
			copy(
				chatBox = chatBox + ChatEntry.PlayerMessage(
					senderSide = player,
					sentAt = Moment.now,
					message = packet.message
				)
			)
		)
	}
	is PlayerAction.UseAbility -> {
		if (packet.type in getPossibleAbilities(player))
			packet.type.finishOnServer(this, player, packet.data)
		else
			GameEvent.InvalidAction("That ability cannot be used right now")
	}
	PlayerAction.TimeOut -> {
		val loserName = admiralInfo(player).fullName
		val winnerName = admiralInfo(player.other).fullName
		
		GameEvent.GameEnd(player.other, "$loserName never joined the battle, yielding victory to $winnerName!")
	}
	PlayerAction.Disconnect -> {
		val loserName = admiralInfo(player).fullName
		val winnerName = admiralInfo(player.other).fullName
		
		GameEvent.GameEnd(player.other, "$loserName has disconnected from the battle, yielding victory to $winnerName!")
	}
}
