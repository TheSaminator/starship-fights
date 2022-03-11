package starshipfights.info

import io.ktor.application.*
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.html.*
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import starshipfights.CurrentConfiguration
import starshipfights.data.auth.User

suspend fun ApplicationCall.mainPage(): HTML.() -> Unit {
	return page(null, standardNavBar(), null) {
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
				+", Starship Fights is about the grand struggle between four major political powers. Fight for liberty and justice with the Empire of Mechyrdia, conquer for glory and honor with the Diadochus Masra Draetsen, preserve your homeland and decide its fate with the Isarnareyksk Federation, or reclaim your people's rightful dominion with the American Vestigium!"
			}
		}
	}
}

suspend fun ApplicationCall.aboutPage(): HTML.() -> Unit {
	val owner = CurrentConfiguration.discordClient?.ownerId?.let {
		User.locate(User::discordId eq it)
	} ?: return page(
		"About", standardNavBar(), null
	) {
		section {
			h1 { +"In Development" }
			p {
				+"This is a test instance of Starship Fights."
			}
		}
	}
	
	return page(
		"About", standardNavBar(), PageNavSidebar(
			listOf(
				NavHead("Useful Links"),
				NavLink("/about/pp", "Privacy Policy"),
				NavLink("/about/tnc", "Terms and Conditions"),
			)
		)
	) {
		section {
			h1 { +"About Starship Fights" }
			p {
				+"Starship Fights is designed and programmed by the person behind "
				a(href = "https://nationstates.net/mechyrdia") { +"Mechyrdia" }
				+". He can be reached by telegram on NationStates, or by his "
				a(href = "/user/${owner.id}") { +"account on this site" }
				+"."
			}
		}
	}
}

suspend fun ApplicationCall.privacyPolicyPage(): HTML.() -> Unit {
	return page(
		"Privacy Policy", standardNavBar(), PageNavSidebar(
			listOf(
				NavHead("Useful Links"),
				NavLink("/about", "About Starship Fights"),
				NavLink("/about/tnc", "Terms and Conditions"),
			)
		)
	) {
		section {
			h1 { +"Privacy Policy" }
			h2 { +"What Data Do We Collect" }
			p { +"Starship Fights does not collect very much personal data; the only data it collects is relevant to either user authentication or user authorization. The following data is collected by the game:" }
			dl {
				dt { +"Discord ID" }
				dd { +"This is needed to keep your Starship Fights user account associated with your Discord login, so that you can keep your admirals and ships when you log in." }
				dt { +"Discord Profile Data (Name, Discriminator, Avatar)" }
				dd {
					+"This is kept so that you have the option of showing what your Discord account is on your profile page. It's optional to display to other users, with the choice being in the "
					a(href = "/me/manage") { +"User Preferences" }
					+" page. Note that we do "
					strong { +"not" }
					+" request or track email addresses."
				}
				dt { +"Your browser's User-Agent" }
				dd {
					+"This is associated with your session data as a layer of security, so that if someone were to (somehow) steal your session token and put it into their browser, that person wouldn't be logged in as you, since the User-Agent would probably be different."
				}
				dt { +"Your public-facing IP address (opt-in)" }
				dd {
					+"This is associated with your sessions, so that it may be displayed to you when you look at your currently logged-in sessions on your "
					a(href = "/me/manage") { +"User Preferences" }
					+" page, so that you can log out of a session if you don't recognize its IP address. You may opt in to the site's collection and storage of your IP address on that same page."
				}
				dt { +"The date and time of your last activity" }
				dd {
					+"This is associated with your user account as a whole, so that your Online/Offline status can be displayed. It's optional to display your current status, and the choice is in your "
					a(href = "/me/manage") { +"User Preferences" }
					+" page."
				}
			}
			h2 { +"How Do We Collect It" }
			p {
				+"Your Discord information is collected using the Discord API whenever you log in via Discord's OAuth2. Your User-Agent and IP address are collected using the HTTP requests that your browser sends to the website, and the date and time of your last activity is tracked using the server's system clock."
			}
			h2 { +"Who Can See It" }
			p {
				+"The only people who can see the data we collect are you and the system administrator. We do not sell data to advertisers. The site is hosted on "
				a(href = "https://hetzner.com/") { +"Hetzner Cloud" }
				+", who can "
				em { +"in theory" }
				+" access it."
			}
			p {
				+"Privacy policies are nice and all, but they're only as strong as the staff that implements them. I have no interest in abusing others, just as I have no interest in doxing or otherwise revealing what locations people log in from. Nor have I any interest in being worshipped as some kind of programmer-god messiah. I am impervious to such corrupting ambitions."
			}
			h2 { +"Who Can't See It" }
			p {
				+"We protect your data by a combination of requiring TLS-secured HTTP connections, and keeping the database's port only open on 127.0.0.1, i.e. no one outside of the server's local machine can even connect to the database, much less access the data stored inside of it."
			}
			h2 { +"When Was This Written" }
			dl {
				dt { +"February 13, 2022" }
				dd { +"Initial writing" }
				dt { +"February 15, 2022" }
				dd { +"Indicate that IP storage is an opt-in-only feature" }
			}
		}
	}
}

