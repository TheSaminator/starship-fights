package net.starshipfights.game

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id

enum class AdmiralRank {
	REAR_ADMIRAL,
	VICE_ADMIRAL,
	ADMIRAL,
	HIGH_ADMIRAL,
	LORD_ADMIRAL;
	
	val maxShipTier: ShipTier
		get() = when (this) {
			REAR_ADMIRAL -> ShipTier.CRUISER
			VICE_ADMIRAL -> ShipTier.BATTLECRUISER
			ADMIRAL -> ShipTier.BATTLESHIP
			HIGH_ADMIRAL -> ShipTier.BATTLESHIP
			LORD_ADMIRAL -> ShipTier.TITAN
		}
	
	val maxBattleSize: BattleSize
		get() = BattleSize.values().last { it.minRank <= this }
	
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

private val AdmiralRank.tylanDisplayName: String
	get() = when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Toukas Vjargardisrar"
		AdmiralRank.VICE_ADMIRAL -> "Toukas Vpursar"
		AdmiralRank.ADMIRAL -> "Toukas"
		AdmiralRank.HIGH_ADMIRAL -> "Toukas Maenar"
		AdmiralRank.LORD_ADMIRAL -> "Hipratoukas"
	}

private val AdmiralRank.olympianDisplayName: String
	get() = when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Centurio Postremus"
		AdmiralRank.VICE_ADMIRAL -> "Centurio Vicarius"
		AdmiralRank.ADMIRAL -> "Centurio Astronauticus"
		AdmiralRank.HIGH_ADMIRAL -> "Legatus Astronauticus"
		AdmiralRank.LORD_ADMIRAL -> "Dux Astronauticus"
	}

private val AdmiralRank.texandrianDisplayName: String
	get() = when (this) {
		AdmiralRank.REAR_ADMIRAL -> "Konteradmiral"
		AdmiralRank.VICE_ADMIRAL -> "Vizeadmiral"
		AdmiralRank.ADMIRAL -> "Admiral"
		AdmiralRank.HIGH_ADMIRAL -> "Generaladmiral"
		AdmiralRank.LORD_ADMIRAL -> "Großadmiral"
	}

fun AdmiralRank.getDisplayName(factionFlavor: FactionFlavor) = when (factionFlavor) {
	FactionFlavor.MECHYRDIA -> getDisplayName(Faction.MECHYRDIA)
	FactionFlavor.TYLA -> tylanDisplayName
	FactionFlavor.OLYMPIA -> olympianDisplayName
	FactionFlavor.TEXANDRIA -> texandrianDisplayName
	FactionFlavor.NDRC -> getDisplayName(Faction.NDRC)
	FactionFlavor.CCC -> getDisplayName(Faction.NDRC)
	FactionFlavor.MJOLNIR_ENERGY -> getDisplayName(Faction.NDRC)
	FactionFlavor.MASRA_DRAETSEN -> getDisplayName(Faction.MASRA_DRAETSEN)
	FactionFlavor.AEDON_CULTISTS -> getDisplayName(Faction.MASRA_DRAETSEN)
	FactionFlavor.FERTHLON_EXILES -> getDisplayName(Faction.MASRA_DRAETSEN)
	FactionFlavor.RES_NOSTRA -> olympianDisplayName
	FactionFlavor.CORSAIRS -> olympianDisplayName
	FactionFlavor.FELINAE_FELICES -> getDisplayName(Faction.FELINAE_FELICES)
	FactionFlavor.ISARNAREYKK -> getDisplayName(Faction.ISARNAREYKK)
	FactionFlavor.SWARTAREYKK -> getDisplayName(Faction.ISARNAREYKK)
	FactionFlavor.THEUDAREYKK -> getDisplayName(Faction.ISARNAREYKK)
	FactionFlavor.STAHLAREYKK -> getDisplayName(Faction.ISARNAREYKK)
	FactionFlavor.LYUDAREYKK -> getDisplayName(Faction.ISARNAREYKK)
	FactionFlavor.NEUIA_FULKREYKK -> getDisplayName(Faction.ISARNAREYKK)
	FactionFlavor.CORVUS_CLUSTER_VESTIGIUM -> getDisplayName(Faction.VESTIGIUM)
	FactionFlavor.COLEMAN_SF_BASE_VESTIGIUM -> getDisplayName(Faction.VESTIGIUM)
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
