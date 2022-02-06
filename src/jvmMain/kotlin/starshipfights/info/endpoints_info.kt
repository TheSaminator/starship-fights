package starshipfights.info

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.routing.*
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
}
