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
import net.starshipfights.game.GlobalShipController
import net.starshipfights.game.GlobalSide
import org.litote.kmongo.div
import java.time.Instant

@Serializable
data class BattleRecord(
	@SerialName("_id")
	override val id: Id<BattleRecord> = Id(),
	
	val battleInfo: BattleInfo,
	
	val whenStarted: @Contextual Instant,
	val whenEnded: @Contextual Instant,
	
	val participants: List<BattleParticipant>,
	
	val winner: GlobalSide?,
	val winMessage: String,
) : DataDocument<BattleRecord> {
	fun getSide(admiral: Id<Admiral>) = participants.singleOrNull { it.admiral == admiral }?.side?.side
	
	fun wasWinner(side: GlobalSide) = if (winner == null)
		null
	else
		winner == side
	
	fun didAdmiralWin(admiral: Id<Admiral>) = getSide(admiral)?.let { wasWinner(it) }
	
	companion object Table : DocumentTable<BattleRecord> by DocumentTable.create({
		index(BattleRecord::participants / BattleParticipant::user)
		index(BattleRecord::participants / BattleParticipant::admiral)
	})
}

@Serializable
data class BattleParticipant(
	val user: Id<User>,
	val admiral: Id<Admiral>,
	val side: GlobalShipController,
	val endMessage: String,
)
