package starshipfights.info

import io.ktor.application.*
import io.ktor.util.*
import kotlinx.html.*

fun ApplicationCall.page(pageTitle: String? = null, navBar: List<NavItem>? = null, sidebar: Sidebar? = null, pageData: PageMetadata = PageMetadata.default, content: MAIN.() -> Unit): HTML.() -> Unit = {
	head {
		meta(charset = "utf-8")
		
		metadata(pageData, url { host = "starshipfights.net" })
		
		link(rel = "icon", type = "image/svg+xml", href = "/static/images/icon.svg")
		link(rel = "preconnect", href = "https://fonts.googleapis.com")
		link(rel = "preconnect", href = "https://fonts.gstatic.com") { attributes["crossorigin"] = "anonymous" }
		link(rel = "stylesheet", href = "https://fonts.googleapis.com/css2?family=Noto+Sans:ital,wght@0,400;0,700;1,400;1,700&family=Orbitron:wght@500;700;900&display=swap")
		link(rel = "stylesheet", href = "/static/style.css")
		
		script(src = "/static/game/three.js") {}
		script(src = "/static/game/three-examples.js") {}
		script(src = "/static/game/three-extras.js") {}
		
		title {
			+"Starship Fights"
			pageTitle?.let { +" | $it" }
		}
	}
	body {
		div { id = "bg" }
		
		navBar?.let {
			nav {
				div(classes = "list") {
					it.forEach {
						div(classes = "item") {
							it.displayIn(this)
						}
					}
				}
			}
		}
		
		sidebar?.let {
			aside {
				it.displayIn(this)
			}
		}
		
		main {
			content()
		}
		
		script(src = "/static/init.js") {}
	}
}
