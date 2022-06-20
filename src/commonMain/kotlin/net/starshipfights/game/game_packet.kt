package net.starshipfights.game

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
	data class GameEnd(
		val winner: GlobalSide?,
		val message: String,
		@Serializable(with = MapAsListSerializer::class)
		val subplotOutcomes: Map<SubplotKey, SubplotOutcome> = emptyMap()
	) : GameEvent()
}

fun GameState.after(player: GlobalShipController, packet: PlayerAction): GameEvent = when (packet) {
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
		val noShowName = admiralInfo(player).fullName
		
		GameEvent.GameEnd(null, "$noShowName never joined the battle", emptyMap())
	}
	PlayerAction.Disconnect -> {
		val quitterName = admiralInfo(player).fullName
		
		GameEvent.GameEnd(null, "$quitterName has disconnected from the battle", emptyMap())
	}
}.let { event ->
	if (event is GameEvent.StateChange) {
		val subplotKeys = event.newState.subplots.map { it.key }
		val finalState = subplotKeys.fold(event.newState) { newState, key ->
			val subplot = newState.subplots.single { it.key == key }
			subplot.onGameStateChanged(newState)
		}
		GameEvent.StateChange(finalState)
	} else event
}
