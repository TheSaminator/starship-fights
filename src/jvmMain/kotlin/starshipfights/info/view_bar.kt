package starshipfights.info

import kotlinx.html.*
import starshipfights.game.ShipType
import starshipfights.game.getDefiniteShortName

abstract class Sidebar {
	protected abstract fun ASIDE.display()
	fun displayIn(aside: ASIDE) = aside.display()
}

class CustomSidebar(private val block: ASIDE.() -> Unit) : Sidebar() {
	override fun ASIDE.display() = block()
}

object IndexSidebar : Sidebar() {
	override fun ASIDE.display() {
		p {
			+"This game is in early development! Report bugs and flaws as you see them, but keep in mind that this game is still being worked on."
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
