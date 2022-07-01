package net.starshipfights.campaign

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id
import net.starshipfights.game.*

val FactionFlavor.getMapColor: IntColor
	get() = when (this) {
		FactionFlavor.MECHYRDIA -> IntColor(255, 204, 51)
		FactionFlavor.TYLA -> IntColor(255, 204, 51)
		FactionFlavor.OLYMPIA -> IntColor(255, 204, 51)
		FactionFlavor.TEXANDRIA -> IntColor(255, 204, 51)
		
		FactionFlavor.NDRC -> IntColor(255, 204, 51)
		FactionFlavor.CCC -> IntColor(255, 204, 51)
		FactionFlavor.MJOLNIR_ENERGY -> IntColor(255, 204, 51)
		
		FactionFlavor.MASRA_DRAETSEN -> IntColor(204, 34, 34)
		FactionFlavor.AEDON_CULTISTS -> IntColor(204, 34, 34)
		FactionFlavor.FERTHLON_EXILES -> IntColor(204, 34, 34)
		
		FactionFlavor.RES_NOSTRA -> IntColor(204, 102, 153)
		FactionFlavor.CORSAIRS -> IntColor(204, 102, 153)
		FactionFlavor.FELINAE_FELICES -> IntColor(204, 102, 153)
		
		FactionFlavor.ISARNAREYKK -> IntColor(34, 221, 34)
		FactionFlavor.SWARTAREYKK -> IntColor(34, 221, 34)
		FactionFlavor.THEUDAREYKK -> IntColor(255, 204, 51) // Mechyrdia
		FactionFlavor.STAHLAREYKK -> IntColor(255, 204, 51) // Also Mechyrdia
		FactionFlavor.LYUDAREYKK -> IntColor(34, 221, 34)
		FactionFlavor.NEUIA_FULKREYKK -> IntColor(34, 221, 34)
		
		FactionFlavor.CORVUS_CLUSTER_VESTIGIUM -> IntColor(108, 96, 153)
		FactionFlavor.COLEMAN_SF_BASE_VESTIGIUM -> IntColor(108, 96, 153)
	}

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

val FactionFlavor.mapCounterShipClass: ShipType
	get() = when (this) {
		FactionFlavor.MECHYRDIA -> ShipType.VENSCA
		FactionFlavor.TYLA -> ShipType.VENSCA
		FactionFlavor.OLYMPIA -> ShipType.VENSCA
		FactionFlavor.TEXANDRIA -> ShipType.VENSCA
		
		FactionFlavor.NDRC -> ShipType.VOORHOEDE
		FactionFlavor.CCC -> ShipType.VOORHOEDE
		FactionFlavor.MJOLNIR_ENERGY -> ShipType.VOORHOEDE
		
		FactionFlavor.MASRA_DRAETSEN -> ShipType.MORGOTH
		FactionFlavor.AEDON_CULTISTS -> ShipType.MORGOTH
		FactionFlavor.FERTHLON_EXILES -> ShipType.MORGOTH
		
		FactionFlavor.RES_NOSTRA -> ShipType.BOBCAT
		FactionFlavor.CORSAIRS -> ShipType.BOBCAT
		FactionFlavor.FELINAE_FELICES -> ShipType.BOBCAT
		
		FactionFlavor.ISARNAREYKK -> ShipType.TEFRAN
		FactionFlavor.SWARTAREYKK -> ShipType.TEFRAN
		FactionFlavor.THEUDAREYKK -> ShipType.TEFRAN
		FactionFlavor.STAHLAREYKK -> ShipType.TEFRAN
		FactionFlavor.LYUDAREYKK -> ShipType.TEFRAN
		FactionFlavor.NEUIA_FULKREYKK -> ShipType.TEFRAN
		
		FactionFlavor.CORVUS_CLUSTER_VESTIGIUM -> ShipType.LEXINGTON
		FactionFlavor.COLEMAN_SF_BASE_VESTIGIUM -> ShipType.LEXINGTON
	}

@Serializable
data class FleetPresence(
	val name: String,
	val owner: FactionFlavor,
	val ships: Map<Id<Ship>, Ship>
)
