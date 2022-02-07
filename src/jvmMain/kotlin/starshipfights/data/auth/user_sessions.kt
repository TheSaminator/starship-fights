package starshipfights.data.auth

import io.ktor.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import starshipfights.data.DataDocument
import starshipfights.data.DocumentTable
import starshipfights.data.Id
import starshipfights.data.invoke

const val usernameRegexStr = "[a-zA-Z0-9_\\-]{2,32}"
val usernameRegex = Regex(usernameRegexStr)
fun String.isValidUsername() = usernameRegex.matchEntire(this) != null
const val usernameTooltip = "Between 2 and 32 characters that are either letters, numbers, hyphens, or underscores"
const val invalidUsernameErrorMessage = "Invalid username. Usernames must be between 2 and 32 characters that are either letters, numbers, hyphens, or underscores"

@Serializable
data class User(
	@SerialName("_id")
	override val id: Id<User> = Id(),
	val username: String,
	val status: UserStatus = UserStatus.AVAILABLE,
) : DataDocument<User> {
	companion object Table : DocumentTable<User> by DocumentTable.create({
		unique(User::username)
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
