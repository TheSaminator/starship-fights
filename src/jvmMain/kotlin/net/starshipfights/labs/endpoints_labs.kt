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
			NavLink(labUrl(slug), title)
		}
	)

fun labUrl(slug: String) = "/labs/$slug"

fun Routing.lab(slug: String, title: String, pageBody: SECTIONS.(errorMessage: String?) -> Unit) {
	labs[slug] = title
	
	get(labUrl(slug)) {
		call.respondHtml(
			block = call.page(
				title,
				call.standardNavBar(),
				labsSidebar,
			) { pageBody(call.request.queryParameters["error"]) }
		)
	}
}

fun Routing.labPost(slug: String, action: PipelineInterceptor<Unit, ApplicationCall>) {
	post(labUrl(slug), action)
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
									a(href = labUrl(slug)) { +title }
								}
							}
						}
				}
			}
		)
	}
}
