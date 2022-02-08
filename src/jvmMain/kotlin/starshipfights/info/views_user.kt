package starshipfights.info

import io.ktor.application.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.html.*
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.or
import starshipfights.auth.getUser
import starshipfights.auth.getUserSession
import starshipfights.data.Id
import starshipfights.data.admiralty.*
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession
import starshipfights.data.auth.usernameRegexStr
import starshipfights.data.auth.usernameTooltip
import starshipfights.game.Faction
import starshipfights.game.GlobalSide
import starshipfights.game.toUrlSlug
import starshipfights.redirect
import java.time.Instant

suspend fun ApplicationCall.userPage(): HTML.() -> Unit {
	val username = parameters["name"]!!
	val user = User.locate(User::username eq username)!!
	
	val isCurrentUser = user.id == getUserSession()?.user
	
	val admirals = Admiral.select(Admiral::owningUser eq user.id).toList()
	
	return page(
		username, standardNavBar(), if (isCurrentUser)
			PageNavSidebar(
				listOf(
					NavLink("/admiral/new", "New Admiral"),
				)
			)
		else
			CustomSidebar {
			
			}
	) {
		section {
			h1 { +username }
			p {
				+"This user's username is $username!"
			}
			
			if (isCurrentUser)
				p {
					+"This user is you!"
				}
			
			if (admirals.isNotEmpty()) {
				p {
					+"This user has the following admirals:"
				}
				ul {
					admirals.forEach { admiral ->
						li {
							a("/admiral/${admiral.id}") { +admiral.fullName }
						}
					}
				}
			} else
				p {
					+"This user has no admirals."
				}
		}
	}
}

suspend fun ApplicationCall.manageUserPage(): HTML.() -> Unit {
	val currentSession = getUserSession() ?: redirect("/login")
	val currentUser = User.get(currentSession.user) ?: redirect("/login")
	val allUserSessions = UserSession.select(and(UserSession::user eq currentUser.id)).toList()
	
	return page(
		"User Preferences", standardNavBar(), PageNavSidebar(
			listOf(
				NavLink("/me", "Back to User Page")
			)
		)
	) {
		section {
			h1 { +"User Preferences" }
			form(method = FormMethod.post, action = "/me/manage") {
				h3 {
					label {
						htmlFor = "username"
						+"Username"
					}
				}
				textInput(name = "name") {
					required = true
					value = currentUser.username
					autoComplete = false
					
					required = true
					minLength = "2"
					maxLength = "32"
					title = usernameTooltip
					pattern = usernameRegexStr
				}
				request.queryParameters["error"]?.let { errorMsg ->
					p {
						style = "color:#d33"
						+errorMsg
					}
				}
				submitInput {
					value = "Accept Changes"
				}
			}
		}
		section {
			h1 { +"Other Sessions" }
			table {
				tr {
					th { +"User-Agent" }
					th { +"Client IPs" }
					th { +Entities.nbsp }
				}
				val now = System.currentTimeMillis()
				val expiredSessions = mutableListOf<UserSession>()
				allUserSessions.forEach { session ->
					if (session.expirationMillis < now) {
						expiredSessions += session
						return@forEach
					}
					
					tr {
						td { +session.userAgent }
						td {
							session.clientAddresses.forEachIndexed { i, clientAddress ->
								if (i != 0) br
								+clientAddress
							}
						}
						td {
							if (session.id == currentSession.id) {
								+"Current Session"
								br
							}
							a(href = "/logout/${session.id}") { +"Logout" }
						}
					}
				}
				tr {
					td {
						colSpan = "3"
						a(href = "/logout-all") { +"Logout All" }
					}
				}
				expiredSessions.forEach { session ->
					tr {
						td { +session.userAgent }
						td {
							session.clientAddresses.forEachIndexed { i, clientAddress ->
								if (i != 0) br
								+clientAddress
							}
						}
						td {
							+"Expired at "
							span(classes = "moment") {
								style = "display:none"
								+session.expirationMillis.toString()
							}
						}
					}
				}
			}
		}
	}
}

