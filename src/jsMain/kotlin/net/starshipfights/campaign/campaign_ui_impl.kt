package net.starshipfights.campaign

import externals.threejs.Scene

private class CampaignUIResponderImpl(val clusterView: StarClusterView, val scene: Scene) : CampaignUIResponder {
	override fun getStarCluster() = clusterView
	override fun getRenderScene() = scene
}

fun uiResponder(clusterView: StarClusterView, scene: Scene): CampaignUIResponder = CampaignUIResponderImpl(clusterView, scene)
