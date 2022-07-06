package net.starshipfights.campaign

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.html.*
import net.starshipfights.auth.withErrorMessage
import net.starshipfights.data.Id
import net.starshipfights.game.*
import net.starshipfights.labs.lab
import net.starshipfights.labs.labPost
import net.starshipfights.labs.labUrl
import net.starshipfights.redirect

fun Routing.installCampaign() {
	lab("cluster", "Star Clusters") { errorMessage ->
		section {
			h1 { +"Star Clusters" }
			p { +"This is only a test and may not be indicative of the finished star-cluster feature for Starship Fights" }
			form(action = "/labs/cluster", method = FormMethod.post) {
				errorMessage?.let { errorMsg ->
					p {
						style = "color:#d22"
						+errorMsg
					}
				}
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
						+corruption.displayName
						br
					}
				}
				h3 { +"Factional Contention" }
				for (contention in ClusterContention.values()) {
					val contentionId = "contention-${contention.toUrlSlug()}"
					label {
						htmlFor = contentionId
						radioInput(name = "contention") {
							id = contentionId
							value = contention.name
							required = true
						}
						+contention.displayName
						br
					}
				}
				h3 { +"Per-Faction Modes" }
				p {
					strong { +"Set All" }
					for (mode in ClusterFactionMode.values()) {
						+Entities.nbsp
						a(href = "#", classes = "set-all") {
							attributes["data-enable-class"] = "faction-mode-${mode.toUrlSlug()}"
							+mode.displayName
						}
					}
				}
				for (factionFlavor in FactionFlavor.values())
					p {
						strong { +factionFlavor.displayName }
						br
						+"Uses ${factionFlavor.shipSource.adjective} ships, is loyal to ${factionFlavor.loyalties.first().getDefiniteShortName()}."
						br
						for (mode in ClusterFactionMode.values()) {
							val modeId = "mode-${factionFlavor.toUrlSlug()}-${mode.toUrlSlug()}"
							label {
								htmlFor = modeId
								radioInput(name = "factions[${factionFlavor.toUrlSlug()}]") {
									id = modeId
									value = mode.name
									required = true
									if (mode == ClusterFactionMode.ALLOW)
										checked = true
									classes = setOf(
										"faction-choice",
										"faction-loyalty-${factionFlavor.loyalties.first().toUrlSlug()}",
										"faction-shipset-${factionFlavor.shipSource.toUrlSlug()}",
										"faction-mode-${mode.toUrlSlug()}",
									)
								}
								+mode.displayName
								+Entities.nbsp
							}
						}
					}
				p {
					strong { +"Set All by Faction Loyalty" }
					val loyalties = FactionFlavor.values().map { it.loyalties.first() }.distinct()
					for (loyalty in loyalties) {
						br
						+"${loyalty.shortName}:"
						for (mode in ClusterFactionMode.values()) {
							+Entities.nbsp
							a(href = "#", classes = "set-all-by-faction") {
								attributes["data-filter-class"] = "faction-loyalty-${loyalty.toUrlSlug()}"
								attributes["data-enable-class"] = "faction-mode-${mode.toUrlSlug()}"
								+mode.displayName
							}
						}
					}
				}
				p {
					strong { +"Set All by Shipset" }
					val shipSets = FactionFlavor.values().map { it.shipSource }.distinct()
					for (shipSet in shipSets) {
						br
						+"${shipSet.shortName}:"
						for (mode in ClusterFactionMode.values()) {
							+Entities.nbsp
							a(href = "#", classes = "set-all-by-faction") {
								attributes["data-filter-class"] = "faction-shipset-${shipSet.toUrlSlug()}"
								attributes["data-enable-class"] = "faction-mode-${mode.toUrlSlug()}"
								+mode.displayName
							}
						}
					}
				}
				submitInput {
					value = "Generate Star Cluster"
				}
				script {
					unsafe { +"window.sfClusterGenTest = true;" }
				}
			}
		}
	}
	
	labPost("cluster") {
		val parameters = call.receiveParameters()
		
		val color = StarClusterBackground.values().valueOfOrRedirect(parameters.getOrFail("color")) { "Invalid value chosen for background color" }
		val size = ClusterSize.values().valueOfOrRedirect(parameters.getOrFail("size")) { "Invalid value chosen for cluster size" }
		val density = ClusterLaneDensity.values().valueOfOrRedirect(parameters.getOrFail("density")) { "Invalid value chosen for warp lane density" }
		val planets = ClusterPlanetDensity.values().valueOfOrRedirect(parameters.getOrFail("planets")) { "Invalid value chosen for planet density" }
		val corruption = ClusterCorruption.values().valueOfOrRedirect(parameters.getOrFail("corruption")) { "Invalid value chosen for eldritch corruption" }
		val contention = ClusterContention.values().valueOfOrRedirect(parameters.getOrFail("contention")) { "Invalid value chosen for factional contention" }
		val factions = try {
			ClusterFactions(FactionFlavor.values().mapNotNull { faction ->
				parameters["factions[${faction.toUrlSlug()}]"]
					?.let { ClusterFactionMode.values().valueOfOrNull(it) }
					?.let { faction to it }
			}.toMap())
		} catch (ex: IllegalArgumentException) {
			redirect(labUrl("cluster") + withErrorMessage("Invalid values chosen for faction modes"))
		}
		
		val cluster = ClusterGenerator(
			ClusterGenerationSettings(color, size, density, planets, corruption, factions, contention)
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

private fun <T : Enum<T>> Array<T>.valueOfOrNull(param: String?) = singleOrNull { it.name == param }

private fun <T : Enum<T>> Array<T>.valueOfOrRedirect(param: String?, message: () -> String) = valueOfOrNull(param) ?: redirect(labUrl("cluster") + withErrorMessage(message()))
