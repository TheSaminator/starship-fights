package net.starshipfights.game

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id

enum class AdmiralRank {
	REAR_ADMIRAL,
	VICE_ADMIRAL,
	ADMIRAL,
	HIGH_ADMIRAL,
	LORD_ADMIRAL;
	
	val maxShipWeightClass: ShipWeightClass
		get() = when (this) {
			REAR_ADMIRAL -> ShipWeightClass.CRUISER
			VICE_ADMIRAL -> ShipWeightClass.BATTLECRUISER
			ADMIRAL -> ShipWeightClass.BATTLESHIP
			HIGH_ADMIRAL -> ShipWeightClass.BATTLESHIP
			LORD_ADMIRAL -> ShipWeightClass.COLOSSUS
		}
	
	val maxBattleSize: BattleSize
		get() = BattleSize.values().last { it.maxWeightClass.tier <= maxShipWeightClass.tier }
	
	val minAcumen: Int
		get() = when (this) {
			REAR_ADMIRAL -> 0
			VICE_ADMIRAL -> 1000
			ADMIRAL -> 4000
			HIGH_ADMIRAL -> 9000
			LORD_ADMIRAL -> 16000
		}
	
	val dailyWage: Int
		get() = when (this) {
			REAR_ADMIRAL -> 40
			VICE_ADMIRAL -> 50
			ADMIRAL -> 60
			HIGH_ADMIRAL -> 70
			LORD_ADMIRAL -> 80
		}
	
	companion object {
		fun fromAcumen(acumen: Int) = values().lastOrNull { it.minAcumen <= acumen } ?: values().first()
	}
}

fun AdmiralRank.getDisplayName(faction: Faction) = when (faction) {
	Faction.MECHYRDIA -> when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Retrógardi Admiral"
		AdmiralRank.VICE_ADMIRAL -> "Vicj Admiral"
		AdmiralRank.ADMIRAL -> "Admiral"
		AdmiralRank.HIGH_ADMIRAL -> "Altadmiral"
		AdmiralRank.LORD_ADMIRAL -> "Dómin Admiral"
	}
	Faction.NDRC -> when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Commandeur"
		AdmiralRank.VICE_ADMIRAL -> "Schout-bij-Nacht"
		AdmiralRank.ADMIRAL -> "Vice-Admiraal"
		AdmiralRank.HIGH_ADMIRAL -> "Luitenant-Admiraal"
		AdmiralRank.LORD_ADMIRAL -> "Admiraal"
	}
	Faction.MASRA_DRAETSEN -> when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Syna Raquor"
		AdmiralRank.VICE_ADMIRAL -> "Ruhn Raquor"
		AdmiralRank.ADMIRAL -> "Raquor"
		AdmiralRank.HIGH_ADMIRAL -> "Vosh Raquor"
		AdmiralRank.LORD_ADMIRAL -> "Yauh Raquor"
	}
	Faction.FELINAE_FELICES -> when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Domina Iunior"
		AdmiralRank.VICE_ADMIRAL -> "Domina Vicaria"
		AdmiralRank.ADMIRAL -> "Domina"
		AdmiralRank.HIGH_ADMIRAL -> "Domina Senior"
		AdmiralRank.LORD_ADMIRAL -> "Ducissa"
	}
	Faction.ISARNAREYKK -> when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Maer nu Ambaght"
		AdmiralRank.VICE_ADMIRAL -> "Neid Fletsleydar"
		AdmiralRank.ADMIRAL -> "Fletsleydar"
		AdmiralRank.HIGH_ADMIRAL -> "Hauk Fletsleydar"
		AdmiralRank.LORD_ADMIRAL -> "Hokst Fletsleydar"
	}
	Faction.VESTIGIUM -> when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Rear Marshal"
		AdmiralRank.VICE_ADMIRAL -> "Vice Marshal"
		AdmiralRank.ADMIRAL -> "Marshal"
		AdmiralRank.HIGH_ADMIRAL -> "Grand Marshal"
		AdmiralRank.LORD_ADMIRAL -> "Chief Marshal"
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
