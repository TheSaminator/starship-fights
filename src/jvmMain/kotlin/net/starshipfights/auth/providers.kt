package net.starshipfights.auth

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.serialization.Serializable
import net.starshipfights.CurrentConfiguration
import net.starshipfights.DiscordLogin
import net.starshipfights.data.Id
import net.starshipfights.data.admiralty.*
import net.starshipfights.data.auth.PreferredTheme
import net.starshipfights.data.auth.User
import net.starshipfights.data.auth.UserSession
import net.starshipfights.data.createNonce
import net.starshipfights.forbid
import net.starshipfights.game.Faction
import net.starshipfights.game.FactionFlavor
import net.starshipfights.game.ShipType
import net.starshipfights.game.toUrlSlug
import net.starshipfights.info.*
import net.starshipfights.redirect
import org.litote.kmongo.*
import java.time.Instant
import java.time.temporal.ChronoUnit

const val PROFILE_NAME_MAX_LENGTH = 32
const val PROFILE_BIO_MAX_LENGTH = 240
const val ADMIRAL_NAME_MAX_LENGTH = 48
const val SHIP_NAME_MAX_LENGTH = 48

interface AuthProvider {
	fun installApplication(app: Application) = Unit
	fun installAuth(conf: Authentication.Configuration)
	fun installRouting(conf: Routing)
	
