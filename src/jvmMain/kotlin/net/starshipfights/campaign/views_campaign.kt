package net.starshipfights.campaign

import io.ktor.application.*
import kotlinx.html.*
import net.starshipfights.game.toUrlSlug
import net.starshipfights.info.page
import net.starshipfights.info.standardNavBar

suspend fun ApplicationCall.campaignTestPage(): HTML.() -> Unit {
	return page(
		"Star Cluster Test",
		standardNavBar(),
		null
	) {
		section {
			h1 { +"Star Cluster Test" }
			p { +"This is only a test and may not be indicative of the finished star-cluster feature for Starship Fights" }
			form(action = "/test-cluster", method = FormMethod.post) {
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
}
