package starshipfights.info

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import starshipfights.data.admiralty.AdmiralNameFlavor
import starshipfights.data.admiralty.AdmiralNames
import starshipfights.game.Moment
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
	
	get("/about/pp") {
		call.respondHtml(HttpStatusCode.OK, call.privacyPolicyPage())
	}
	
	get("/about/tnc") {
		call.respondHtml(HttpStatusCode.OK, call.termsAndConditionsPage())
	}
	
	get("/users") {
		call.respondHtml(HttpStatusCode.OK, call.newUsersPage())
	}
	
	// Random name generation
	get("/generate-name/{flavor}/{gender}") {
		val flavor = call.parameters["flavor"]?.let { flavor -> AdmiralNameFlavor.values().singleOrNull { it.toUrlSlug().equals(flavor, ignoreCase = true) } }!!
		val isFemale = call.parameters["gender"]?.startsWith('f', ignoreCase = true) ?: false
		
		call.respondText(AdmiralNames.randomName(flavor, isFemale), ContentType.Text.Plain)
	}
	
	// Cache utils
	val cacheTime = String.format("%f", Moment.now.toMillis())
	get("/cache-time") {
		call.respondText(cacheTime, ContentType.Text.Plain, HttpStatusCode.OK)
	}
}
