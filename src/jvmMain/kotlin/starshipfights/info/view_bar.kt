package starshipfights.info

import kotlinx.html.*
import starshipfights.data.auth.User
import starshipfights.game.ShipType
import starshipfights.game.getDefiniteShortName

abstract class Sidebar {
	protected abstract fun ASIDE.display()
	fun displayIn(aside: ASIDE) = aside.display()
}

class CustomSidebar(private val block: ASIDE.() -> Unit) : Sidebar() {
	override fun ASIDE.display() = block()
}

data class IndexSidebar(val madeBy: User) : Sidebar() {
	override fun ASIDE.display() {
		p {
			style = "text-align:center"
			+"Starship Fights is made by"
		}
		img(src = madeBy.discordAvatarUrl) {
			style = "border-radius:50%"
		}
		p {
			style = "text-align:center"
			+madeBy.discordName
			+"#"
			+madeBy.discordDiscriminator
		}
	}
}

data class ShipViewSidebar(val shipType: ShipType) : Sidebar() {
	override fun ASIDE.display() {
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
	override fun ASIDE.display() {
		div(classes = "list") {
			contents.forEach {
				div(classes = "item") {
					it.displayIn(this)
				}
			}
		}
	}
}
