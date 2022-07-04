package net.starshipfights.campaign

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id
import net.starshipfights.game.*

val Faction.allegiences: Set<FactionFlavor>
	get() = FactionFlavor.values().filter { this in it.loyalties }.toSet()

val FactionFlavor.loyalties: List<Faction>
	get() = when (this) {
		FactionFlavor.MECHYRDIA -> listOf(Faction.MECHYRDIA, Faction.NDRC)
		FactionFlavor.TYLA -> listOf(Faction.MECHYRDIA, Faction.NDRC)
		FactionFlavor.OLYMPIA -> listOf(Faction.MECHYRDIA, Faction.NDRC)
		FactionFlavor.TEXANDRIA -> listOf(Faction.MECHYRDIA, Faction.NDRC)
		
		FactionFlavor.NDRC -> listOf(Faction.MECHYRDIA, Faction.NDRC)
		FactionFlavor.CCC -> listOf(Faction.MECHYRDIA, Faction.NDRC)
		FactionFlavor.MJOLNIR_ENERGY -> listOf(Faction.MECHYRDIA, Faction.NDRC)
		
		FactionFlavor.MASRA_DRAETSEN -> listOf(Faction.MASRA_DRAETSEN)
		FactionFlavor.AEDON_CULTISTS -> listOf(Faction.MASRA_DRAETSEN)
		FactionFlavor.FERTHLON_EXILES -> listOf(Faction.MASRA_DRAETSEN)
		
		FactionFlavor.RES_NOSTRA -> listOf(Faction.FELINAE_FELICES)
		FactionFlavor.CORSAIRS -> listOf(Faction.FELINAE_FELICES)
		FactionFlavor.FELINAE_FELICES -> listOf(Faction.FELINAE_FELICES)
		
		FactionFlavor.ISARNAREYKK -> listOf(Faction.ISARNAREYKK)
		FactionFlavor.SWARTAREYKK -> listOf(Faction.ISARNAREYKK)
		FactionFlavor.THEUDAREYKK -> listOf(Faction.MECHYRDIA, Faction.NDRC)
		FactionFlavor.STAHLAREYKK -> listOf(Faction.MECHYRDIA, Faction.NDRC)
		FactionFlavor.LYUDAREYKK -> listOf(Faction.ISARNAREYKK)
		FactionFlavor.NEUIA_FULKREYKK -> listOf(Faction.ISARNAREYKK)
		
		FactionFlavor.CORVUS_CLUSTER_VESTIGIUM -> listOf(Faction.VESTIGIUM)
		FactionFlavor.COLEMAN_SF_BASE_VESTIGIUM -> listOf(Faction.VESTIGIUM)
	}

@Serializable
data class FleetPresence(
	val name: String,
	val owner: FactionFlavor,
	val ships: Map<Id<Ship>, Ship>,
	
	val admiralName: String,
	val admiralIsFemale: Boolean,
	val admiralRank: AdmiralRank,
) {
	val pointValue: Int
		get() = ships.values.sumOf { it.pointCost }
	
	val admiralFullName: String
		get() = "${admiralRank.getDisplayName(owner)} $admiralName"
}
