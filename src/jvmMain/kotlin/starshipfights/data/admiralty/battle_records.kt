package starshipfights.data.admiralty

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import starshipfights.data.DataDocument
import starshipfights.data.DocumentTable
import starshipfights.data.Id
import starshipfights.data.auth.User
import starshipfights.data.invoke
import starshipfights.game.BattleInfo
import starshipfights.game.GlobalSide
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
) : DataDocument<BattleRecord> {
	companion object Table : DocumentTable<BattleRecord> by DocumentTable.create({
		index(BattleRecord::hostUser)
		index(BattleRecord::guestUser)
		
		index(BattleRecord::hostAdmiral)
		index(BattleRecord::guestAdmiral)
	})
}
