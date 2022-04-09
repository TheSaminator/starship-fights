package starshipfights.info

import io.ktor.application.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.html.*
import org.litote.kmongo.eq
import org.litote.kmongo.or
import starshipfights.auth.getUser
import starshipfights.auth.getUserSession
import starshipfights.data.admiralty.Admiral
import starshipfights.data.admiralty.BattleRecord
import starshipfights.data.admiralty.ShipInDrydock
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession
import starshipfights.game.GlobalSide
import starshipfights.redirect
import java.time.Instant

suspend fun ApplicationCall.privateInfo(): String {
	val currentSession = getUserSession() ?: redirect("/login")
	val userId = currentSession.user
	val (user, userData) = coroutineScope {
		val getUser = async { User.get(userId) }
		val getAdmirals = async { Admiral.filter(Admiral::owningUser eq userId).toList() }
		val getSessions = async { UserSession.filter(UserSession::user eq userId).toList() }
		val getBattles = async {
			BattleRecord.filter(
				or(
					BattleRecord::hostUser eq userId,
					BattleRecord::guestUser eq userId
				)
			).toList()
		}
		
		getUser.await() to Triple(getAdmirals.await(), getSessions.await(), getBattles.await())
	}
	
	val now = Instant.now()
	val (userAdmirals, userSessions, userBattles) = userData
	user ?: redirect("/login")
	
	val battleEndings = userBattles.associate { record ->
		record.id to when (record.winner) {
			GlobalSide.HOST -> record.hostUser == userId
			GlobalSide.GUEST -> record.guestUser == userId
			null -> null
		}
	}
	
	val (admiralShips, battleAdmirals, battleOpponents) = coroutineScope {
		val getShips = userAdmirals.associate { admiral ->
			admiral.id to async { ShipInDrydock.filter(ShipInDrydock::owningAdmiral eq admiral.id).toList() }
		}
		val getAdmirals = userBattles.associate { record ->
			val admiralId = if (record.hostUser == userId) record.hostAdmiral else record.guestAdmiral
			record.id to async { Admiral.get(admiralId) }
		}
		val getOpponents = userBattles.associate { record ->
			val (opponentId, opponentAdmiralId) = if (record.hostUser == userId) record.guestUser to record.guestAdmiral else record.hostUser to record.hostAdmiral
			
			record.id to (async { User.get(opponentId) } to async { Admiral.get(opponentAdmiralId) })
		}
		
		Triple(
			getShips.mapValues { (_, deferred) -> deferred.await() },
			getAdmirals.mapValues { (_, deferred) -> deferred.await() },
			getOpponents.mapValues { (_, deferred) -> deferred.let { (u, a) -> u.await() to a.await() } }
		)
	}
	
	return buildString {
		appendLine("# Private data of user https://starshipfights.net/user/$userId\n")
		appendLine("Profile name: ${user.profileName}")
		appendLine("Profile bio: \"\"\"")
		appendLine(user.profileBio)
		appendLine("\"\"\"")
		appendLine("")
		appendLine("## Activity data")
		appendLine("Registered at: ${user.registeredAt}")
		appendLine("Last activity: ${user.lastActivity}")
		appendLine("Online status: ${if (user.showUserStatus) "shown" else "hidden"}")
		appendLine("")
		appendLine("## Discord login data")
		appendLine("Discord ID: ${user.discordId}")
		appendLine("Discord name: ${user.discordName}")
		appendLine("Discord discriminator: ${user.discordDiscriminator}")
		appendLine(user.discordAvatar?.let { "Discord avatar: $it" } ?: "Discord avatar absent")
		appendLine("Discord profile: ${if (user.showDiscordName) "shown" else "hidden"}")
		appendLine("")
		appendLine("## Session data")
		appendLine("IP addresses are ${if (user.logIpAddresses) "stored" else "ignored"}")
		userSessions.sortedByDescending { it.expiration }.forEach { session ->
			appendLine("")
			appendLine("### Session ${session.id}")
			appendLine("Browser User-Agent: ${session.userAgent}")
			appendLine("Client addresses${if (session.clientAddresses.isEmpty()) " are not stored" else ":"}")
			session.clientAddresses.forEach { addr -> appendLine("* $addr") }
			appendLine("${if (session.expiration > now) "Will expire" else "Has expired"} at: ${session.expiration}")
		}
		appendLine("")
		appendLine("## Battle-record data")
		userBattles.sortedBy { it.whenEnded }.forEach { record ->
			appendLine("")
			appendLine("### Battle record ${record.id}")
			appendLine("Battle size: ${record.battleInfo.size.displayName} (${record.battleInfo.size.numPoints})")
			appendLine("Battle background: ${record.battleInfo.bg.displayName}")
			appendLine("Battle started at: ${record.whenStarted}")
			appendLine("Battle completed at: ${record.whenEnded}")
			appendLine("Battle was fought by ${battleAdmirals[record.id]?.let { "${it.fullName} (https://starshipfights.net/admiral/${it.id})" } ?: "{deleted admiral}"}")
			appendLine("Battle was fought against ${battleOpponents[record.id]?.second?.let { "${it.fullName} (https://starshipfights.net/admiral/${it.id})" } ?: "{deleted admiral}"}")
			appendLine(" => ${battleOpponents[record.id]?.first?.let { "${it.profileName} (https://starshipfights.net/user/${it.id})" } ?: "{deleted user}"}")
			when (battleEndings[record.id]) {
				true -> appendLine("Battle ended in victory")
				false -> appendLine("Battle ended in defeat")
				null -> appendLine("Battle ended in stalemate")
			}
			appendLine(" => \"${record.winMessage}\"")
		}
		appendLine("")
		appendLine("## Admiral data")
		userAdmirals.forEach { admiral ->
			appendLine("")
			appendLine("### ${admiral.fullName} (https://starshipfights.net/admiral/${admiral.id})")
			appendLine("Admiral is ${if (admiral.isFemale) "female" else "male"}")
			appendLine("Admiral serves the ${admiral.faction.navyName}")
			appendLine("Admiral's experience is ${admiral.acumen} acumen")
			appendLine("Admiral's monetary wealth is ${admiral.money} ${admiral.faction.currencyName}")
			appendLine("Admiral can command ships as big as a ${admiral.rank.maxShipWeightClass.displayName}")
			val ships = admiralShips[admiral.id].orEmpty()
			appendLine("Admiral has ${ships.size} ships:")
			ships.forEach { ship ->
				appendLine("")
				appendLine("#### ${ship.fullName} (${ship.id})")
				appendLine("Ship is a ${ship.shipType.fullerDisplayName}")
				appendLine("Ship ${if (ship.readyAt > now) "will be ready at" else "has been ready since"} ${ship.readyAt}")
			}
			appendLine("")
			appendLine("# More information")
			appendLine("This document contains the totality of your private data as stored by Starship Fights")
			appendLine("This page can be accessed at https://starshipfights.net/me/private-info")
			appendLine("All private info can be downloaded at https://starshipfights.net/me/private-info/txt")
			appendLine("The privacy policy can be reviewed at https://starshipfights.net/about/pp")
		}
	}
}

suspend fun ApplicationCall.privateInfoPage(): HTML.() -> Unit {
	if (getUser() == null) redirect("/login")
	
	return page(
		null, standardNavBar(), PageNavSidebar(
			listOf(
				NavLink("/me/manage", "Back to Preferences"),
				NavLink("/about/pp", "Review Privacy Policy"),
			)
		)
	) {
		section {
			h1 { +"Your Private Info" }
			
			iframe {
				style = "width:100%;height:25em"
				src = "/me/private-info/txt"
			}
			
			p {
				a(href = "/me/private-info/txt") {
					attributes["download"] = "private-info.txt"
					+"Download your private info"
				}
			}
		}
	}
}
