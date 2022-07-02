package net.starshipfights.labs

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import net.starshipfights.info.*

private val labs = mutableMapOf<String, String>()

private val labsSidebar: Sidebar
	get() = PageNavSidebar(
		listOf(NavHead("Other Labs")) + labs.map { (slug, title) ->
			NavLink("/labs/$slug", title)
		}
	)

fun Routing.lab(slug: String, title: String, pageBody: SECTIONS.() -> Unit) {
	labs[slug] = title
	
	get("/labs/$slug") {
		call.respondHtml(
			block = call.page(
				title,
				call.standardNavBar(),
				labsSidebar,
				pageBody
			)
		)
	}
}

fun Routing.labPost(slug: String, action: PipelineInterceptor<Unit, ApplicationCall>) {
	post("/labs/$slug", action)
}

fun Routing.installLabs() {
	get("/labs") {
		call.respondHtml(
			block = call.page(
				"Experimental Features",
				call.standardNavBar(),
				null
			) {
				section {
					h1 { +"Experimental Features" }
					p { +"Sometimes it is desirable that an in-progress feature be demonstrated to the playerbase before it is fully ready to be integrated into the game. In that case, the progress on the feature made so far will be accessible here." }
					if (labs.isEmpty())
						p { +"No labs are currently visible." }
					else
						ul {
							for ((slug, title) in labs) {
								li {
									a(href = "/labs/$slug") { +title }
								}
							}
						}
				}
			}
		)
	}
}
