package starshipfights.auth

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.sessions.*
import starshipfights.data.Id
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession

const val EXPIRATION_TIME = 86_400_000

suspend fun Id<UserSession>.resolve(userAgent: String) = UserSession.get(this)?.takeIf { session ->
	session.userAgent == userAgent && session.expirationMillis > System.currentTimeMillis()
}

suspend fun UserSession.renewed(clientAddress: String) = copy(
	expirationMillis = System.currentTimeMillis() + EXPIRATION_TIME,
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
