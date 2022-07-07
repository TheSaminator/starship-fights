package net.starshipfights.game

import kotlinx.serialization.Serializable
import net.starshipfights.campaign.CampaignAdmiral
import net.starshipfights.campaign.StarClusterView
import net.starshipfights.data.Id

@Serializable
sealed class ClientMode {
	@Serializable
	data class CampaignMap(
		val playingAs: Id<InGameAdmiral>?,
		val admirals: Map<Id<InGameAdmiral>, CampaignAdmiral>,
		val clusterToken: Id<StarClusterView>,
		val clusterView: StarClusterView
	) : ClientMode()
	
	@Serializable
	data class MatchmakingMenu(
		val admirals: List<InGameAdmiral>
	) : ClientMode()
	
	@Serializable
	data class InTrainingGame(
		val initialState: GameState
	) : ClientMode()
	
	@Serializable
	data class InGame(
		val playerSide: GlobalShipController,
		val connectToken: String,
		val initialState: GameState
	) : ClientMode()
	
	@Serializable
	data class Error(
		val message: String
	) : ClientMode()
}