	companion object Installer {
		private val newCurrentProvider: AuthProvider
			get() = CurrentConfiguration.discordClient?.let { ProductionAuthProvider(it) } ?: TestAuthProvider
		
		private var cachedCurrentProvider: AuthProvider? = null
		
		val currentProvider: AuthProvider
			get() = cachedCurrentProvider ?: newCurrentProvider.also { cachedCurrentProvider = it }
		
		fun install(into: Application) {
			currentProvider.installApplication(into)
			
			into.install(Sessions) {
				cookie<Id<UserSession>>("sf_user_session") {
					serializer = UserSessionIdSerializer
					transform(SessionTransportTransformerMessageAuthentication(hex(CurrentConfiguration.secretHashingKey)))
					
					cookie.path = "/"
					cookie.extensions["Secure"] = null
					cookie.extensions["SameSite"] = "Lax"
				}
			}
			
			into.install(Authentication) {
				session<Id<UserSession>>("session") {
					validate { id ->
						val userAgent = request.userAgent() ?: return@validate null
						id.resolve(userAgent)?.let { sess ->
							User.get(sess.user)?.let { user ->
								sess.renewed(request.origin.remoteHost, user)
							}
						}
					}
					challenge("/login")
				}
				
				currentProvider.installAuth(this)
			}
			
			into.routing {
				get("/me") {
					val redirectTo = call.getUserSession()?.let { sess ->
						"/user/${sess.user}"
					} ?: "/login"
					
					redirect(redirectTo)
				}
				
				get("/me/manage") {
					call.respondHtml(HttpStatusCode.OK, call.manageUserPage())
				}
				
				get("/me/private-info") {
					call.respondHtml(HttpStatusCode.OK, call.privateInfoPage())
				}
				
				get("/me/private-info/txt") {
					call.respondText(ContentType.Text.Plain, HttpStatusCode.OK) { call.privateInfo() }
				}
				
				post("/me/manage") {
					val form = call.receiveValidatedParameters()
					val currentUser = call.getUser() ?: redirect("/login")
					
					val newUser = currentUser.copy(
						showDiscordName = form["showdiscord"] == "yes",
						showUserStatus = form["showstatus"] == "yes",
						logIpAddresses = form["logaddress"] == "yes",
						profileName = form["name"]?.takeIf { it.isNotBlank() && it.length <= PROFILE_NAME_MAX_LENGTH } ?: redirect("/me/manage" + withErrorMessage("Invalid name - must not be blank, must be at most $PROFILE_NAME_MAX_LENGTH characters")),
						profileBio = form["bio"]?.takeIf { it.isNotBlank() && it.length <= PROFILE_BIO_MAX_LENGTH } ?: redirect("/me/manage" + withErrorMessage("Invalid bio - must not be blank, must be at most $PROFILE_BIO_MAX_LENGTH characters")),
						preferredTheme = form["theme"]?.uppercase()?.takeIf { it in PreferredTheme.values().map { it.name } }?.let { PreferredTheme.valueOf(it) } ?: currentUser.preferredTheme
					)
					User.put(newUser)
					
					if (!newUser.logIpAddresses)
						launch {
							UserSession.update(
								UserSession::user eq currentUser.id,
								setValue(UserSession::clientAddresses, emptyList())
							)
						}
					
					redirect("/user/${newUser.id}")
				}
				
				get("/user/{id}") {
					call.respondHtml(HttpStatusCode.OK, call.userPage())
				}
				
				get("/admiral/new") {
					call.respondHtml(HttpStatusCode.OK, call.createAdmiralPage())
				}
				
				post("/admiral/new") {
					val form = call.receiveValidatedParameters()
					val currentUser = call.getUserSession()?.user ?: redirect("/login")
					
					val faction = Faction.valueOf(form.getOrFail("faction"))
					val newAdmiral = Admiral(
						owningUser = currentUser,
						name = form["name"]?.takeIf { it.isNotBlank() && it.length <= ADMIRAL_NAME_MAX_LENGTH } ?: redirect("/me/manage" + withErrorMessage("That name is not valid - must not be blank, must not be longer than $ADMIRAL_NAME_MAX_LENGTH characters")),
						isFemale = form.getOrFail("sex") == "female" || faction == Faction.FELINAE_FELICES,
						faction = faction,
						acumen = if (CurrentConfiguration.isDevEnv) 20_000 else 0,
						money = 500
					)
					val newShips = generateFleet(newAdmiral)
					
					coroutineScope {
						launch { Admiral.put(newAdmiral) }
						launch { ShipInDrydock.put(newShips) }
					}
					
					redirect("/admiral/${newAdmiral.id}")
				}
				
				get("/admiral/{id}") {
					call.respondHtml(HttpStatusCode.OK, call.admiralPage())
				}
				
				get("/admiral/{id}/manage") {
					call.respondHtml(HttpStatusCode.OK, call.manageAdmiralPage())
				}
				
				post("/admiral/{id}/manage") {
					val form = call.receiveValidatedParameters()
					
					val currentUser = call.getUserSession()?.user
					val admiralId = call.parameters["id"]?.let { Id<Admiral>(it) }!!
					val admiral = Admiral.get(admiralId)!!
					
					if (admiral.owningUser != currentUser) forbid()
					
					val newAdmiral = admiral.copy(
						name = form["name"]?.takeIf { it.isNotBlank() && it.length <= ADMIRAL_NAME_MAX_LENGTH } ?: redirect("/me/manage" + withErrorMessage("That name is not valid - must not be blank, must not be longer than $ADMIRAL_NAME_MAX_LENGTH characters")),
						isFemale = form["sex"] == "female" || admiral.faction == Faction.FELINAE_FELICES
					)
					
					Admiral.put(newAdmiral)
					redirect("/admiral/$admiralId")
				}
				
				get("/admiral/{id}/rename/{ship}") {
					call.respondHtml(HttpStatusCode.OK, call.renameShipPage())
				}
				
				post("/admiral/{id}/rename/{ship}") {
					val formParams = call.receiveValidatedParameters()
					val currentUser = call.getUserSession()?.user
					
					val admiralId = call.parameters["id"]?.let { Id<Admiral>(it) }!!
					val shipId = call.parameters["ship"]?.let { Id<ShipInDrydock>(it) }!!
					
					val (admiral, ship) = coroutineScope {
						val admiral = async { Admiral.get(admiralId)!! }
						val ship = async { ShipInDrydock.get(shipId)!! }
						admiral.await() to ship.await()
					}
					
					if (admiral.owningUser != currentUser) forbid()
					if (ship.owningAdmiral != admiralId) forbid()
					
					val newName = formParams["name"]?.takeIf { it.isNotBlank() && it.length <= SHIP_NAME_MAX_LENGTH } ?: redirect("/admiral/${admiralId}/manage" + withErrorMessage("That name is not valid - must not be blank, must not be longer than $SHIP_NAME_MAX_LENGTH characters"))
					ShipInDrydock.set(shipId, setValue(ShipInDrydock::name, newName))
					
					redirect("/admiral/${admiralId}/manage")
				}
				
				get("/admiral/{id}/sell/{ship}") {
					call.respondHtml(HttpStatusCode.OK, call.sellShipConfirmPage())
				}
				
				post("/admiral/{id}/sell/{ship}") {
					call.receiveValidatedParameters()
					
					val currentUser = call.getUserSession()?.user
					
					val admiralId = call.parameters["id"]?.let { Id<Admiral>(it) }!!
					val shipId = call.parameters["ship"]?.let { Id<ShipInDrydock>(it) }!!
					
					val (admiral, ship) = coroutineScope {
						val admiral = async { Admiral.get(admiralId)!! }
						val ship = async { ShipInDrydock.get(shipId)!! }
						admiral.await() to ship.await()
					}
					
					if (admiral.owningUser != currentUser) forbid()
					if (ship.owningAdmiral != admiralId) forbid()
					
					if (ship.readyAt > Instant.now()) redirect("/admiral/${admiralId}/manage" + withErrorMessage("Cannot sell ships that are not ready for battle"))
					if (ship.shipType.weightClass.isUnique) redirect("/admiral/${admiralId}/manage" + withErrorMessage("Cannot sell a ${ship.shipType.fullDisplayName}"))
					
					coroutineScope {
						launch { ShipInDrydock.del(shipId) }
						launch { Admiral.set(admiralId, inc(Admiral::money, ship.shipType.sellPrice)) }
					}
					
					redirect("/admiral/${admiralId}/manage")
				}
				
				get("/admiral/{id}/buy/{ship}") {
					call.respondHtml(HttpStatusCode.OK, call.buyShipConfirmPage())
				}
				
				post("/admiral/{id}/buy/{ship}") {
					call.receiveValidatedParameters()
					
					val currentUser = call.getUserSession()?.user
					val admiralId = call.parameters["id"]?.let { Id<Admiral>(it) }!!
					val admiral = Admiral.get(admiralId)!!
					
					if (admiral.owningUser != currentUser) forbid()
					val ownedShips = ShipInDrydock.filter(ShipInDrydock::owningAdmiral eq admiralId).toList()
					
					val shipType = call.parameters["ship"]?.let { param -> ShipType.values().singleOrNull { it.toUrlSlug() == param } }!!
					val shipPrice = shipType.buyPrice(admiral, ownedShips) ?: throw NotFoundException()
					
					if (shipPrice > admiral.money)
						redirect("/admiral/${admiralId}/manage" + withErrorMessage("You cannot afford that ship"))
					
					val shipNames = ownedShips.map { it.name }.toMutableSet()
					val newShipName = newShipName(shipType.faction, shipType.weightClass, shipNames) ?: nameShip(shipType.faction, shipType.weightClass)
					
					val newShip = ShipInDrydock(
						name = newShipName,
						shipType = shipType,
						shipFlavor = FactionFlavor.defaultForFaction(admiral.faction),
						readyAt = Instant.now().plus(2, ChronoUnit.HOURS),
						owningAdmiral = admiralId
					)
					
					coroutineScope {
						launch { ShipInDrydock.put(newShip) }
						launch { Admiral.set(admiralId, inc(Admiral::money, -shipPrice)) }
					}
					
					redirect("/admiral/${admiralId}/manage")
				}
				
				get("/admiral/{id}/delete") {
					call.respondHtml(HttpStatusCode.OK, call.deleteAdmiralConfirmPage())
				}
				
				post("/admiral/{id}/delete") {
					call.receiveValidatedParameters()
					
					val currentUser = call.getUserSession()?.user
					val admiralId = call.parameters["id"]?.let { Id<Admiral>(it) }!!
					val admiral = Admiral.get(admiralId)!!
					
					if (admiral.owningUser != currentUser) forbid()
					
					coroutineScope {
						launch { Admiral.del(admiralId) }
						launch { ShipInDrydock.remove(ShipInDrydock::owningAdmiral eq admiralId) }
					}
					
					redirect("/me")
				}
				
				post("/logout") {
					call.receiveValidatedParameters()
					
					call.getUserSession()?.let { sess ->
						launch {
							val newTime = Instant.now().minusMillis(100)
							UserSession.update(UserSession::id eq sess.id, setValue(UserSession::expiration, newTime))
						}
					}
					
					call.sessions.clear<Id<UserSession>>()
					redirect("/")
				}
				
				post("/logout/{id}") {
					call.receiveValidatedParameters()
					
					val id = Id<UserSession>(call.parameters.getOrFail("id"))
					call.getUserSession()?.let { sess ->
						launch {
							val newTime = Instant.now().minusMillis(100)
							UserSession.update(and(UserSession::id eq id, UserSession::user eq sess.user), setValue(UserSession::expiration, newTime))
						}
					}
					
					redirect("/me/manage")
				}
				
				post("/logout-all") {
					call.receiveValidatedParameters()
					
					call.getUserSession()?.let { sess ->
						launch {
							val newTime = Instant.now().minusMillis(100)
							UserSession.update(and(UserSession::user eq sess.user, UserSession::id ne sess.id), setValue(UserSession::expiration, newTime))
						}
					}
					
					redirect("/me/manage")
				}
				
				post("/clear-expired/{id}") {
					call.receiveValidatedParameters()
					
					val id = Id<UserSession>(call.parameters.getOrFail("id"))
					call.getUserSession()?.let { sess ->
						launch {
							val now = Instant.now()
							UserSession.remove(and(UserSession::id eq id, UserSession::user eq sess.user, UserSession::expiration lte now))
						}
					}
					
					redirect("/me/manage")
				}
				
				post("/clear-all-expired") {
					call.receiveValidatedParameters()
					
					call.getUserSession()?.let { sess ->
						launch {
							val now = Instant.now()
							UserSession.remove(and(UserSession::user eq sess.user, UserSession::expiration lte now))
						}
					}
					
					redirect("/me/manage")
				}
				
				currentProvider.installRouting(this)
			}
		}
	}
}

