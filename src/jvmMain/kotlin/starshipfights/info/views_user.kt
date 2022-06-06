package starshipfights.info

import io.ktor.application.*
import io.ktor.features.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.html.*
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.or
import starshipfights.auth.*
import starshipfights.data.Id
import starshipfights.data.admiralty.*
import starshipfights.data.auth.*
import starshipfights.forbid
import starshipfights.game.*
import starshipfights.redirect
import java.time.Instant

suspend fun ApplicationCall.userPage(): HTML.() -> Unit {
	val userId = Id<User>(parameters["id"]!!)
	val user = User.get(userId)!!
	val currentUser = getUserSession()
	
	val isCurrentUser = user.id == currentUser?.user
	val hasOpenSessions = UserSession.locate(
		and(UserSession::user eq userId, UserSession::expiration gt Instant.now())
	) != null
	
	val admirals = Admiral.filter(Admiral::owningUser eq user.id).toList()
	
	return page(
		user.profileName, standardNavBar(), CustomSidebar {
			if (user.showDiscordName) {
				img(src = user.discordAvatarUrl) {
					style = "border-radius:50%"
				}
				p {
					style = "text-align:center"
					+user.discordName
					+"#"
					+user.discordDiscriminator
				}
			} else {
				img(src = user.anonymousAvatarUrl) {
					style = "border-radius:50%"
				}
			}
			for (trophy in user.getTrophies())
				renderTrophy(trophy)
			
			if (user.showUserStatus) {
				p {
					style = "text-align:center"
					+when (user.status) {
						UserStatus.IN_BATTLE -> "In Battle"
						UserStatus.READY_FOR_BATTLE -> "In Battle"
						UserStatus.IN_MATCHMAKING -> "In Matchmaking"
						UserStatus.AVAILABLE -> if (hasOpenSessions) "Online" else "Offline"
					}
				}
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
			} /*else if (currentUser != null) {
				hr { style = "border-color:#036" }
				div(classes = "list") {
					div(classes = "item") {
						a(href = "/user/${userId}/send") { +"Send Message" }
					}
				}
			}*/
		}
	) {
		section {
			h1 { +user.profileName }
			
			for (paragraph in user.profileBio.split('\n'))
				p { +paragraph }
		}
		section {
			h2 { +"Admirals" }
			
			if (admirals.isNotEmpty()) {
				p {
					+"This user has the following admirals:"
				}
				ul {
					for (admiral in admirals.sortedBy { it.name }.sortedBy { it.rank }.sortedBy { it.faction }) {
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
	val allUserSessions = UserSession.filter(and(UserSession::user eq currentUser.id)).toList()
	
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
				csrfToken(currentSession.id)
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
				h3 {
					+"Display Theme"
				}
				p {
					+"Clicking one of the options here will preview the selected theme. It is still necessary to click Accept Changes to keep your choice of theme."
				}
				label {
					radioInput(name = "theme") {
						id = "system-theme"
						value = "system"
						required = true
						checked = currentUser.preferredTheme == PreferredTheme.SYSTEM
					}
					+Entities.nbsp
					+"System Choice"
				}
				br
				label {
					radioInput(name = "theme") {
						id = "light-theme"
						value = "light"
						required = true
						checked = currentUser.preferredTheme == PreferredTheme.LIGHT
					}
					+Entities.nbsp
					+"Light Theme"
				}
				br
				label {
					radioInput(name = "theme") {
						id = "dark-theme"
						value = "dark"
						required = true
						checked = currentUser.preferredTheme == PreferredTheme.DARK
					}
					+Entities.nbsp
					+"Dark Theme"
				}
				h3 {
					+"Privacy Settings"
				}
				label {
					checkBoxInput {
						name = "showdiscord"
						checked = currentUser.showDiscordName
						value = "yes"
					}
					+Entities.nbsp
					+"Show Discord name"
				}
				br
				label {
					checkBoxInput {
						name = "showstatus"
						checked = currentUser.showUserStatus
						value = "yes"
					}
					+Entities.nbsp
					+"Show Online Status"
				}
				br
				label {
					checkBoxInput {
						name = "logaddress"
						checked = currentUser.logIpAddresses
						value = "yes"
					}
					+Entities.nbsp
					+"Log Session IP Addresses"
				}
				p {
					+"Your private info can be viewed at the "
					a(href = "/me/private-info") { +"Private Info" }
					+" page."
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
			script {
				unsafe { +"window.sfThemeChoice = true;" }
			}
		}
		section {
			h2 { +"Logged-In Sessions" }
			table {
				tr {
					th { +"User-Agent" }
					if (currentUser.logIpAddresses)
						th { +"Client IPs" }
					th { +Entities.nbsp }
				}
				val now = Instant.now()
				val expiredSessions = mutableListOf<UserSession>()
				for (session in allUserSessions) {
					if (session.expiration < now) {
						expiredSessions += session
						continue
					}
					
					tr {
						td { +session.userAgent }
						if (currentUser.logIpAddresses)
							td {
								for ((i, clientAddress) in session.clientAddresses.withIndex()) {
									if (i != 0) br
									+clientAddress
								}
							}
						td {
							if (session.id == currentSession.id) {
								+"Current Session"
								br
							}
							a(href = "/logout/${session.id}") {
								method = "post"
								csrfToken(currentSession.id)
								+"Logout"
							}
						}
					}
				}
				tr {
					td {
						colSpan = if (currentUser.logIpAddresses) "3" else "2"
						a(href = "/logout-all") {
							method = "post"
							csrfToken(currentSession.id)
							+"Logout All Other Sessions"
						}
					}
				}
				for (session in expiredSessions) {
					tr {
						td { +session.userAgent }
						if (currentUser.logIpAddresses)
							td {
								for ((i, clientAddress) in session.clientAddresses.withIndex()) {
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
							br
							a(href = "/clear-expired/${session.id}") {
								method = "post"
								csrfToken(currentSession.id)
								+"Clear"
							}
						}
					}
				}
				if (expiredSessions.isNotEmpty())
					tr {
						td {
							colSpan = if (currentUser.logIpAddresses) "3" else "2"
							a(href = "/clear-all-expired") {
								method = "post"
								csrfToken(currentSession.id)
								+"Clear All Expired Sessions"
							}
						}
					}
			}
		}
	}
}

suspend fun ApplicationCall.createAdmiralPage(): HTML.() -> Unit {
	val sessionId = getUserSession()?.id ?: redirect("/login")
	
	return page(
		"Creating Admiral", standardNavBar(), null
	) {
		section {
			h1 { +"Creating Admiral" }
			form(method = FormMethod.post, action = "/admiral/new") {
				csrfToken(sessionId)
				h3 {
					label {
						htmlFor = "faction"
						+"Faction"
					}
				}
				p {
					for (faction in Faction.values()) {
						val factionId = "faction-${faction.toUrlSlug()}"
						label {
							htmlFor = factionId
							radioInput(name = "faction") {
								id = factionId
								value = faction.name
								required = true
								if (faction == Faction.FELINAE_FELICES)
									attributes["data-force-gender"] = "female"
							}
							img(src = faction.flagUrl) {
								style = "height:0.75em;width:1.2em"
							}
							+Entities.nbsp
							+faction.shortName
						}
						br
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
					for ((i, flavor) in AdmiralNameFlavor.values().withIndex()) {
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
				unsafe { +"window.sfAdmiralNameGen = true; window.sfFactionSelect = true;" }
			}
		}
	}
}

suspend fun ApplicationCall.admiralPage(): HTML.() -> Unit {
	val currentUser = getUserSession()?.user
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val admiral = Admiral.get(admiralId)!!
	val (ships, graveyard, records) = coroutineScope {
		val ships = async { ShipInDrydock.filter(ShipInDrydock::owningAdmiral eq admiralId).toList() }
		val graveyard = async { ShipMemorial.filter(ShipMemorial::owningAdmiral eq admiralId).toList() }
		val records = async { BattleRecord.filter(or(BattleRecord::hostAdmiral eq admiralId, BattleRecord::guestAdmiral eq admiralId)).toList() }
		
		Triple(ships.await(), graveyard.await(), records.await())
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
				
				val now = Instant.now()
				for (ship in ships.sortedBy { it.name }.sortedBy { it.shipType.weightClass.tier }) {
					tr {
						td { +ship.shipData.fullName }
						td {
							a(href = "/info/${ship.shipData.shipType.toUrlSlug()}") {
								+ship.shipData.shipType.fullDisplayName
							}
						}
						td {
							val shipReadyAt = ship.readyAt
							if (shipReadyAt <= now) {
								+"Ready"
								br
								+"(since "
								span(classes = "moment") {
									style = "display:none"
									+shipReadyAt.toEpochMilli().toString()
								}
								+")"
							} else {
								+"Will be ready at "
								span(classes = "moment") {
									style = "display:none"
									+shipReadyAt.toEpochMilli().toString()
								}
							}
						}
					}
				}
			}
			
			p {
				+"The following ships were lost under "
				+(if (admiral.isFemale) "her" else "his")
				+" command:"
			}
			table {
				tr {
					th { +"Ship Name" }
					th { +"Ship Class" }
					th { +"Destruction" }
				}
				
				for (ship in graveyard.sortedBy { it.name }.sortedBy { it.shipType.weightClass.tier }) {
					tr {
						td { +ship.fullName }
						td {
							a(href = "/info/${ship.shipType.toUrlSlug()}") {
								+ship.shipType.fullDisplayName
							}
						}
						td {
							+"Destroyed by "
							val opponent = recordOpponents[ship.destroyedIn]
							if (opponent == null)
								i { +"(Deleted Admiral)" }
							else
								a(href = "/admiral/${opponent.id}") {
									+opponent.fullName
								}
							br
							br
							+"Destroyed at "
							span(classes = "moment") {
								style = "display:none"
								+ship.destroyedAt.toEpochMilli().toString()
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
				for (record in records.sortedBy { it.whenEnded }) {
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
								i { +"(Deleted Admiral)" }
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
	val currentSession = getUserSession() ?: redirect("/login")
	val currentUser = currentSession.user
	
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val admiral = Admiral.get(admiralId)!!
	
	if (admiral.owningUser != currentUser) forbid()
	
	val ownedShips = ShipInDrydock.filter(ShipInDrydock::owningAdmiral eq admiralId).toList()
	val buyableShips = ShipType.values()
		.mapNotNull { type -> type.buyPriceChecked(admiral, ownedShips)?.let { price -> type to price } }
		.sortedBy { (_, price) -> price }
		.sortedBy { (type, _) -> type.name }
		.sortedBy { (type, _) -> type.weightClass.tier }
		.sortedBy { (type, _) -> if (type.faction == admiral.faction) -1 else type.faction.ordinal }
		.toMap()
	
	return page(
		"Managing ${admiral.name}", standardNavBar(), PageNavSidebar(
			listOf(
				NavLink("/admiral/${admiral.id}", "Back to Admiral")
			)
		)
	) {
		section {
			h1 { +"Managing ${admiral.name}" }
			request.queryParameters["error"]?.let { errorMsg ->
				p {
					style = "color:#d22"
					+errorMsg
				}
			}
			form(method = FormMethod.post, action = "/admiral/${admiral.id}/manage") {
				csrfToken(currentSession.id)
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
				if (admiral.faction == Faction.FELINAE_FELICES)
					p {
						style = "font-size:0.8em;font-style:italic;color:#555"
						checkBoxInput {
							style = "display:none"
							id = "sex-female"
							checked = true
						}
						+"The Felinae Felices are a female-only faction."
					}
				else
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
					for ((i, flavor) in AdmiralNameFlavor.values().withIndex()) {
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
			val currRank = admiral.rank
			if (currRank.ordinal < AdmiralRank.values().size - 1) {
				val nextRank = AdmiralRank.values()[currRank.ordinal + 1]
				val reqAcumen = nextRank.minAcumen - currRank.minAcumen
				val hasAcumen = admiral.acumen - currRank.minAcumen
				
				label {
					h2 { +"Progress to Promotion" }
					progress {
						style = "width:100%;box-sizing:border-box"
						max = "$reqAcumen"
						value = "$hasAcumen"
						+"$hasAcumen/$reqAcumen"
					}
				}
				p {
					+"${admiral.fullName} is $hasAcumen/$reqAcumen Acumen away from being promoted to ${nextRank.getDisplayName(admiral.faction)}."
				}
			} else {
				h2 { +"Progress to Promotion" }
				p {
					+"${admiral.fullName} is at the maximum rank possible for the ${admiral.faction.navyName}."
				}
			}
		}
		section {
			h2 { +"Manage Fleet" }
			p {
				+"${admiral.fullName} currently owns ${admiral.money} ${admiral.faction.currencyName}, and earns ${admiral.rank.dailyWage} ${admiral.faction.currencyName} every day."
			}
			table {
				tr {
					th { +"Ship Name" }
					th { +"Ship Class" }
					th { +"Ship Status" }
					th { +"Ship Value" }
				}
				
				val now = Instant.now()
				for (ship in ownedShips.sortedBy { it.name }.sortedBy { it.shipType.weightClass.tier }) {
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
							val shipReadyAt = ship.readyAt
							if (shipReadyAt <= now) {
								+"Ready"
								br
								+"(since "
								span(classes = "moment") {
									style = "display:none"
									+shipReadyAt.toEpochMilli().toString()
								}
								+")"
							} else {
								+"Will be ready at "
								span(classes = "moment") {
									style = "display:none"
									+shipReadyAt.toEpochMilli().toString()
								}
							}
						}
						td {
							+ship.shipType.sellPrice.toString()
							+" "
							+admiral.faction.currencyName
							if (ship.readyAt <= now && !ship.shipType.weightClass.isUnique) {
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
				}
				for ((st, price) in buyableShips) {
					tr {
						td {
							a(href = "/info/${st.toUrlSlug()}") { +st.fullDisplayName }
						}
						td {
							+price.toString()
							+" "
							+admiral.faction.currencyName
							br
							a(href = "/admiral/${admiralId}/buy/${st.toUrlSlug()}") { +"Buy" }
						}
					}
				}
			}
		}
	}
}

suspend fun ApplicationCall.renameShipPage(): HTML.() -> Unit {
	val currentSession = getUserSession() ?: redirect("/login")
	val currentUser = currentSession.user
	
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val shipId = parameters["ship"]?.let { Id<ShipInDrydock>(it) }!!
	
	val (admiral, ship) = coroutineScope {
		val admiral = async { Admiral.get(admiralId)!! }
		val ship = async { ShipInDrydock.get(shipId)!! }
		admiral.await() to ship.await()
	}
	
	if (admiral.owningUser != currentUser) forbid()
	if (ship.owningAdmiral != admiralId) forbid()
	
	return page("Renaming Ship", null, null) {
		section {
			h1 { +"Renaming Ship" }
			p {
				+"${admiral.fullName} is about to rename the ${ship.shipType.fullDisplayName} ${ship.shipData.fullName}. Choose a name here:"
			}
			form(method = FormMethod.post, action = "/admiral/${admiral.id}/rename/${ship.id}") {
				csrfToken(currentSession.id)
				textInput(name = "name") {
					id = "name"
					value = ship.name
					
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
	val currentSession = getUserSession() ?: redirect("/login")
	val currentUser = currentSession.user
	
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val shipId = parameters["ship"]?.let { Id<ShipInDrydock>(it) }!!
	
	val (admiral, ship) = coroutineScope {
		val admiral = async { Admiral.get(admiralId)!! }
		val ship = async { ShipInDrydock.get(shipId)!! }
		admiral.await() to ship.await()
	}
	
	if (admiral.owningUser != currentUser) forbid()
	if (ship.owningAdmiral != admiralId) forbid()
	
	if (ship.readyAt > Instant.now()) redirect("/admiral/${admiralId}/manage")
	if (ship.shipType.weightClass.isUnique) redirect("/admiral/${admiralId}/manage")
	
	return page(
		"Are You Sure?", null, null
	) {
		section {
			h1 { +"Are You Sure?" }
			p {
				+"${admiral.fullName} is about to sell the ${ship.shipType.fullDisplayName} ${ship.shipData.fullName} for ${ship.shipType.sellPrice} ${admiral.faction.currencyName}."
			}
			form(method = FormMethod.get, action = "/admiral/${admiral.id}/manage") {
				submitInput {
					value = "Cancel"
				}
			}
			form(method = FormMethod.post, action = "/admiral/${admiral.id}/sell/${ship.id}") {
				csrfToken(currentSession.id)
				submitInput {
					value = "Sell"
				}
			}
		}
	}
}

suspend fun ApplicationCall.buyShipConfirmPage(): HTML.() -> Unit {
	val currentSession = getUserSession() ?: redirect("/login")
	val currentUser = currentSession.user
	
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val admiral = Admiral.get(admiralId)!!
	
	if (admiral.owningUser != currentUser) forbid()
	
	val shipType = parameters["ship"]?.let { param -> ShipType.values().singleOrNull { it.toUrlSlug() == param } }!!
	
	if (shipType.buyPrice > admiral.money) {
		return page(
			"Too Expensive", null, null
		) {
			section {
				h1 { +"Too Expensive" }
				p {
					+"Unfortunately, the ${shipType.fullDisplayName} is out of ${admiral.fullName}'s budget. It costs ${shipType.buyPrice} ${admiral.faction.currencyName}, and ${admiral.name} only has ${admiral.money} ${admiral.faction.currencyName}."
				}
				form(method = FormMethod.get, action = "/admiral/${admiral.id}/manage") {
					submitInput {
						value = "Back"
					}
				}
			}
		}
	}
	
	val ownedShips = ShipInDrydock.filter(ShipInDrydock::owningAdmiral eq admiralId).toList()
	if (shipType.buyPriceChecked(admiral, ownedShips) == null)
		throw NotFoundException()
	
	return page(
		"Are You Sure?", null, null
	) {
		section {
			h1 { +"Are You Sure?" }
			p {
				+"${admiral.fullName} is about to buy a ${shipType.fullDisplayName} for ${shipType.buyPrice} ${admiral.faction.currencyName}."
			}
			form(method = FormMethod.get, action = "/admiral/${admiral.id}/manage") {
				submitInput {
					value = "Cancel"
				}
			}
			form(method = FormMethod.post, action = "/admiral/${admiral.id}/buy/${shipType.toUrlSlug()}") {
				csrfToken(currentSession.id)
				submitInput {
					value = "Checkout"
				}
			}
		}
	}
}

suspend fun ApplicationCall.deleteAdmiralConfirmPage(): HTML.() -> Unit {
	val currentSession = getUserSession() ?: redirect("/login")
	val currentUser = currentSession.user
	
	val admiralId = parameters["id"]?.let { Id<Admiral>(it) }!!
	val admiral = Admiral.get(admiralId)!!
	
	if (admiral.owningUser != currentUser) forbid()
	
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
				csrfToken(currentSession.id)
				submitInput(classes = "evil") {
					value = "Yes"
				}
			}
		}
	}
}
