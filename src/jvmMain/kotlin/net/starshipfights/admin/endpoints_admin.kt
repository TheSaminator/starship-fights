package net.starshipfights.admin

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.Job
import kotlinx.html.*
import net.starshipfights.auth.getUser
import net.starshipfights.auth.getUserAndSession
import net.starshipfights.auth.receiveValidatedParameters
import net.starshipfights.forbid
import net.starshipfights.info.csrfToken
import net.starshipfights.info.page
import net.starshipfights.info.standardNavBar
import net.starshipfights.redirect

private val shutDown = Job()

fun Routing.installAdmin() {
	get("/admin") {
		val (sess, user) = call.getUserAndSession()
		
		if (!user.isAdmin)
			forbid()
		
		sess ?: redirect("/login")
		user ?: redirect("/login")
		
		call.respondHtml(HttpStatusCode.OK, call.page("Admin Panel", call.standardNavBar()) {
			section {
				h1 { +"Admin Panel" }
			}
			section {
				h2 { +"Public Announcements" }
				form(action = "/admin/announce", method = FormMethod.post) {
					textInput {
						name = "announcement"
						required = true
					}
					csrfToken(sess.id)
					submitInput {
						value = "Announce"
					}
				}
			}
		})
	}
	
	get("/admin/shutdown") {
		if (!call.getUser().isAdmin)
			forbid()
		
		shutDown.complete()
		
		call.respond(HttpStatusCode.Gone)
	}
	
	post("/admin/announce") {
		val user = call.getUser()
		user ?: redirect("/login")
		
		if (!user.isAdmin)
			forbid()
		
		val params = call.receiveValidatedParameters()
		val announcement = params.getOrFail("announcement")
		sendAdminAnnouncement(announcement)
		redirect("/admin")
	}
}

suspend fun awaitShutDown() = shutDown.join()