object TestAuthProvider : AuthProvider {
	private const val USERNAME_KEY = "username"
	private const val PASSWORD_KEY = "password"
	
	private const val PASSWORD_VALUE = "very secure"
	
	override fun installApplication(app: Application) {
		app.install(DoubleReceive)
	}
	
	override fun installAuth(conf: Authentication.Configuration) {
		with(conf) {
			form("test-auth") {
				userParamName = USERNAME_KEY
				passwordParamName = PASSWORD_KEY
				validate { credentials ->
					val originAddress = request.origin.remoteHost
					val userAgent = request.userAgent()
					if (userAgent != null && credentials.name.isNotBlank() && credentials.password == PASSWORD_VALUE) {
						val user = User.locate(User::discordId eq credentials.name)
							?: User(
								discordId = credentials.name,
								discordName = "",
								discordDiscriminator = "",
								discordAvatar = null,
								showDiscordName = false,
								profileName = credentials.name,
								profileBio = "BEEP BOOP I EXIST ONLY FOR TESTING BLOP BLARP.",
								registeredAt = Instant.now(),
								lastActivity = Instant.now(),
								showUserStatus = false,
								logIpAddresses = false,
							).also {
								User.put(it)
							}
						
						UserSession(
							user = user.id,
							clientAddresses = listOf(originAddress),
							userAgent = userAgent,
							expiration = newExpiration()
						).also {
							UserSession.put(it)
						}
					} else
						null
				}
				challenge { credentials ->
					val errorMsg = if (call.request.userAgent() == null)
						"User-Agent must be specified when logging in. Are you using some weird API client?"
					else if (credentials == null || credentials.name.isBlank())
						"A username must be provided."
					else if (credentials.password != PASSWORD_VALUE)
						"Password is incorrect."
					else
						"An unknown error occurred."
					
					val redirectUrl = "/login" + withErrorMessage(errorMsg)
					call.respondRedirect(redirectUrl)
				}
			}
		}
	}
	
