package starshipfights.data.auth

import io.ktor.auth.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import starshipfights.data.DataDocument
import starshipfights.data.DocumentTable
import starshipfights.data.Id
import starshipfights.data.invoke
import java.time.Instant

@Serializable
data class User(
	@SerialName("_id")
	override val id: Id<User> = Id(),
	
	val discordId: String,
	val discordName: String,
	val discordDiscriminator: String,
	val discordAvatar: String?,
	val showDiscordName: Boolean,
	
	val profileName: String,
	val profileBio: String,
	
	val registeredAt: @Contextual Instant,
	val lastActivity: @Contextual Instant,
	val showUserStatus: Boolean,
	
	val logIpAddresses: Boolean,
	
	val status: UserStatus = UserStatus.AVAILABLE,
	
	val amountDonatedInUsCents: Int = 0,
) : DataDocument<User> {
	val discordAvatarUrl: String
		get() = discordAvatar?.takeIf { showDiscordName }?.let {
			"https://cdn.discordapp.com/avatars/$discordId/$it." + (if (it.startsWith("a_")) "gif" else "png") + "?size=256"
		} ?: anonymousAvatarUrl
	
	val anonymousAvatarUrl: String
		get() = "https://cdn.discordapp.com/embed/avatars/${(discordDiscriminator.lastOrNull()?.digitToInt() ?: 0) % 5}.png"
	
	companion object Table : DocumentTable<User> by DocumentTable.create({
		unique(User::discordId)
		index(User::registeredAt)
	})
}

enum class UserStatus {
	AVAILABLE, IN_MATCHMAKING, READY_FOR_BATTLE, IN_BATTLE
}

@Serializable
data class UserSession(
	@SerialName("_id")
	override val id: Id<UserSession> = Id(),
	val user: Id<User>,
	val clientAddresses: List<String>,
	val userAgent: String,
	val expiration: @Contextual Instant
) : DataDocument<UserSession>, Principal {
	companion object Table : DocumentTable<UserSession> by DocumentTable.create({
		index(UserSession::user)
	})
}