suspend fun ApplicationCall.createAdmiralPage(): HTML.() -> Unit {
	getUser() ?: redirect("/login")
	
	return page(
		"Creating Admiral", standardNavBar(), null
	) {
		section {
			h1 { +"Creating Admiral" }
			form(method = FormMethod.post, action = "/admiral/new") {
				h3 {
					label {
						htmlFor = "faction"
						+"Faction"
					}
				}
				p {
					Faction.values().forEach { faction ->
						val factionId = "faction-${faction.toUrlSlug()}"
						label {
							htmlFor = factionId
							radioInput(name = "faction") {
								id = factionId
								value = faction.name
								required = true
							}
							img(src = faction.flagUrl) {
								style = "height:0.75em;width:1.2em"
							}
							+Entities.nbsp
							+faction.shortName
						}
					}
				}
				h3 {
					label {
						htmlFor = "name"
						+"Name"
					}
				}
				textInput(name = "name") {
					id = "name"
					autoComplete = false
					required = true
					minLength = "2"
					maxLength = "32"
				}
				p {
					label {
						htmlFor = "sex-male"
						radioInput(name = "sex") {
							id = "sex-male"
							value = "male"
							required = true
						}
						+"Male"
					}
					label {
						htmlFor = "sex-female"
						radioInput(name = "sex") {
							id = "sex-female"
							value = "female"
							required = true
						}
						+"Female"
					}
				}
				h3 { +"Generate Random Name" }
				p {
					AdmiralNameFlavor.values().forEachIndexed { i, flavor ->
						if (i != 0)
							br
						a(href = "#", classes = "generate-admiral-name") {
							attributes["data-flavor"] = flavor.toUrlSlug()
							+flavor.displayName
						}
					}
				}
				submitInput {
					value = "Create Admiral"
				}
			}
		}
	}
}

suspend fun ApplicationCall.admiralPage(): HTML.() -> Unit {
	val currentUser = getUserSession()?.user
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val (admiral, ships, records) = coroutineScope {
		val admiral = async { Admiral.get(admiralId)!! }
		val ships = async { ShipInDrydock.select(ShipInDrydock::owningAdmiral eq admiralId).toList() }
		val records = async { BattleRecord.select(or(BattleRecord::hostAdmiral eq admiralId, BattleRecord::guestAdmiral eq admiralId)).toList() }
		
		Triple(admiral.await(), ships.await(), records.await())
	}
	val admiralOwner = User.get(admiral.owningUser)!!.username
	
	val recordRoles = records.mapNotNull {
		when (admiralId) {
			it.hostAdmiral -> GlobalSide.HOST
			it.guestAdmiral -> GlobalSide.GUEST
			else -> null
		}?.let { role -> it.id to role }
	}.toMap()
	
	val recordOpponents = coroutineScope {
		records.mapNotNull {
			recordRoles[it.id]?.let { role ->
				val aId = when (role) {
					GlobalSide.HOST -> it.guestAdmiral
					GlobalSide.GUEST -> it.hostAdmiral
				}
				it.id to async { Admiral.get(aId) }
			}
		}.mapNotNull { (id, deferred) ->
			deferred.await()?.let { id to it }
		}.toMap()
	}
	
	return page(
		admiral.fullName, standardNavBar(), PageNavSidebar(
			listOf(
				NavLink("/user/${admiralOwner}", "Back to User")
			) + if (currentUser == admiral.owningUser)
				listOf(
					NavLink("/admiral/${admiral.id}/manage", "Manage Admiral")
				)
			else emptyList()
		)
	) {
		section {
			h1 { +admiral.fullName }
			p {
				+admiral.fullName
				+" is a flag officer of the "
				+admiral.faction.navyName
				+". "
				+(if (admiral.isFemale) "She" else "He")
				+" controls the following ships:"
			}
			
			table {
				tr {
					th { +"Ship Name" }
					th { +"Ship Class" }
					th { +"Ship Status" }
				}
				ships.sortedBy { it.name }.sortedBy { it.shipType.weightClass.rank }.forEach { ship ->
					tr {
						td { +ship.shipData.fullName }
						td {
							a(href = "/info/${ship.shipData.shipType.toUrlSlug()}") {
								+ship.shipData.shipType.fullDisplayName
							}
						}
						td {
							val now = Instant.now()
							+when (ship.status) {
								DrydockStatus.Ready -> "Ready"
								is DrydockStatus.InRepair -> {
									val distance = (ship.status.until.epochSecond - now.epochSecond) / 3600 + 1
									"Repairing (ready in ${distance}h)"
								}
							}
						}
					}
				}
			}
		}
		section {
			h2 { +"Valor" }
			p {
				+"This admiral has fought in the following battles:"
			}
			table {
				tr {
					th { +"When" }
					th { +"Role" }
					th { +"Against" }
					th { +"Result" }
				}
				records.sortedBy { it.whenEnded }.forEach { record ->
					tr {
						td {
							span(classes = "moment") {
								style = "display:none"
								+record.whenEnded.toEpochMilli().toString()
							}
						}
						td {
							+when (recordRoles[record.id]) {
								GlobalSide.HOST -> "Host"
								GlobalSide.GUEST -> "Guest"
								else -> "N/A"
							}
						}
						td {
							val opponent = recordOpponents[record.id]
							if (opponent == null)
								+"N/A"
							else
								a(href = "/admiral/${opponent.id}") {
									+opponent.fullName
								}
						}
						td {
							+when (recordRoles[record.id]) {
								record.winner -> "Victory"
								else -> "Defeat"
							}
						}
					}
				}
			}
		}
	}
}

