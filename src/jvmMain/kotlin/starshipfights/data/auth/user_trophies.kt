package starshipfights.data.auth

import kotlinx.html.*
import kotlinx.serialization.Serializable
import starshipfights.CurrentConfiguration

@Serializable
sealed class UserTrophy : Comparable<UserTrophy> {
	protected abstract fun ASIDE.render()
	fun renderInto(sidebar: ASIDE) = sidebar.render()
	
	// Higher rank = lower on page
	protected abstract val rank: Int
	override fun compareTo(other: UserTrophy): Int {
		return rank.compareTo(other.rank)
	}
}

fun ASIDE.renderTrophy(trophy: UserTrophy) = trophy.renderInto(this)

@Serializable
object SiteOwnerTrophy : UserTrophy() {
	override fun ASIDE.render() {
		p {
			style = "text-align:center;border:2px solid #a82;padding:3px;background-color:#fc3;color:#541;font-variant:small-caps;font-family:'JetBrains Mono',monospace"
			+"Site Owner"
		}
	}
	
	override val rank: Int
		get() = 0
}

@Serializable
object SiteDeveloperTrophy : UserTrophy() {
	override fun ASIDE.render() {
		p {
			style = "text-align:center;border:2px solid #62a;padding:3px;background-color:#93f;color:#315;font-variant:small-caps;font-family:'JetBrains Mono',monospace"
			title = "This person helps with coding the game"
			+"Site Developer"
		}
	}
	
	override val rank: Int
		get() = 1
}

@Serializable
data class SiteJanitorTrophy(val isSenior: Boolean) : UserTrophy() {
	override fun ASIDE.render() {
		p {
			style = "text-align:center;border:2px solid #840;padding:3px;background-color:#c60;color:#420;font-variant:small-caps;font-family:'JetBrains Mono',monospace"
			title = "This person helps with cleaning the poo out of the site"
			+if (isSenior) "Senior Janitor" else "Janitor"
		}
	}
	
	override val rank: Int
		get() = 2
}

@Serializable
data class SiteSupporterTrophy(val amountInUsCents: Int) : UserTrophy() {
	override fun ASIDE.render() {
		p {
			style = "text-align:center;border:2px solid #694;padding:3px;background-color:#af7;color:#231;font-variant:small-caps;font-family:'JetBrains Mono',monospace"
			title = "\"I spent money on an online game and all I got was this lousy trophy!\""
			+"Site Supporter:"
			br
			+when {
				amountInUsCents < 100 -> "Rear Admiral"
				amountInUsCents < 500 -> "Vice Admiral"
				amountInUsCents < 1000 -> "Admiral"
				amountInUsCents < 2000 -> "High Admiral"
				else -> "Lord Admiral"
			}
		}
	}
	
	override val rank: Int
		get() = 3
}

fun User.getTrophiesUnsorted(): Set<UserTrophy> =
	(if (discordId == CurrentConfiguration.discordClient?.ownerId)
		setOf(SiteOwnerTrophy)
	else emptySet()) + (if (amountDonatedInUsCents > 0)
		setOf(SiteSupporterTrophy(amountDonatedInUsCents))
	else emptySet())

fun User.getTrophies(): List<UserTrophy> = getTrophiesUnsorted().sorted()
