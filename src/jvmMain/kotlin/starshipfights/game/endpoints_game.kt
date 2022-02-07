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
import starshipfights.data.auth.UserStatus
import starshipfights.redirect

fun Routing.installGame() {
	get("/lobby") {
		val user = call.getUser() ?: redirect("/login")
		
		val clientMode = if (user.status == UserStatus.AVAILABLE)
			ClientMode.MatchmakingMenu(getAllInGameAdmirals(user))
		else
			ClientMode.Error("You cannot play in multiple battles at the same time")
		
		call.respondHtml(HttpStatusCode.OK, clientMode.view())
	}
	
	post("/play") {
		val user = call.getUser() ?: redirect("/login")
		
		val clientMode = when (user.status) {
			UserStatus.AVAILABLE -> ClientMode.Error("You must use the matchmaking interface to enter a game")
			UserStatus.IN_MATCHMAKING -> call.getGameClientMode()
			UserStatus.IN_BATTLE -> ClientMode.Error("You cannot play in multiple battles at the same time")
		}
		
		call.respondHtml(HttpStatusCode.OK, clientMode.view())
	}
	
	webSocket("/matchmaking") {
		val oldUser = call.getUser() ?: closeAndReturn("You must be logged in to play") { return@webSocket }
		if (oldUser.status != UserStatus.AVAILABLE)
			closeAndReturn("You cannot play in multiple battles at the same time") { return@webSocket }
		
		val user = oldUser.copy(status = UserStatus.IN_MATCHMAKING)
		launch {
			User.put(user)
		}
		
		matchmakingEndpoint(user)
	}
	
	webSocket("/game/{token}") {
		val token = call.parameters["token"] ?: closeAndReturn("Invalid or missing battle token") { return@webSocket }
		
		val oldUser = call.getUser() ?: closeAndReturn("You must be logged in to play") { return@webSocket }
		
		if (oldUser.status == UserStatus.IN_BATTLE)
			closeAndReturn("You cannot play in multiple battles at the same time") { return@webSocket }
		if (oldUser.status == UserStatus.AVAILABLE)
			closeAndReturn("You must use the matchmaking interface to enter a game") { return@webSocket }
		
		val user = oldUser.copy(status = UserStatus.IN_BATTLE)
		User.put(user)
		
		gameEndpoint(user, token)
		
		launch {
			val postGameUser = user.copy(status = UserStatus.AVAILABLE)
			User.put(postGameUser)
		}
	}
}