suspend fun ApplicationCall.termsAndConditionsPage(): HTML.() -> Unit {
	val ownerDiscordUsername = CurrentConfiguration.discordClient?.ownerId?.let {
		User.locate(User::discordId eq it)
	}?.let { "${it.discordName}#${it.discordDiscriminator}" }
	
	return page(
		"Terms and Conditions", standardNavBar(), PageNavSidebar(
			listOf(
				NavHead("Useful Links"),
				NavLink("/about", "About Starship Fights"),
				NavLink("/about/pp", "Privacy Policy"),
			)
		)
	) {
		section {
			h1 { +"Terms And Conditions" }
			h2 { +"Section I - Privacy Policy" }
			p {
				+"By agreeing to these Terms and Conditions, you confirm that you have read and acknowledged the Privacy Policy of Starship Fights, accessible at "
				a(href = "https://starshipfights.net/about/pp") { +"https://starshipfights.net/about/pp" }
				+"."
			}
			h2 { +"Section II - Limitation of Liability" }
			p {
				+"UNDER NO CIRCUMSTANCES will Starship Fights be liable or responsible to either its users or any third party for any damages or injuries sustained as a result of using this website."
			}
			h2 { +"Section III - Termination" }
			p {
				+"Starship Fights may terminate your usage if:"
			}
			ol {
				li { +"You are in breach of these Terms and Conditions." }
				li { +"You, at any point, inflict abuse upon the website, including but not limited to: DDoS attacks, vulnerability scanning, vulnerability exploitation, etc." }
				li { +"For any reason, at our sole discretion." }
			}
			h2 { +"Section IV - Amendment Process" }
			p {
				+"Starship Fights will notify users when amendments to the Terms and Conditions will impact their usage of their site."
				CurrentConfiguration.discordClient?.serverInvite?.let { invite ->
					+" Users will be notified via the "
					a(href = "https://discord.gg/$invite") { +"Starship Fights Discord server" }
					+"."
				}
			}
			h2 { +"Section V - Amendments" }
			dl {
				dt { +"March 11, 2022" }
				dd { +"Initial writing" }
			}
			ownerDiscordUsername?.let {
				h2 { +"Section VI - Contact" }
				p {
					+"The operator of Starship Fights may be contacted via Discord at $it, or via telegram to "
					a(href = "https://nationstates.net/mechyrdia") { +"his NationStates account" }
					+"."
				}
			}
		}
	}
}

suspend fun ApplicationCall.newUsersPage(): HTML.() -> Unit {
	val newUsers = User.sorted(descending(User::registeredAt)).take(20).toList()
	
	return page("New Users", standardNavBar()) {
		section {
			h1 { +"New Users" }
			div {
				style = "text-align:center"
				newUsers.forEach { newUser ->
					div {
						style = "display:inline-block;width:20%;padding:2%"
						a(href = "/user/${newUser.id}") {
							img(src = newUser.discordAvatarUrl) {
								style = "width:100%;border-radius:50%"
							}
						}
						p {
							style = "text-align:center"
							a(href = "/user/${newUser.id}") {
								+newUser.profileName
							}
						}
					}
				}
			}
		}
	}
}
