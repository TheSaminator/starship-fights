package starshipfights.game

import io.ktor.application.*
import io.ktor.request.*
import kotlinx.html.*
import starshipfights.auth.getUserSession
import starshipfights.redirect

fun ClientMode.view(): HTML.() -> Unit = {
	head {
		meta(charset = "UTF-8")
		
		link(rel = "icon", type = "image/svg+xml", href = "/static/images/icon.svg")
		
		link(rel = "preconnect", href = "https://fonts.googleapis.com")
		link(rel = "preconnect", href = "https://fonts.gstatic.com") { attributes["crossorigin"] = "anonymous" }
		link(rel = "stylesheet", href = "https://fonts.googleapis.com/css2?family=Cinzel:wght@400;500;600;700;800;900&family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap")
		link(rel = "stylesheet", href = "/static/game/style.css")
		
		script(src = "/static/game/textfit.min.js") {}
		
		script(src = "/static/game/three.js") {}
		script(src = "/static/game/three-examples.js") {}
		script(src = "/static/game/three-extras.js") {}
		
		when (this@view) {
			is ClientMode.MatchmakingMenu -> title("Starship Fights | Lobby")
			is ClientMode.InTrainingGame -> title("Starship Fights | Training")
			is ClientMode.InGame -> title("Starship Fights | In-Game")
			is ClientMode.Error -> title("Starship Fights | Error!")
		}
	}
	body {
		canvas { id = "three-canvas" }
		
		div(classes = "ui-layer") { id = "ui" }
		
		div(classes = "hide") {
			id = "popup"
			div(classes = "panel") {
				id = "popup-panel"
				div {
					id = "popup-box"
				}
			}
		}
		
		script {
			attributes["id"] = "sf-client-mode"
			type = "application/json"
			unsafe {
				+jsonSerializer.encodeToString(ClientMode.serializer(), this@view)
			}
		}
		
		script(src = "/static/game/starship-fights.js") {}
	}
}

suspend fun ApplicationCall.getGameClientMode(): ClientMode {
	val userId = getUserSession()?.user ?: redirect("/login")
	val token = receiveParameters()["token"] ?: return ClientMode.Error("Invalid or missing battle token")
	val game = GameManager.joinGame(userId, token, false) ?: return ClientMode.Error("That battle is no longer available")
	return ClientMode.InGame(
		game.side,
		token,
		game.session.state.value
	)
}
