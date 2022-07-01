package net.starshipfights.campaign

private class CampaignUIResponderImpl(val clusterView: StarClusterView) : CampaignUIResponder {
	override fun getStarCluster() = clusterView
}

fun uiResponder(clusterView: StarClusterView): CampaignUIResponder = CampaignUIResponderImpl(clusterView)