	override fun installRouting(conf: Routing) {
		with(conf) {
			get("/login") {
				if (call.getUserSession() != null)
					redirect("/me")
				
				call.respondHtml(HttpStatusCode.OK, call.page("Authentication Test", call.standardNavBar(), CustomSidebar {
					p {
						+"This instance does not have Discord OAuth login set up. As a fallback, this authentication mode is used for testing only."
					}
				}) {
					section {
						h1 { +"Authentication Test" }
						form(action = "/login", method = FormMethod.post) {
							h3 {
								label {
									this.htmlFor = USERNAME_KEY
									+"Username"
								}
							}
							textInput {
								id = USERNAME_KEY
								name = USERNAME_KEY
								autoComplete = false
								
								required = true
							}
							call.request.queryParameters["error"]?.let { errorMsg ->
								p {
									style = "color:#d22"
									+errorMsg
								}
							}
							submitInput {
								value = "Authenticate"
							}
							hiddenInput {
								name = PASSWORD_KEY
								value = PASSWORD_VALUE
							}
						}
					}
				})
			}
			
			authenticate("test-auth") {
				post("/login") {
					call.principal<UserSession>()?.id?.let { sessionId ->
						call.sessions.set(sessionId)
					}
					redirect("/me")
				}
			}
		}
	}
}

