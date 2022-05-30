package starshipfights.game

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.litote.kmongo.setValue
import starshipfights.auth.getUser
import starshipfights.data.DocumentTable
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
		delay(750L) // nasty hack
		
		val user = call.getUser() ?: redirect("/login")
		
		val clientMode = when (user.status) {
			UserStatus.AVAILABLE -> ClientMode.Error("You must use the matchmaking interface to enter a game")
			UserStatus.IN_MATCHMAKING -> ClientMode.Error("You must start a game in the matchmaking interface")
			UserStatus.READY_FOR_BATTLE -> call.getGameClientMode()
			UserStatus.IN_BATTLE -> ClientMode.Error("You cannot play in multiple battles at the same time")
		}
		
		call.respondHtml(HttpStatusCode.OK, clientMode.view())
	}
	
	post("/train") {
		val clientMode = call.getTrainingClientMode()
		
		call.respondHtml(HttpStatusCode.OK, clientMode.view())
	}
	
	webSocket("/matchmaking") {
		val oldUser = call.getUser() ?: closeAndReturn("You must be logged in to play") { return@webSocket }
		if (oldUser.status != UserStatus.AVAILABLE)
			closeAndReturn("You cannot play in multiple battles at the same time") { return@webSocket }
		
		val user = oldUser.copy(status = UserStatus.IN_MATCHMAKING)
		User.put(user)
		
		closeReason.invokeOnCompletion {
			DocumentTable.launch {
				delay(150L)
				if (User.get(user.id)?.status == UserStatus.IN_MATCHMAKING)
					User.set(user.id, setValue(User::status, UserStatus.AVAILABLE))
			}
		}
		
		if (matchmakingEndpoint(user))
			User.set(user.id, setValue(User::status, UserStatus.READY_FOR_BATTLE))
	}
	
	webSocket("/game/{token}") {
		val token = call.parameters["token"] ?: closeAndReturn("Invalid or missing battle token") { return@webSocket }
		
		val oldUser = call.getUser() ?: closeAndReturn("You must be logged in to play") { return@webSocket }
		
		if (oldUser.status == UserStatus.IN_BATTLE)
			closeAndReturn("You cannot play in multiple battles at the same time") { return@webSocket }
		if (oldUser.status == UserStatus.IN_MATCHMAKING)
			closeAndReturn("You must start a game in the matchmaking interface") { return@webSocket }
		if (oldUser.status == UserStatus.AVAILABLE)
			closeAndReturn("You must use the matchmaking interface to enter a game") { return@webSocket }
		
		val user = oldUser.copy(status = UserStatus.IN_BATTLE)
		User.put(user)
		
		closeReason.invokeOnCompletion {
			DocumentTable.launch {
				User.set(user.id, setValue(User::status, UserStatus.AVAILABLE))
			}
		}
		
		gameEndpoint(user, token)
	}
}
