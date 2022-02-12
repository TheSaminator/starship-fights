package starshipfights.info

import io.ktor.application.*
import io.ktor.features.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.html.*
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.or
import starshipfights.CurrentConfiguration
import starshipfights.ForbiddenException
import starshipfights.auth.*
import starshipfights.data.Id
import starshipfights.data.admiralty.*
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession
import starshipfights.data.auth.UserStatus
import starshipfights.game.*
import starshipfights.redirect
import java.time.Instant

suspend fun ApplicationCall.userPage(): HTML.() -> Unit {
	val username = Id<User>(parameters["id"]!!)
	val user = User.get(username)!!
	
	val isCurrentUser = user.id == getUserSession()?.user
	val hasOpenSessions = UserSession.select(
		and(UserSession::user eq username, UserSession::expiration gt Instant.now())
	).firstOrNull() != null
	
	val admirals = Admiral.select(Admiral::owningUser eq user.id).toList()
	
	return page(
		user.profileName, standardNavBar(), CustomSidebar {
			img(src = user.discordAvatarUrl)
			p {
				style = "text-align:center"
				+user.discordName
				+"#"
				+user.discordDiscriminator
				br
				when (user.status) {
					UserStatus.IN_BATTLE -> +"In Battle"
					UserStatus.READY_FOR_BATTLE -> +"In Battle"
					UserStatus.IN_MATCHMAKING -> +"In Matchmaking"
					UserStatus.AVAILABLE -> if (hasOpenSessions) +"Online" else +"Offline"
				}
			}
			if (user.discordId == CurrentConfiguration.discordClient?.ownerId)
				p {
					style = "text-align:center;border:2px solid #a82;padding:3px;background-color:#fc3;color:#a82;font-variant:small-caps;font-family:'Orbitron',sans-serif"
					+"Site Owner"
				}
			hr { style = "border-color:#036" }
			p {
				style = "text-align:center"
				+"Registered at "
				span(classes = "moment") {
					style = "display:none"
					+user.registeredAt.toEpochMilli().toString()
				}
				br
				+"Last active at "
				span(classes = "moment") {
					style = "display:none"
					+user.lastActivity.toEpochMilli().toString()
				}
			}
			if (isCurrentUser) {
				hr { style = "border-color:#036" }
				div(classes = "list") {
					div(classes = "item") {
						a(href = "/admiral/new") { +"Create New Admiral" }
					}
					div(classes = "item") {
						a(href = "/me/manage") { +"Edit Profile" }
					}
				}
			}
		}
	) {
		section {
			h1 { +user.profileName }
			
			+user.profileBio
		}
		section {
			h2 { +"Admirals" }
			
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
				h2 {
					+"Profile"
				}
				h3 {
					label {
						htmlFor = "name"
						+"Display Name"
					}
				}
				textInput(name = "name") {
					required = true
					maxLength = "$PROFILE_NAME_MAX_LENGTH"
					
					value = currentUser.profileName
					autoComplete = false
				}
				p {
					style = "font-style:italic;font-size:0.8em;color:#555"
					+"Max length $PROFILE_NAME_MAX_LENGTH characters"
				}
				h3 {
					label {
						htmlFor = "bio"
						+"Public Bio"
					}
				}
				textArea {
					name = "bio"
					style = "width: 100%;height:5em"
					
					required = true
					maxLength = "$PROFILE_BIO_MAX_LENGTH"
					
					+currentUser.profileBio
				}
				request.queryParameters["error"]?.let { errorMsg ->
					p {
						style = "color:#d22"
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
				val now = Instant.now()
				val expiredSessions = mutableListOf<UserSession>()
				allUserSessions.forEach { session ->
					if (session.expiration.isBefore(now)) {
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
								+session.expiration.toEpochMilli().toString()
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
					maxLength = "$ADMIRAL_NAME_MAX_LENGTH"
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
			script {
				unsafe { +"window.sfAdmiralNameGen = true;" }
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
				NavLink("/user/${admiral.owningUser}", "Back to User")
			) + if (currentUser == admiral.owningUser)
				listOf(
					NavLink("/admiral/${admiral.id}/manage", "Manage Admiral")
				)
			else emptyList()
		)
	) {
		section {
			h1 { +admiral.name }
			p {
				b { +admiral.fullName }
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
							when (ship.status) {
								DrydockStatus.Ready -> +"Ready"
								is DrydockStatus.InRepair -> {
									+"Repairing"
									br
									+"Will be ready at "
									span(classes = "moment") {
										style = "display:none"
										+ship.status.until.toEpochMilli().toString()
									}
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
					th { +"Size" }
					th { +"Role" }
					th { +"Against" }
					th { +"Result" }
				}
				records.sortedBy { it.whenEnded }.forEach { record ->
					tr {
						td {
							+"Started at "
							span(classes = "moment") {
								style = "display:none"
								+record.whenStarted.toEpochMilli().toString()
							}
							br
							+"Ended at "
							span(classes = "moment") {
								style = "display:none"
								+record.whenEnded.toEpochMilli().toString()
							}
						}
						td {
							+record.battleInfo.size.displayName
							+" ("
							+record.battleInfo.size.numPoints.toString()
							+")"
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
							+when (record.winner) {
								null -> "Stalemate"
								recordRoles[record.id] -> "Victory"
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
	
	if (admiral.owningUser != currentUser) throw ForbiddenException()
	
	val ownedShips = ShipInDrydock.select(ShipInDrydock::owningAdmiral eq admiralId).toList()
	
	val buyableShips = ShipType.values().filter { type ->
		type.faction == admiral.faction && type.weightClass.rank <= admiral.rank.maxShipWeightClass.rank && type.weightClass.buyPrice <= admiral.money && (if (type.weightClass.isUnique) ownedShips.none { it.shipType.weightClass == type.weightClass } else true)
	}.sortedBy { it.name }.sortedBy { it.weightClass.rank }
	
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
					id = "name"
					autoComplete = false
					
					required = true
					value = admiral.name
					maxLength = "$ADMIRAL_NAME_MAX_LENGTH"
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
				script {
					unsafe { +"window.sfAdmiralNameGen = true;" }
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
		section {
			h2 { +"Manage Fleet" }
			p {
				+"${admiral.fullName} currently owns ${admiral.money} Electro-Ducats, and earns ${admiral.rank.dailyWage} Electro-Ducats every day."
			}
			table {
				tr {
					th { +"Ship Name" }
					th { +"Ship Class" }
					th { +"Ship Status" }
					th { +"Ship Value" }
				}
				ownedShips.sortedBy { it.name }.sortedBy { it.shipType.weightClass.rank }.forEach { ship ->
					tr {
						td {
							+ship.shipData.fullName
							br
							a(href = "/admiral/${admiralId}/rename/${ship.id}") { +"Rename" }
						}
						td {
							a(href = "/info/${ship.shipData.shipType.toUrlSlug()}") {
								+ship.shipData.shipType.fullDisplayName
							}
						}
						td {
							when (ship.status) {
								DrydockStatus.Ready -> +"Ready"
								is DrydockStatus.InRepair -> {
									+"Repairing"
									br
									+"Will be ready at "
									span(classes = "moment") {
										style = "display:none"
										+ship.status.until.toEpochMilli().toString()
									}
								}
							}
						}
						td {
							+ship.shipType.weightClass.sellPrice.toString()
							+" Electro-Ducats"
							if (ship.status == DrydockStatus.Ready && !ship.shipType.weightClass.isUnique) {
								br
								a(href = "/admiral/${admiralId}/sell/${ship.id}") { +"Sell" }
							}
						}
					}
				}
			}
			h3 { +"Buy New Ship" }
			table {
				tr {
					th { +"Ship Class" }
					th { +"Ship Cost" }
					th { +Entities.nbsp }
				}
				buyableShips.forEach { st ->
					tr {
						td { +st.fullDisplayName }
						td {
							+st.weightClass.buyPrice.toString()
							+" Electro-Ducats"
						}
						td {
							a(href = "/admiral/${admiralId}/buy/${st.toUrlSlug()}") {
								+"Buy"
							}
						}
					}
				}
			}
		}
	}
}

suspend fun ApplicationCall.renameShipPage(): HTML.() -> Unit {
	val currentUser = getUserSession()?.user
	
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val shipId = parameters["ship"]?.let { Id<ShipInDrydock>(it) }!!
	
	val (admiral, ship) = coroutineScope {
		Admiral.get(admiralId)!! to ShipInDrydock.get(shipId)!!
	}
	
	if (admiral.owningUser != currentUser) throw ForbiddenException()
	if (ship.owningAdmiral != admiralId) throw ForbiddenException()
	
	return page("Renaming Ship", null, null) {
		section {
			h1 { +"Renaming Ship" }
			p {
				+"${admiral.fullName} is about to rename the ${ship.shipData.fullName}. Choose a name here:"
			}
			form(method = FormMethod.post, action = "/admiral/${admiral.id}/rename/${ship.id}") {
				textInput(name = "name") {
					id = "name"
					
					autoComplete = false
					required = true
					
					maxLength = "$SHIP_NAME_MAX_LENGTH"
				}
				p {
					style = "font-style:italic;font-size:0.8em;color:#555"
					+"Max length $SHIP_NAME_MAX_LENGTH characters"
				}
				submitInput {
					value = "Rename"
				}
			}
			form(method = FormMethod.get, action = "/admiral/${admiral.id}/manage") {
				submitInput {
					value = "Cancel"
				}
			}
		}
	}
}

suspend fun ApplicationCall.sellShipConfirmPage(): HTML.() -> Unit {
	val currentUser = getUserSession()?.user
	
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val shipId = parameters["ship"]?.let { Id<ShipInDrydock>(it) }!!
	
	val (admiral, ship) = coroutineScope {
		Admiral.get(admiralId)!! to ShipInDrydock.get(shipId)!!
	}
	
	if (admiral.owningUser != currentUser) throw ForbiddenException()
	if (ship.owningAdmiral != admiralId) throw ForbiddenException()
	
	if (ship.status != DrydockStatus.Ready) redirect("/admiral/${admiralId}/manage")
	if (ship.shipType.weightClass.isUnique) redirect("/admiral/${admiralId}/manage")
	
	return page(
		"Are You Sure?", null, null
	) {
		section {
			h1 { +"Are You Sure?" }
			p {
				+"${admiral.fullName} is about to sell the ${ship.shipData.fullName} for ${ship.shipType.weightClass.sellPrice} Electro-Ducats."
			}
			form(method = FormMethod.get, action = "/admiral/${admiral.id}/manage") {
				submitInput {
					value = "Cancel"
				}
			}
			form(method = FormMethod.post, action = "/admiral/${admiral.id}/sell/${ship.id}") {
				submitInput {
					value = "Sell"
				}
			}
		}
	}
}

suspend fun ApplicationCall.buyShipConfirmPage(): HTML.() -> Unit {
	val currentUser = getUserSession()?.user
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val admiral = Admiral.get(admiralId)!!
	
	if (admiral.owningUser != currentUser) throw ForbiddenException()
	
	val shipType = parameters["ship"]?.let { param -> ShipType.values().singleOrNull { it.toUrlSlug() == param } }!!
	
	if (shipType.faction != admiral.faction || shipType.weightClass.rank > admiral.rank.maxShipWeightClass.rank)
		throw NotFoundException()
	
	if (shipType.weightClass.buyPrice > admiral.money) {
		return page(
			"Too Expensive", null, null
		) {
			section {
				h1 { +"Too Expensive" }
				p {
					+"Unfortunately, the ${shipType.fullDisplayName} is out of ${admiral.fullName}'s budget. It costs ${shipType.weightClass.buyPrice} Electro-Ducats, and ${admiral.name} only has ${admiral.money} Electro-Ducats."
				}
				form(method = FormMethod.get, action = "/admiral/${admiral.id}/manage") {
					submitInput {
						value = "Back"
					}
				}
			}
		}
	}
	
	return page(
		"Are You Sure?", null, null
	) {
		section {
			h1 { +"Are You Sure?" }
			p {
				+"${admiral.fullName} is about to buy a ${shipType.fullDisplayName} for ${shipType.weightClass.buyPrice} Electro-Ducats."
			}
			form(method = FormMethod.get, action = "/admiral/${admiral.id}/manage") {
				submitInput {
					value = "Cancel"
				}
			}
			form(method = FormMethod.post, action = "/admiral/${admiral.id}/buy/${shipType.toUrlSlug()}") {
				submitInput {
					value = "Checkout"
				}
			}
		}
	}
}

suspend fun ApplicationCall.deleteAdmiralConfirmPage(): HTML.() -> Unit {
	val currentUser = getUserSession()?.user
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val admiral = Admiral.get(admiralId)!!
	
	if (admiral.owningUser != currentUser) throw ForbiddenException()
	
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
