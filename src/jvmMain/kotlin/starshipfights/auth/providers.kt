package starshipfights.auth

import com.mongodb.MongoException
import io.ktor.application.*
import io.ktor.auth.*
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
import org.litote.kmongo.eq
import starshipfights.CurrentConfiguration
import starshipfights.data.Id
import starshipfights.data.admiralty.Admiral
import starshipfights.data.admiralty.ShipInDrydock
import starshipfights.data.admiralty.generateFleet
import starshipfights.data.auth.*
import starshipfights.game.AdmiralRank
import starshipfights.game.Faction
import starshipfights.info.*
import starshipfights.redirect

interface AuthProvider {
	fun installApplication(app: Application) = Unit
	fun installAuth(conf: Authentication.Configuration)
	fun installRouting(conf: Routing)
	
	companion object Installer {
		private val currentProvider: AuthProvider
			get() = if (CurrentConfiguration.isDevEnv)
				TestAuthProvider
			else
				TODO("Need to implement production AuthProvider")
		
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
							User.get(it.user)?.username?.let { name ->
								"/user/$name"
							}
						} ?: "/login"
						
						redirect(redirectTo)
					}
				}
				
				get("/me/manage") {
					call.respondHtml(HttpStatusCode.OK, call.manageUserPage())
				}
				
				post("/me/manage") {
					val currentUser = call.getUser() ?: redirect("/login")
					val form = call.receiveParameters()
					
					val newName = form.getOrFail("name")
					if (usernameRegex.matchEntire(newName) == null)
						redirect("/me/manage?" + parametersOf("error", invalidUsernameErrorMessage).formUrlEncode())
					
					val newUser = currentUser.copy(
						username = form.getOrFail("name")
					)
					try {
						User.put(newUser)
						redirect("/user/${newUser.username}")
					} catch (ex: MongoException) {
						redirect("/me/manage?" + parametersOf("error", "That username is already taken").formUrlEncode())
					}
				}
				
				get("/user/{name}") {
					call.respondHtml(HttpStatusCode.OK, call.userPage())
				}
				
				get("/admiral/new") {
					call.respondHtml(HttpStatusCode.OK, call.createAdmiralPage())
				}
				
				post("/admiral/new") {
					val currentUser = call.getUserSession()?.user ?: redirect("/login")
					val form = call.receiveParameters()
					
					val newAdmiral = Admiral(
						owningUser = currentUser,
						name = form.getOrFail("name"),
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
				
				get("/admiral/{id}") {
					call.respondHtml(HttpStatusCode.OK, call.admiralPage())
				}
				
				get("/admiral/{id}/manage") {
					call.respondHtml(HttpStatusCode.OK, call.manageAdmiralPage())
				}
				
				post("/admiral/{id}/manage") {
					val currentUser = call.getUserSession()?.user
					val admiralId = call.parameters["id"]?.let { Id<Admiral>(it) }!!
					val admiral = Admiral.get(admiralId)!!
					
					if (admiral.owningUser != currentUser) throw IllegalArgumentException()
					
					val form = call.receiveParameters()
					val newAdmiral = admiral.copy(
						name = form["name"] ?: admiral.name,
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
					
					if (admiral.owningUser != currentUser) throw IllegalArgumentException()
					
					Admiral.del(admiralId)
					redirect("/me")
				}
				
				get("/logout") {
					call.sessions.get<Id<UserSession>>()?.let { sessId ->
						launch {
							UserSession.del(sessId)
						}
					}
					
					call.sessions.clear<Id<UserSession>>()
					redirect("/")
				}
				
				currentProvider.installRouting(this)
			}
		}
	}
}

object TestAuthProvider : AuthProvider {
	private const val USERNAME_KEY = "username"
	private const val PASSWORD_KEY = "password"
	private const val REMEMBER_ME_KEY = "remember-me"
	
	private const val PASSWORD_VALUE = "very secure"
	private const val REMEMBER_ME_VALUE = "yes"
	
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
					if (userAgent != null && credentials.name.isValidUsername() && credentials.password == PASSWORD_VALUE) {
						val user = User.locate(User::username eq credentials.name)
							?: User(username = credentials.name).also {
								User.put(it)
							}
						
						val formParams = receiveOrNull<Parameters>()
						val timeToRemember = if (formParams?.get(REMEMBER_ME_KEY) == REMEMBER_ME_VALUE)
							31_556_925_216L // 1 solar year
						else
							3_600_000L // 1 hour
						
						UserSession(
							user = user.id,
							clientAddresses = listOf(originAddress),
							userAgent = userAgent,
							expirationMillis = System.currentTimeMillis() + timeToRemember
						).also {
							UserSession.put(it)
						}
					} else
						null
				}
				challenge { credentials ->
					val errorMsg = if (call.request.userAgent() == null)
						"User-Agent must be specified when logging in. Are you using some weird API client?"
					else if (credentials == null)
						"A username must be provided."
					else if (!credentials.name.isValidUsername())
						invalidUsernameErrorMessage
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
								minLength = "2"
								maxLength = "32"
								title = usernameTooltip
								pattern = usernameRegexStr
							}
							errorMsg?.let { msg ->
								p {
									style = "color:#d22"
									+msg
								}
							}
							p {
								label {
									htmlFor = REMEMBER_ME_KEY
									checkBoxInput {
										id = REMEMBER_ME_KEY
										name = REMEMBER_ME_KEY
										value = REMEMBER_ME_VALUE
									}
									+"Remember Me"
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
