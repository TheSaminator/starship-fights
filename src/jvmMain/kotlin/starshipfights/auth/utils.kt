package starshipfights.auth

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.sessions.*
import starshipfights.data.Id
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession
import java.time.Instant
import java.time.temporal.ChronoUnit

suspend fun Id<UserSession>.resolve(userAgent: String) = UserSession.get(this)?.takeIf { session ->
	session.userAgent == userAgent && session.expiration.isAfter(Instant.now())
}

suspend fun UserSession.renewed(clientAddress: String) = copy(
	expiration = Instant.now().plus(1, ChronoUnit.DAYS),
	clientAddresses = if (clientAddresses.last() != clientAddress) clientAddresses + clientAddress else clientAddresses
).also { UserSession.put(it) }

suspend fun ApplicationCall.getUserSession() = request.userAgent()?.let { sessions.get<Id<UserSession>>()?.resolve(it) }?.renewed(request.origin.remoteHost)

suspend fun ApplicationCall.getUser() = getUserSession()?.user?.let { User.get(it) }

object UserSessionIdSerializer : SessionSerializer<Id<UserSession>> {
	override fun serialize(session: Id<UserSession>): String {
		return session.id
	}
	
	override fun deserialize(text: String): Id<UserSession> {
		return Id(text)
	}
}
