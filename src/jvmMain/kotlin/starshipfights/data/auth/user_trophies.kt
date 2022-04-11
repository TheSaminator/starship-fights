package starshipfights.data.auth

import kotlinx.html.*
import kotlinx.serialization.Serializable
import starshipfights.CurrentConfiguration

@Serializable
sealed class UserTrophy : Comparable<UserTrophy> {
	protected abstract fun TagConsumer<*>.render()
	fun renderInto(consumer: TagConsumer<*>) = consumer.render()
	
	// Higher rank = lower on page
	protected abstract val rank: Int
	override fun compareTo(other: UserTrophy): Int {
		return rank.compareTo(other.rank)
	}
}

fun TagConsumer<*>.renderTrophy(trophy: UserTrophy) = trophy.renderInto(this)

@Serializable
object SiteOwnerTrophy : UserTrophy() {
	override fun TagConsumer<*>.render() {
		p {
			style = "text-align:center;border:2px solid #a82;padding:3px;background-color:#fc3;color:#541;font-variant:small-caps;font-family:'JetBrains Mono',monospace"
			+"Site Owner"
		}
	}
	
	override val rank: Int
		get() = 0
}

fun User.getTrophiesUnsorted(): Set<UserTrophy> =
	(if (discordId == CurrentConfiguration.discordClient?.ownerId)
		setOf(SiteOwnerTrophy)
	else emptySet())

fun User.getTrophies(): List<UserTrophy> = getTrophiesUnsorted().sorted()
