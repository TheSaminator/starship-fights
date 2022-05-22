package starshipfights.auth

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.sessions.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import starshipfights.data.Id
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession
import starshipfights.data.createNonce
import starshipfights.invalidCsrfToken
import starshipfights.redirect
import java.time.Instant
import java.time.temporal.ChronoUnit

suspend fun Id<UserSession>.resolve(userAgent: String) = UserSession.get(this)?.takeIf { session ->
	session.userAgent == userAgent && session.expiration > Instant.now()
}

fun newExpiration(): Instant = Instant.now().plus(2, ChronoUnit.HOURS)

suspend fun UserSession.renewed(clientAddress: String) = copy(
	expiration = newExpiration(),
	clientAddresses = if (User.get(user)?.logIpAddresses != true)
		emptyList()
	else if (clientAddresses.lastOrNull() != clientAddress)
		clientAddresses + clientAddress
	else
		clientAddresses
).also { UserSession.put(it) }

suspend fun User.updated() = copy(
	lastActivity = Instant.now()
).also { User.put(it) }

val UserAndSessionAttribute = AttributeKey<Pair<UserSession?, User?>>("SfUserAndSession")

suspend fun ApplicationCall.getUserSession() = getUserAndSession().first

suspend fun ApplicationCall.getUser() = getUserAndSession().second

suspend fun ApplicationCall.getUserAndSession() = request.pipeline.attributes.getOrNull(UserAndSessionAttribute)
	?: request.userAgent()?.let { sessions.get<Id<UserSession>>()?.resolve(it) }
		?.renewed(request.origin.remoteHost)
		?.let { it to User.get(it.user)?.updated() }
		?.also { request.pipeline.attributes.put(UserAndSessionAttribute, it) }
	?: (null to null)

object UserSessionIdSerializer : SessionSerializer<Id<UserSession>> {
	override fun serialize(session: Id<UserSession>): String {
		return session.id
	}
	
	override fun deserialize(text: String): Id<UserSession> {
		return Id(text)
	}
}

data class CsrfInput(val cookie: Id<UserSession>, val target: String)

object CsrfProtector {
	private val nonces = mutableMapOf<String, CsrfInput>()
	
	const val csrfInputName = "csrf-token"
	
	fun newNonce(token: Id<UserSession>, action: String): String {
		return createNonce().also { nonces[it] = CsrfInput(token, action) }
	}
	
	fun verifyNonce(nonce: String, token: Id<UserSession>, action: String): Boolean {
		return nonces.remove(nonce) == CsrfInput(token, action)
	}
}

suspend fun ApplicationCall.receiveValidatedParameters(): Parameters {
	val formInput = receiveParameters()
	val sessionId = sessions.get<Id<UserSession>>() ?: redirect("/login")
	val csrfToken = formInput.getOrFail(CsrfProtector.csrfInputName)
	
	if (CsrfProtector.verifyNonce(csrfToken, sessionId, request.uri))
		return formInput
	else
		invalidCsrfToken()
}

val JsonClientCodec = Json {
	ignoreUnknownKeys = true
}

fun withErrorMessage(message: String) = "?${parametersOf("error", message).formUrlEncode()}"
