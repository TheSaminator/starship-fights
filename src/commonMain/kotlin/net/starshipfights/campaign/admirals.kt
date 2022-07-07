package net.starshipfights.campaign

import kotlinx.serialization.Serializable
import net.starshipfights.game.InGameAdmiral

enum class CampaignAdmiralStatus {
	HOST,
	MEMBER,
	INVITED;
	
	val displayName: String
		get() = when (this) {
			HOST -> "Campaign Host"
			MEMBER -> "Campaign Player"
			INVITED -> "Invited to Campaign"
		}
}

@Serializable
data class CampaignAdmiral(
	val admiral: InGameAdmiral,
	val status: CampaignAdmiralStatus
)
