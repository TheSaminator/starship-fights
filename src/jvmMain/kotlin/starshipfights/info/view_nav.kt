package starshipfights.info

import io.ktor.application.*
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.span
import kotlinx.html.style
import starshipfights.auth.getUser

sealed class NavItem {
	protected abstract fun DIV.display()
	fun displayIn(div: DIV) = div.display()
}

data class NavHead(val label: String) : NavItem() {
	override fun DIV.display() {
		span {
			style = "font-variant:small-caps;text-decoration:underline"
			+label
		}
	}
}

data class NavLink(val to: String, val text: String) : NavItem() {
	override fun DIV.display() {
		a(href = to) {
			+text
		}
	}
}

suspend fun ApplicationCall.standardNavBar(): List<NavItem> = listOf(
	NavLink("/", "Main Page"),
	NavLink("/info", "Read Manual"),
	NavLink("/about", "About Starship Fights"),
	NavHead("Your Account"),
) + when (val user = getUser()) {
	null -> listOf(
		NavLink("/login", "Log In"),
	)
	else -> listOf(
		NavLink("/me", user.username),
		NavLink("/me/manage", "User Preferences"),
		NavLink("/lobby", "Enter Game Lobby"),
		NavLink("/logout", "Log Out"),
	)
} + listOf(
	NavHead("External Information"),
	NavLink("https://mechyrdia.netlify.app/", "Mechyrdia Infobase"),
	NavLink("https://nationstates.net/mechyrdia", "Multiverse Access"),
)
