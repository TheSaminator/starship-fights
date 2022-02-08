package starshipfights.auth

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.setValue
import starshipfights.*
import starshipfights.data.Id
import starshipfights.data.admiralty.Admiral
import starshipfights.data.admiralty.ShipInDrydock
import starshipfights.data.admiralty.generateFleet
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession
import starshipfights.data.createNonce
import starshipfights.game.AdmiralRank
import starshipfights.game.Faction
import starshipfights.info.*

interface AuthProvider {
	fun installApplication(app: Application) = Unit
	fun installAuth(conf: Authentication.Configuration)
	fun installRouting(conf: Routing)
	
	companion object Installer {
		private val currentProvider: AuthProvider
			get() = CurrentConfiguration.discordClient?.let { ProductionAuthProvider(it) } ?: TestAuthProvider
		
		fun install(into: Application) {
			currentProvider.installApplication(into)
			
			into.install(Sessions) {
				cookie<Id<UserSession>>("sf_user_session") {
					serializer = UserSessionIdSerializer
					
					cookie.path = "/"
					cookie.extensions["Secure"] = null
					cookie.extensions["SameSite"] = "Lax"
				}
			}
			
			into.install(Authentication) {
				session<Id<UserSession>>("session") {
					validate { id ->
						val userAgent = request.userAgent() ?: return@validate null
						id.resolve(userAgent)?.renewed(request.origin.remoteHost)
					}
					challenge("/login")
				}
				
				currentProvider.installAuth(this)
			}
			
			into.routing {
				authenticate("session") {
					get("/me") {
						val redirectTo = call.principal<UserSession>()?.let {
							"/user/${it.user}"
						} ?: "/login"
						
						redirect(redirectTo)
					}
					
					get("/me/manage") {
						call.respondHtml(HttpStatusCode.OK, call.manageUserPage())
					}
					
					post("/me/manage") {
						val currentUser = call.getUser() ?: redirect("/login")
						val form = call.receiveParameters()
						
						val newUser = currentUser.copy(
							profileName = form["name"]?.takeIf { it.isNotBlank() } ?: currentUser.profileName
						)
						User.put(newUser)
						redirect("/user/${newUser.id}")
					}
				}
				
				get("/user/{id}") {
					call.respondHtml(HttpStatusCode.OK, call.userPage())
				}
				
				authenticate("session") {
					get("/admiral/new") {
						call.respondHtml(HttpStatusCode.OK, call.createAdmiralPage())
					}
					
					post("/admiral/new") {
						val currentUser = call.getUserSession()?.user ?: redirect("/login")
						val form = call.receiveParameters()
						
						val newAdmiral = Admiral(
							owningUser = currentUser,
							name = form["name"]?.takeIf { it.isNotBlank() } ?: throw MissingRequestParameterException("name"),
							isFemale = form.getOrFail("sex") == "female",
							faction = Faction.valueOf(form.getOrFail("faction")),
							// TODO change to Rear Admiral
							rank = AdmiralRank.LORD_ADMIRAL
						)
						val newShips = generateFleet(newAdmiral)
						
						coroutineScope {
							launch { Admiral.put(newAdmiral) }
							newShips.forEach {
								launch { ShipInDrydock.put(it) }
							}
						}
						
						redirect("/admiral/${newAdmiral.id}")
					}
				}
				
				get("/admiral/{id}") {
					call.respondHtml(HttpStatusCode.OK, call.admiralPage())
				}
				
				authenticate("session") {
					get("/admiral/{id}/manage") {
						call.respondHtml(HttpStatusCode.OK, call.manageAdmiralPage())
					}
					
					post("/admiral/{id}/manage") {
						val currentUser = call.getUserSession()?.user
						val admiralId = call.parameters["id"]?.let { Id<Admiral>(it) }!!
						val admiral = Admiral.get(admiralId)!!
						
						if (admiral.owningUser != currentUser) throw ForbiddenException()
						
						val form = call.receiveParameters()
						val newAdmiral = admiral.copy(
							name = form["name"]?.takeIf { it.isNotBlank() } ?: admiral.name,
							isFemale = form["sex"] == "female"
						)
						
						Admiral.put(newAdmiral)
						redirect("/admiral/$admiralId")
					}
					
					get("/admiral/{id}/delete") {
						call.respondHtml(HttpStatusCode.OK, call.deleteAdmiralConfirmPage())
					}
					
					post("/admiral/{id}/delete") {
						val currentUser = call.getUserSession()?.user
						val admiralId = call.parameters["id"]?.let { Id<Admiral>(it) }!!
						val admiral = Admiral.get(admiralId)!!
						
						if (admiral.owningUser != currentUser) throw ForbiddenException()
						
						Admiral.del(admiralId)
						redirect("/me")
					}
					
					get("/logout") {
						call.getUserSession()?.let { sess ->
							launch {
								val newTime = System.currentTimeMillis() - 100
								UserSession.update(UserSession::id eq sess.id, setValue(UserSession::expirationMillis, newTime))
							}
						}
						
						call.sessions.clear<Id<UserSession>>()
						redirect("/")
					}
					
					get("/logout/{id}") {
						val id = Id<UserSession>(call.parameters.getOrFail("id"))
						call.getUserSession()?.let { sess ->
							launch {
								val newTime = System.currentTimeMillis() - 100
								UserSession.update(and(UserSession::id eq id, UserSession::user eq sess.user), setValue(UserSession::expirationMillis, newTime))
							}
						}
						
						redirect("/me/manage")
					}
					
					get("/logout-all") {
						call.getUserSession()?.let { sess ->
							launch {
								val newTime = System.currentTimeMillis() - 100
								UserSession.update(and(UserSession::user eq sess.user, UserSession::id ne sess.id), setValue(UserSession::expirationMillis, newTime))
							}
						}
						
						redirect("/me/manage")
					}
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
								discordDiscriminator = "0000",
								discordAvatar = null,
								profileName = "Test User"
							).also {
								User.put(it)
							}
						
						UserSession(
							user = user.id,
							clientAddresses = listOf(originAddress),
							userAgent = userAgent,
							expirationMillis = System.currentTimeMillis() + 3_600_000L
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
					
					val redirectUrl = "/login?" + parametersOf("error", errorMsg).formUrlEncode()
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
				
				val errorMsg = call.request.queryParameters["error"]
				
				call.respondHtml(HttpStatusCode.OK, page("Authentication Test", call.standardNavBar(), CustomSidebar {
					p {
						+"This method of authentication is only for testing. "
						+"I trust you not to abuse this; don't make me regret my trust."
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
							errorMsg?.let { msg ->
								p {
									style = "color:#d22"
									+msg
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

class ProductionAuthProvider(val discordLogin: DiscordLogin) : AuthProvider {
	private val httpClient = HttpClient(Apache)
	
	override fun installAuth(conf: Authentication.Configuration) {
		conf.oauth("auth-oauth-discord") {
			urlProvider = { discordLogin.redirectUrlOrigin.removeSuffix("/") + "/login/discord/callback" }
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
				val errorMsg = call.request.queryParameters["error"]
				
				call.respondHtml(HttpStatusCode.OK, page("Login with Discord", call.standardNavBar(), null) {
					section {
						if (errorMsg != null)
							p {
								style = "color:#d22"
								+errorMsg
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
					val userAgent = call.request.userAgent() ?: throw ForbiddenException()
					val principal: OAuthAccessTokenResponse.OAuth2 = call.principal() ?: redirect("/login")
					val userInfoJson = httpClient.get<String>("https://discord.com/api/users/@me") {
						headers {
							append(HttpHeaders.Authorization, "Bearer ${principal.accessToken}")
						}
					}
					
					val userInfo = JsonConfigCodec.parseToJsonElement(userInfoJson) as? JsonObject ?: redirect("/login")
					val discordId = (userInfo["id"] as? JsonPrimitive)?.content ?: redirect("/login")
					val discordUsername = (userInfo["username"] as? JsonPrimitive)?.content ?: redirect("/login")
					val discordDiscriminator = (userInfo["discriminator"] as? JsonPrimitive)?.content ?: redirect("/login")
					val discordAvatar = (userInfo["avatar"] as? JsonPrimitive)?.content
					
					val user = User.locate(User::discordId eq discordId)?.copy(
						discordName = discordUsername,
						discordDiscriminator = discordDiscriminator,
						discordAvatar = discordAvatar
					) ?: User(
						discordId = discordId,
						discordName = discordUsername,
						discordDiscriminator = discordDiscriminator,
						discordAvatar = discordAvatar,
						profileName = discordUsername
					)
					
					val userSession = UserSession(
						user = user.id,
						clientAddresses = listOf(call.request.origin.host),
						userAgent = userAgent,
						expirationMillis = System.currentTimeMillis() + 86_400_000
					)
					
					launch { User.put(user) }
					launch { UserSession.put(userSession) }
					
					call.sessions.set(userSession.id)
					redirect("/me")
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