suspend fun ApplicationCall.manageAdmiralPage(): HTML.() -> Unit {
	val currentUser = getUserSession()?.user
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val admiral = Admiral.get(admiralId)!!
	
	if (admiral.owningUser != currentUser) throw IllegalArgumentException()
	
	return page(
		"Managing ${admiral.name}", standardNavBar(), PageNavSidebar(
			listOf(
				NavLink("/admiral/${admiral.id}", "Back to Admiral")
			)
		)
	) {
		section {
			h1 { +"Managing ${admiral.name}" }
			form(method = FormMethod.post, action = "/admiral/${admiral.id}/manage") {
				h3 {
					label {
						htmlFor = "name"
						+"Name"
					}
				}
				textInput(name = "name") {
					required = true
					value = admiral.name
					minLength = "4"
					maxLength = "24"
				}
				p {
					label {
						htmlFor = "sex-male"
						radioInput(name = "sex") {
							id = "sex-male"
							value = "male"
							required = true
							checked = !admiral.isFemale
						}
						+"Male"
					}
					label {
						htmlFor = "sex-female"
						radioInput(name = "sex") {
							id = "sex-female"
							value = "female"
							required = true
							checked = admiral.isFemale
						}
						+"Female"
					}
				}
				submitInput {
					value = "Submit Changes"
				}
			}
			form(method = FormMethod.get, action = "/admiral/${admiral.id}/delete") {
				submitInput(classes = "evil") {
					value = "Delete this Admiral"
				}
			}
		}
	}
}

suspend fun ApplicationCall.deleteAdmiralConfirmPage(): HTML.() -> Unit {
	val currentUser = getUserSession()?.user
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val admiral = Admiral.get(admiralId)!!
	
	if (admiral.owningUser != currentUser) throw IllegalArgumentException()
	
	return page(
		"Are You Sure?", null, null
	) {
		section {
			h1 { +"Are You Sure?" }
			p {
				+"Are you sure you want to delete "
				+admiral.fullName
				+"? Deletion cannot be undone!"
			}
			form(method = FormMethod.get, action = "/admiral/${admiral.id}/manage") {
				submitInput {
					value = "No"
				}
			}
			form(method = FormMethod.post, action = "/admiral/${admiral.id}/delete") {
				submitInput(classes = "evil") {
					value = "Yes"
				}
			}
		}
	}
}
