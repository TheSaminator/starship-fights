package net.starshipfights.info

import io.ktor.application.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.html.*
import net.starshipfights.auth.getUser
import net.starshipfights.auth.getUserSession
import net.starshipfights.data.admiralty.*
import net.starshipfights.data.auth.User
import net.starshipfights.data.auth.UserSession
import net.starshipfights.redirect
import org.litote.kmongo.div
import org.litote.kmongo.eq
import java.time.Instant

suspend fun ApplicationCall.privateInfo(): String {
	val currentSession = getUserSession() ?: redirect("/login")
	
	val now = Instant.now()
	
	val userId = currentSession.user
	val (user, userData) = coroutineScope {
		val getUser = async { User.get(userId) }
		val getAdmirals = async { Admiral.filter(Admiral::owningUser eq userId).toList() }
		val getSessions = async { UserSession.filter(UserSession::user eq userId).toList() }
		val getBattles = async {
			BattleRecord.filter(
				(BattleRecord::participants / BattleParticipant::user) eq userId
			).toList()
		}
		
		getUser.await() to Triple(getAdmirals.await(), getSessions.await(), getBattles.await())
	}
	val (userAdmirals, userSessions, userBattles) = userData
	user ?: redirect("/login")
	
	val admiralBattles = userAdmirals.associate { admiral ->
		admiral.id to userBattles.filter { record ->
			record.participants.any { it.admiral == admiral.id }
		}
	}
	
	val (admiralShips, battleOtherParticipants) = coroutineScope {
		val getShips = userAdmirals.associate { admiral ->
			admiral.id to (async {
				ShipInDrydock.filter(ShipInDrydock::owningAdmiral eq admiral.id).toList()
			} to async {
				ShipMemorial.filter(ShipMemorial::owningAdmiral eq admiral.id).toList()
			})
		}
		val getOtherParticipants = admiralBattles.mapValues { (admiralId, records) ->
			records.associate { record ->
				record.id to record.participants.filter { it.admiral != admiralId }.map { participant ->
					async { Admiral.get(participant.admiral) }
				}
			}
		}
		
		getShips.mapValues { (_, pair) ->
			val (ships, graves) = pair
			ships.await() to graves.await()
		} to getOtherParticipants.mapValues { (_, records) ->
			records.mapValues { (_, admirals) ->
				admirals.mapNotNull { admiralAsync ->
					admiralAsync.await()
				}
			}
		}
	}
	
	return buildString {
		appendLine("# Private data of user https://starshipfights.net/user/$userId\n")
		appendLine("Profile name: ${user.profileName}")
		appendLine("Profile bio: \"\"\"")
		appendLine(user.profileBio)
		appendLine("\"\"\"")
		appendLine("Display theme: ${user.preferredTheme}")
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
		for (session in userSessions.sortedByDescending { it.expiration }) {
			appendLine("")
			appendLine("### Session ${session.id}")
			appendLine("Browser User-Agent: ${session.userAgent}")
			appendLine("Client addresses${if (session.clientAddresses.isEmpty()) " are not stored" else ":"}")
			for (addr in session.clientAddresses) appendLine("* $addr")
			appendLine("${if (session.expiration > now) "Will expire" else "Has expired"} at: ${session.expiration}")
		}
		appendLine("")
		appendLine("")
		appendLine("## Admiral data")
		for (admiral in userAdmirals) {
			appendLine("")
			appendLine("### ${admiral.fullName} (https://starshipfights.net/admiral/${admiral.id})")
			appendLine("Admiral is ${if (admiral.isFemale) "female" else "male"}")
			appendLine("Admiral serves the ${admiral.faction.navyName}")
			appendLine("Admiral's experience is ${admiral.acumen} acumen")
			appendLine("Admiral's monetary wealth is ${admiral.money} ${admiral.faction.currencyName}")
			appendLine("Admiral can command ships as big as ${admiral.rank.maxShipTier.displayName} size")
			val ships = admiralShips[admiral.id]?.first.orEmpty()
			appendLine("Admiral has ${ships.size} ships:")
			for (ship in ships) {
				appendLine("")
				appendLine("#### ${ship.fullName} (${ship.id})")
				appendLine("Ship is a ${ship.shipType.fullerDisplayName}")
				appendLine("Ship ${if (ship.readyAt > now) "will be ready at" else "has been ready since"} ${ship.readyAt}")
			}
			appendLine("")
			val graves = admiralShips[admiral.id]?.second.orEmpty()
			appendLine("Admiral has lost ${graves.size} ships in battle:")
			for (grave in graves) {
				appendLine("")
				appendLine("#### ${grave.fullName} (${grave.id})")
				appendLine("Ship is a ${grave.shipType.fullerDisplayName}")
				appendLine("Ship was destroyed at ${grave.destroyedAt} in battle recorded at ${grave.destroyedIn}")
			}
			
			val records = admiralBattles[admiral.id].orEmpty()
			appendLine("Admiral has fought in ${records.size} battles:")
			for (record in records.sortedBy { it.whenEnded }) {
				appendLine("")
				appendLine("##### Battle record ${record.id}")
				appendLine("Battle size: ${record.battleInfo.size.displayName} (${record.battleInfo.size.numPoints})")
				appendLine("Battle background: ${record.battleInfo.bg.displayName}")
				appendLine("Battle started at: ${record.whenStarted}")
				appendLine("Battle completed at: ${record.whenEnded}")
				
				val otherParticipants = battleOtherParticipants[admiral.id]?.get(record.id)
					.orEmpty()
					.filter { record.getSide(it.id) != null }
					.sortedBy { if (record.getSide(it.id) == record.getSide(admiral.id)) 0 else 1 }
				
				for (otherParticipant in otherParticipants) {
					val preposition = if (record.getSide(otherParticipant.id) == record.getSide(admiral.id))
						"alongside"
					else "against"
					
					appendLine("Battle was fought $preposition ${otherParticipant.fullName} (https://starshipfights.net/admiral/${otherParticipant.id})")
				}
				
				val endMessage = record.participants.singleOrNull { it.admiral == admiral.id }?.endMessage ?: "Stalemate"
				appendLine("Battle ended in a $endMessage")
				appendLine(" => \"${record.winMessage}\"")
			}
		}
		appendLine("")
		appendLine("# More information")
		appendLine("This document contains the totality of your private data as stored by Starship Fights")
		appendLine("This page can be accessed at https://starshipfights.net/me/private-info")
		appendLine("All private info can be downloaded at https://starshipfights.net/me/private-info/txt")
		appendLine("The privacy policy can be reviewed at https://starshipfights.net/about/pp")
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
