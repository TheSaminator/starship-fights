package net.starshipfights.campaign

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.html.*
import net.starshipfights.data.Id
import net.starshipfights.game.ClientMode
import net.starshipfights.game.toUrlSlug
import net.starshipfights.game.view
import net.starshipfights.labs.lab
import net.starshipfights.labs.labPost

fun Routing.installCampaign() {
	lab("cluster", "Star Clusters") {
		section {
			h1 { +"Star Clusters" }
			p { +"This is only a test and may not be indicative of the finished star-cluster feature for Starship Fights" }
			form(action = "/labs/cluster", method = FormMethod.post) {
				h2 { +"Generation Parameters" }
				h3 { +"Background Color" }
				for (color in StarClusterBackground.values()) {
					val colorId = "color-${color.toUrlSlug()}"
					label {
						htmlFor = colorId
						radioInput(name = "color") {
							id = colorId
							value = color.name
							required = true
						}
						+Entities.nbsp
						+color.displayName
						br
					}
				}
				h3 { +"Size" }
				for (size in ClusterSize.values()) {
					val sizeId = "size-${size.toUrlSlug()}"
					label {
						htmlFor = sizeId
						radioInput(name = "size") {
							id = sizeId
							value = size.name
							required = true
						}
						+Entities.nbsp
						+size.displayName
						br
					}
				}
				h3 { +"Warp Lane Density" }
				for (density in ClusterLaneDensity.values()) {
					val densityId = "density-${density.toUrlSlug()}"
					label {
						htmlFor = densityId
						radioInput(name = "density") {
							id = densityId
							value = density.name
							required = true
						}
						+Entities.nbsp
						+density.displayName
						br
					}
				}
				h3 { +"Planet Density" }
				for (planets in ClusterPlanetDensity.values()) {
					val planetsId = "planets-${planets.toUrlSlug()}"
					label {
						htmlFor = planetsId
						radioInput(name = "planets") {
							id = planetsId
							value = planets.name
							required = true
						}
						+Entities.nbsp
						+planets.displayName
						br
					}
				}
				h3 { +"Eldritch Corruption" }
				for (corruption in ClusterCorruption.values()) {
					val corruptionId = "corruption-${corruption.toUrlSlug()}"
					label {
						htmlFor = corruptionId
						radioInput(name = "corruption") {
							id = corruptionId
							value = corruption.name
							required = true
						}
						+Entities.nbsp
						+corruption.displayName
						br
					}
				}
				submitInput {
					value = "Generate Star Cluster"
				}
			}
		}
	}
	
	labPost("cluster") {
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
