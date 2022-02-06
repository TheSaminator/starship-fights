package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id

enum class AdmiralRank(val maxShipWeightClass: ShipWeightClass) {
	REAR_ADMIRAL(ShipWeightClass.CRUISER),
	VICE_ADMIRAL(ShipWeightClass.CRUISER),
	ADMIRAL(ShipWeightClass.BATTLECRUISER),
	HIGH_ADMIRAL(ShipWeightClass.BATTLESHIP),
	LORD_ADMIRAL(ShipWeightClass.COLOSSUS);
	
	val maxBattleSize: BattleSize
		get() = BattleSize.values().last { it.maxWeightClass <= maxShipWeightClass }
}

fun AdmiralRank.getDisplayName(faction: Faction) = when (faction) {
	Faction.MECHYRDIA -> when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Retrógardi Admiral"
		AdmiralRank.VICE_ADMIRAL -> "Vicj Admiral"
		AdmiralRank.ADMIRAL -> "Admiral"
		AdmiralRank.HIGH_ADMIRAL -> "Altadmiral"
		AdmiralRank.LORD_ADMIRAL -> "Dómin Admiral"
	}
	Faction.MASRA_DRAETSEN -> when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Syna Raquor"
		AdmiralRank.VICE_ADMIRAL -> "Ruhn Raquor"
		AdmiralRank.ADMIRAL -> "Raquor"
		AdmiralRank.HIGH_ADMIRAL -> "Vosh Raquor"
		AdmiralRank.LORD_ADMIRAL -> "Yauh Raquor"
	}
	Faction.ISARNAREYKK -> when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Maer nu Ambaght"
		AdmiralRank.VICE_ADMIRAL -> "Neid Fletsleydar"
		AdmiralRank.ADMIRAL -> "Fletsleydar"
		AdmiralRank.HIGH_ADMIRAL -> "Hauk Fletsleydar"
		AdmiralRank.LORD_ADMIRAL -> "Hokst Fletsleydar"
	}
	Faction.VESTIGIUM -> when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Lieutenant Colonel"
		AdmiralRank.VICE_ADMIRAL -> "Colonel"
		AdmiralRank.ADMIRAL -> "Brigadier General"
		AdmiralRank.HIGH_ADMIRAL -> "Major General"
		AdmiralRank.LORD_ADMIRAL -> "Lieutenant General"
	}
}

@Serializable
data class InGameUser(
	val id: Id<InGameUser>,
	val username: String
)

@Serializable
data class InGameAdmiral(
	val id: Id<InGameAdmiral>,
	
	val user: InGameUser,
	
	val name: String,
	val isFemale: Boolean,
	
	val faction: Faction,
	val rank: AdmiralRank,
) {
	val fullName: String
		get() = "${rank.getDisplayName(faction)} $name"
}
