package net.starshipfights.info

import kotlinx.html.*
import net.starshipfights.data.auth.User
import net.starshipfights.data.auth.getTrophies
import net.starshipfights.data.auth.renderTrophy
import net.starshipfights.game.ShipType
import net.starshipfights.game.getDefiniteShortName

abstract class Sidebar {
	protected abstract fun TagConsumer<*>.display()
	fun displayIn(aside: ASIDE) = aside.consumer.display()
}

class CustomSidebar(private val block: TagConsumer<*>.() -> Unit) : Sidebar() {
	override fun TagConsumer<*>.display() = block()
}

data class ShipViewSidebar(val shipType: ShipType) : Sidebar() {
	override fun TagConsumer<*>.display() {
		p {
			img(alt = "Flag of ${shipType.faction.getDefiniteShortName()}", src = shipType.faction.flagUrl)
		}
		p {
			style = "text-align:center"
			+shipType.weightClass.displayName
			+" of the "
			+shipType.faction.navyName
		}
	}
}

data class PageNavSidebar(val contents: List<NavItem>) : Sidebar() {
	override fun TagConsumer<*>.display() {
		div(classes = "list") {
			for (it in contents) {
				div(classes = "item") {
					it.displayIn(this)
				}
			}
		}
	}
}

data class UserProfileSidebar(val user: User, val isCurrentUser: Boolean, val hasOpenSessions: Boolean) : Sidebar() {
	override fun TagConsumer<*>.display() {
		if (user.showDiscordName) {
			img(src = user.discordAvatarUrl) {
				style = "border-radius:50%"
			}
			p {
				style = "text-align:center"
				+user.discordName
				+"#"
				+user.discordDiscriminator
			}
		} else {
			img(src = user.anonymousAvatarUrl) {
				style = "border-radius:50%"
			}
		}
		for (trophy in user.getTrophies())
			renderTrophy(trophy)
		
		if (user.showUserStatus) {
			p {
				style = "text-align:center"
				+if (hasOpenSessions) "Online" else "Offline"
			}
			p {
				style = "text-align:center"
				+"Registered at "
				span(classes = "moment") {
					style = "display:none"
					+user.registeredAt.toEpochMilli().toString()
				}
				br
				+"Last active at "
				span(classes = "moment") {
					style = "display:none"
					+user.lastActivity.toEpochMilli().toString()
				}
			}
		}
		if (isCurrentUser) {
			hr { style = "border-color:#036" }
			div(classes = "list") {
				div(classes = "item") {
					a(href = "/admiral/new") { +"Create New Admiral" }
				}
				div(classes = "item") {
					a(href = "/me/manage") { +"Edit Profile" }
				}
			}
		}
	}
}
