package starshipfights.game

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import starshipfights.auth.getUser
import starshipfights.data.admiralty.getAllInGameAdmirals
import starshipfights.data.auth.User
import starshipfights.redirect

fun Routing.installGame() {
	get("/lobby") {
		val user = call.getUser() ?: redirect("/login")
		
		val clientMode = if (user.isInBattle)
			ClientMode.Error("You cannot play in multiple battles at the same time")
		else
			ClientMode.MatchmakingMenu(getAllInGameAdmirals(user))
		
		call.respondHtml(HttpStatusCode.OK, clientMode.view())
	}
	
	post("/play") {
		val user = call.getUser() ?: redirect("/login")
		
		val clientMode = if (user.isInBattle)
			ClientMode.Error("You cannot play in multiple battles at the same time")
		else
			call.getGameClientMode()
		
		call.respondHtml(HttpStatusCode.OK, clientMode.view())
	}
	
	webSocket("/matchmaking") {
		val user = call.getUser() ?: closeAndReturn("You must be logged in to play") { return@webSocket }
		if (user.isInBattle)
			closeAndReturn("You cannot play in multiple battles at the same time") { return@webSocket }
		
		matchmakingEndpoint(user)
	}
	
	webSocket("/game/{token}") {
		val token = call.parameters["token"] ?: closeAndReturn("Invalid or missing battle token") { return@webSocket }
		
		val oldUser = call.getUser() ?: closeAndReturn("You must be logged in to play") { return@webSocket }
		if (oldUser.isInBattle)
			closeAndReturn("You cannot play in multiple battles at the same time") { return@webSocket }
		
		val user = oldUser.copy(isInBattle = true)
		launch {
			User.put(user)
		}
		
		gameEndpoint(user, token)
		
		val postGameUser = user.copy(isInBattle = false)
		launch {
			User.put(postGameUser)
		}
	}
}
