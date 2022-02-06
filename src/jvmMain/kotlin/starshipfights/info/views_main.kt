package starshipfights.info

import io.ktor.application.*
import kotlinx.html.*

suspend fun ApplicationCall.mainPage(): HTML.() -> Unit {
	return page(null, standardNavBar(), IndexSidebar) {
		section {
			img(alt = "Starship Fights Logo", src = "/static/images/logo.svg") {
				style = "width:100%"
			}
			p {
				+"Starship Fights is a space fleet battle game. Choose your allegiance, create your admiral, build up your fleet, and destroy your enemies' fleets with it. You might, on occasion, get destroyed by your enemies; that's entirely normal, and all a part of learning."
			}
			p {
				+"Set in the galaxy-wide "
				a(href = "https://nationstates.net/mechyrdia") { +"Mechyrdiaverse" }
				+", Starship Fights is about the grand struggle between four major political powers. Fight for liberty and justice with the Empire of Mechyrdia, conquer for glory and honor with the Diadochus Masra Draetsen, preserve your homeland and decide its fate with the Isarnareyksk Iunta, or reclaim your people's rightful dominion with the American Vestigium!"
			}
		}
	}
}

suspend fun ApplicationCall.aboutPage(): HTML.() -> Unit = page("About", standardNavBar(), IndexSidebar) {
	section {
		img(alt = "Starship Fights Logo", src = "/static/images/logo.svg") {
			style = "width:100%"
		}
		p {
			+"Starship Fights is designed and programmed by the person behind "
			a(href = "https://nationstates.net/mechyrdia") { +"Mechyrdia" }
			+". He can be reached by telegram on NationStates, or by his "
			a(href = "https://discord.id/?prefill=307880116715913217") { +"Discord account" }
			+"."
		}
	}
}
