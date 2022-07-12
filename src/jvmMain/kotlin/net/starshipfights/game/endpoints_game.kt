package net.starshipfights.game

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import net.starshipfights.auth.getUser
import net.starshipfights.data.admiralty.getAllInGameAdmiralsForBattle
import net.starshipfights.redirect

fun Routing.installGame() {
	get("/lobby") {
		val user = call.getUser() ?: redirect("/login")
		
		val clientMode = ClientMode.MatchmakingMenu(getAllInGameAdmiralsForBattle(user))
		
		call.respondHtml(HttpStatusCode.OK, clientMode.view())
	}
	
	post("/play") {
		delay(750L) // nasty hack
		
		val clientMode = call.getGameClientMode()
		
		call.respondHtml(HttpStatusCode.OK, clientMode.view())
	}
	
	post("/train") {
		val clientMode = call.getTrainingClientMode()
		
		call.respondHtml(HttpStatusCode.OK, clientMode.view())
	}
	
	webSocket("/matchmaking") {
		val user = call.getUser() ?: closeAndReturn("You must be logged in to play") { return@webSocket }
		
		matchmakingEndpoint(user)
	}
	
	webSocket("/game/{token}") {
		val token = call.parameters["token"] ?: closeAndReturn("Invalid or missing battle token") { return@webSocket }
		
		val user = call.getUser() ?: closeAndReturn("You must be logged in to play") { return@webSocket }
		
		gameEndpoint(user, token)
	}
}
