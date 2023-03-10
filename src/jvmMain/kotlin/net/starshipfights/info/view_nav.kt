package net.starshipfights.info

import io.ktor.application.*
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.span
import kotlinx.html.style
import net.starshipfights.CurrentConfiguration
import net.starshipfights.auth.getUserAndSession
import net.starshipfights.data.Id
import net.starshipfights.data.auth.UserSession

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

data class NavLink(val to: String, val text: String, val classes: String? = null, val isPost: Boolean = false, val csrfUserCookie: Id<UserSession>? = null) : NavItem() {
	override fun DIV.display() {
		a(href = to, classes = classes) {
			if (isPost)
				method = "post"
			csrfUserCookie?.let { csrfToken(it) }
			
			+text
		}
	}
}

suspend fun ApplicationCall.standardNavBar(): List<NavItem> = listOf(
	NavLink("/", "Main Page"),
	NavLink("/info", "Read Manual"),
	NavLink("/about", "About Starship Fights"),
	NavLink("/users", "New Users"),
	NavHead("Your Account"),
) + getUserAndSession().let { (session, user) ->
	if (session == null || user == null)
		listOf(
			NavLink("/login", "Login with Discord"),
		)
	else
		listOf(
			NavLink("/me", user.profileName),
			NavLink("/me/manage", "User Preferences"),
			NavLink("/lobby", "Enter Game Lobby", classes = "desktop"),
			NavLink("/logout", "Log Out", isPost = true, csrfUserCookie = session.id),
		)
} + listOf(
	NavHead("External Information")
) + (CurrentConfiguration.discordClient?.serverInvite?.let {
	listOf<NavItem>(
		NavLink("https://discord.gg/$it", "Official Discord")
	)
} ?: emptyList()) + listOf(
	NavLink("https://mechyrdia.info/", "Mechyrdia Infobase"),
	NavLink("https://nationstates.net/mechyrdia", "Multiverse Access"),
	NavLink("https://www.buymeacoffee.com/starshipfights", "Support Starship Fights"),
)
