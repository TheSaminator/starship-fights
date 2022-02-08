package starshipfights.data.auth

import io.ktor.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import starshipfights.data.DataDocument
import starshipfights.data.DocumentTable
import starshipfights.data.Id
import starshipfights.data.invoke

@Serializable
data class User(
	@SerialName("_id")
	override val id: Id<User> = Id(),
	val discordId: String,
	val discordName: String,
	val discordDiscriminator: String,
	val profileName: String,
	val status: UserStatus = UserStatus.AVAILABLE,
) : DataDocument<User> {
	companion object Table : DocumentTable<User> by DocumentTable.create({
		unique(User::discordId)
	})
}

enum class UserStatus {
	AVAILABLE, IN_MATCHMAKING, IN_BATTLE
}

@Serializable
data class UserSession(
	@SerialName("_id")
	override val id: Id<UserSession> = Id(),
	val user: Id<User>,
	val clientAddresses: List<String>,
	val userAgent: String,
	val expirationMillis: Long
) : DataDocument<UserSession>, Principal {
	companion object Table : DocumentTable<UserSession> by DocumentTable.create({
		index(UserSession::user)
	})
}
