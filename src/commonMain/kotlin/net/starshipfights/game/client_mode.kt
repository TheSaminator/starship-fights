package net.starshipfights.game

import kotlinx.serialization.Serializable

@Serializable
sealed class ClientMode {
	@Serializable
	data class MatchmakingMenu(val admirals: List<InGameAdmiral>) : ClientMode()
	
	@Serializable
	data class InTrainingGame(val initialState: GameState) : ClientMode()
	
	@Serializable
	data class InGame(val playerSide: GlobalShipController, val connectToken: String, val initialState: GameState) : ClientMode()
	
	@Serializable
	data class Error(val message: String) : ClientMode()
}
