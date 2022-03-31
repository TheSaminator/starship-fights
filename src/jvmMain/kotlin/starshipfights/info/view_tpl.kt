package starshipfights.info

import kotlinx.html.*

fun page(pageTitle: String? = null, navBar: List<NavItem>? = null, sidebar: Sidebar? = null, content: SECTIONS.() -> Unit): HTML.() -> Unit = {
	head {
		meta(charset = "utf-8")
		meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
		
		link(rel = "icon", type = "image/svg+xml", href = "/static/images/icon.svg")
		link(rel = "preconnect", href = "https://fonts.googleapis.com")
		link(rel = "preconnect", href = "https://fonts.gstatic.com") { attributes["crossorigin"] = "anonymous" }
		link(rel = "stylesheet", href = "https://fonts.googleapis.com/css2?family=Noto+Sans:ital,wght@0,400;0,700;1,400;1,700&family=Jetbrains+Mono:wght@400;600;800&display=swap")
		link(rel = "stylesheet", href = "/static/style.css")
		
		title {
			+"Starship Fights"
			pageTitle?.let { +" | $it" }
		}
	}
	body {
		div { id = "bg" }
		
		navBar?.let {
			nav(classes = "desktop") {
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
			aside(classes = "desktop") {
				it.displayIn(this)
			}
		}
		
		main {
			sidebar?.let {
				aside(classes = "mobile") {
					it.displayIn(this)
				}
			}
			
			with(sectioned()) {
				content()
			}
			
			navBar?.let {
				nav(classes = "mobile") {
					div(classes = "list") {
						it.forEach {
							div(classes = "item") {
								it.displayIn(this)
							}
						}
					}
				}
			}
		}
		
		script(src = "/static/init.js") {}
	}
}
