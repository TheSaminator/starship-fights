package net.starshipfights.campaign

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import net.starshipfights.data.Id
import net.starshipfights.game.ClientMode
import net.starshipfights.game.view

fun Routing.installCampaign() {
	get("/test-cluster") {
		call.respondHtml(block = call.campaignTestPage())
	}
	
	post("/test-cluster") {
		val parameters = call.receiveParameters()
		
		val color = StarClusterBackground.valueOf(parameters.getOrFail("color"))
		val size = ClusterSize.valueOf(parameters.getOrFail("size"))
		val density = ClusterLaneDensity.valueOf(parameters.getOrFail("density"))
		val planets = ClusterPlanetDensity.valueOf(parameters.getOrFail("planets"))
		val corruption = ClusterCorruption.valueOf(parameters.getOrFail("corruption"))
		
		val cluster = ClusterGenerator(
			ClusterGenerationSettings(color, size, density, planets, corruption)
		).generateCluster()
		
		val clientMode = ClientMode.CampaignMap(
			Id(""),
			emptyMap(),
			Id(""),
			cluster
		)
		
		call.respondHtml(HttpStatusCode.OK, clientMode.view())
	}
}