class ProductionAuthProvider(private val discordLogin: DiscordLogin) : AuthProvider {
	private val httpClient = HttpClient(Apache) {
		install(UserAgent) {
			agent = discordLogin.userAgent
		}
		
		install(RateLimit) {
			jsonCodec = JsonClientCodec
		}
	}
	
	override fun installAuth(conf: Authentication.Configuration) {
		conf.oauth("auth-oauth-discord") {
			urlProvider = { "https://starshipfights.net/login/discord/callback" }
			providerLookup = {
				OAuthServerSettings.OAuth2ServerSettings(
					name = "discord",
					authorizeUrl = "https://discord.com/api/oauth2/authorize",
					accessTokenUrl = "https://discord.com/api/oauth2/token",
					requestMethod = HttpMethod.Post,
					clientId = discordLogin.clientId,
					clientSecret = discordLogin.clientSecret,
					defaultScopes = listOf("identify"),
					nonceManager = StateParameterManager
				)
			}
			client = httpClient
		}
	}
	
	override fun installRouting(conf: Routing) {
		with(conf) {
			get("/login") {
				call.respondHtml(HttpStatusCode.OK, call.page("Login with Discord", call.standardNavBar()) {
					section {
						p {
							style = "text-align:center"
							+"By logging in, you indicate your agreement to the "
							a(href = "/about/tnc") { +"Terms and Conditions" }
							+" and the "
							a(href = "/about/pp") { +"Privacy Policy" }
							+"."
						}
						call.request.queryParameters["error"]?.let { errorMsg ->
							p {
								style = "color:#d22"
								+errorMsg
							}
						}
						p {
							style = "text-align:center"
							a(href = "/login/discord") { +"Continue to Discord" }
						}
					}
				})
			}
			
			authenticate("auth-oauth-discord") {
				get("/login/discord") {
					// Redirects to 'authorizeUrl' automatically
				}
				
				get("/login/discord/callback") {
					val userAgent = call.request.userAgent() ?: forbid()
					val principal: OAuthAccessTokenResponse.OAuth2 = call.principal() ?: redirect("/login")
					val userInfoJson = httpClient.get<String>("https://discord.com/api/users/@me") {
						headers {
							append(HttpHeaders.Authorization, "Bearer ${principal.accessToken}")
						}
					}
					
					val userInfo = JsonClientCodec.decodeFromString(DiscordUserInfo.serializer(), userInfoJson)
					val (discordId, discordUsername, discordDiscriminator, discordAvatar) = userInfo
					
					var redirectTo = "/me"
					
					val user = User.locate(User::discordId eq discordId)?.copy(
						discordName = discordUsername,
						discordDiscriminator = discordDiscriminator,
						discordAvatar = discordAvatar
					) ?: User(
						discordId = discordId,
						discordName = discordUsername,
						discordDiscriminator = discordDiscriminator,
						discordAvatar = discordAvatar,
						showDiscordName = false,
						profileName = discordUsername,
						profileBio = "Hi, I'm new here!",
						registeredAt = Instant.now(),
						lastActivity = Instant.now(),
						showUserStatus = false,
						logIpAddresses = false,
					).also { redirectTo = "/me/manage" }
					
					val userSession = UserSession(
						user = user.id,
						clientAddresses = if (user.logIpAddresses) listOf(call.request.origin.remoteHost) else emptyList(),
						userAgent = userAgent,
						expiration = newExpiration()
					)
					
					coroutineScope {
						launch { User.put(user) }
						launch { UserSession.put(userSession) }
					}
					
					call.sessions.set(userSession.id)
					redirect(redirectTo)
				}
			}
		}
	}
}

object StateParameterManager : NonceManager {
	private val nonces = mutableSetOf<String>()
	
	override suspend fun newNonce(): String {
		return createNonce().also { nonces += it }
	}
	
	override suspend fun verifyNonce(nonce: String): Boolean {
		return nonces.remove(nonce)
	}
}

@Serializable
data class DiscordUserInfo(
	val id: String,
	val username: String,
	val discriminator: String,
	val avatar: String
)
