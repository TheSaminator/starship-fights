package net.starshipfights.info

import kotlinx.html.*
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