package net.starshipfights.data.admiralty

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.starshipfights.data.DataDocument
import net.starshipfights.data.DocumentTable
import net.starshipfights.data.Id
import net.starshipfights.data.auth.User
import net.starshipfights.data.invoke
import net.starshipfights.game.BattleInfo
import net.starshipfights.game.GlobalSide
import java.time.Instant

@Serializable
data class BattleRecord(
	@SerialName("_id")
	override val id: Id<BattleRecord> = Id(),
	
	val battleInfo: BattleInfo,
	
	val whenStarted: @Contextual Instant,
	val whenEnded: @Contextual Instant,
	
	val hostUser: Id<User>,
	val guestUser: Id<User>,
	
	val hostAdmiral: Id<Admiral>,
	val guestAdmiral: Id<Admiral>,
	
	val hostEndingMessage: String,
	val guestEndingMessage: String,
	
	val winner: GlobalSide?,
	val winMessage: String,
	val was2v1: Boolean = false,
) : DataDocument<BattleRecord> {
	fun getSide(admiral: Id<Admiral>) = when (admiral) {
		hostAdmiral -> GlobalSide.HOST
		guestAdmiral -> GlobalSide.GUEST
		else -> null
	}
	
	fun getUserSide(user: Id<User>) = when (user) {
		hostUser -> GlobalSide.HOST
		guestUser -> GlobalSide.GUEST
		else -> null
	}
	
	fun wasWinner(side: GlobalSide) = if (winner == null)
		null
	else if (was2v1)
		winner == GlobalSide.HOST
	else
		winner == side
	
	fun didAdmiralWin(admiral: Id<Admiral>) = getSide(admiral)?.let { wasWinner(it) }
	
	fun didUserWin(user: Id<User>) = getUserSide(user)?.let { wasWinner(it) }
	
	companion object Table : DocumentTable<BattleRecord> by DocumentTable.create({
		index(BattleRecord::hostUser)
		index(BattleRecord::guestUser)
		
		index(BattleRecord::hostAdmiral)
		index(BattleRecord::guestAdmiral)
	})
}
