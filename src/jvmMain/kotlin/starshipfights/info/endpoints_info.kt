package starshipfights.info

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import starshipfights.data.admiralty.AdmiralNameFlavor
import starshipfights.data.admiralty.AdmiralNames
import starshipfights.game.ShipType
import starshipfights.game.toUrlSlug

fun Routing.installPages() {
	get("/") {
		call.respondHtml(HttpStatusCode.OK, call.mainPage())
	}
	
	get("/info") {
		call.respondHtml(HttpStatusCode.OK, call.shipsPage())
	}
	
	get("/info/{ship}") {
		val ship = call.parameters["ship"]?.let { param -> ShipType.values().singleOrNull { it.toUrlSlug() == param } }!!
		call.respondHtml(HttpStatusCode.OK, call.shipPage(ship))
	}
	
	get("/about") {
		call.respondHtml(HttpStatusCode.OK, call.aboutPage())
	}
	
	// Random name generation
	get("/generate-name/{flavor}/{gender}") {
		val flavor = call.parameters["flavor"]?.let { flavor -> AdmiralNameFlavor.values().singleOrNull { it.toUrlSlug() == flavor.lowercase() } }!!
		val isFemale = call.parameters["gender"]?.lowercase()?.startsWith('f') ?: false
		
		call.respondText(AdmiralNames.randomName(flavor, isFemale), ContentType.Text.Plain)
	}
}
