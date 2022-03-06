package starshipfights.info

import kotlinx.html.*

fun page(pageTitle: String? = null, navBar: List<NavItem>? = null, sidebar: Sidebar? = null, content: MAIN.() -> Unit): HTML.() -> Unit = {
	head {
		meta(charset = "utf-8")
		
		link(rel = "icon", type = "image/svg+xml", href = "/static/images/icon.svg")
		link(rel = "preconnect", href = "https://fonts.googleapis.com")
		link(rel = "preconnect", href = "https://fonts.gstatic.com") { attributes["crossorigin"] = "anonymous" }
		link(rel = "stylesheet", href = "https://fonts.googleapis.com/css2?family=Noto+Sans:ital,wght@0,400;0,700;1,400;1,700&family=Orbitron:wght@500;700;900&display=swap")
		link(rel = "stylesheet", href = "/static/style.css")
		
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
