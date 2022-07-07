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
		FactionFlavor.NEW_AUSTIN_VESTIGIUM -> listOf(Faction.VESTIGIUM)
	}

val FactionFlavor.shipSource: Faction
	get() = when (this) {
		FactionFlavor.MECHYRDIA -> Faction.MECHYRDIA
		FactionFlavor.TYLA -> Faction.MECHYRDIA
		FactionFlavor.OLYMPIA -> Faction.MECHYRDIA
		FactionFlavor.TEXANDRIA -> Faction.MECHYRDIA
		
		FactionFlavor.NDRC -> Faction.NDRC
		FactionFlavor.CCC -> Faction.NDRC
		FactionFlavor.MJOLNIR_ENERGY -> Faction.NDRC
		
		FactionFlavor.MASRA_DRAETSEN -> Faction.MASRA_DRAETSEN
		FactionFlavor.AEDON_CULTISTS -> Faction.MASRA_DRAETSEN
		FactionFlavor.FERTHLON_EXILES -> Faction.MASRA_DRAETSEN
		
		FactionFlavor.RES_NOSTRA -> Faction.FELINAE_FELICES
		FactionFlavor.CORSAIRS -> Faction.FELINAE_FELICES
		FactionFlavor.FELINAE_FELICES -> Faction.FELINAE_FELICES
		
		FactionFlavor.ISARNAREYKK -> Faction.ISARNAREYKK
		FactionFlavor.SWARTAREYKK -> Faction.ISARNAREYKK
		FactionFlavor.THEUDAREYKK -> Faction.ISARNAREYKK
		FactionFlavor.STAHLAREYKK -> Faction.ISARNAREYKK
		FactionFlavor.LYUDAREYKK -> Faction.ISARNAREYKK
		FactionFlavor.NEUIA_FULKREYKK -> Faction.ISARNAREYKK
		
		FactionFlavor.CORVUS_CLUSTER_VESTIGIUM -> Faction.VESTIGIUM
		FactionFlavor.COLEMAN_SF_BASE_VESTIGIUM -> Faction.VESTIGIUM
		FactionFlavor.NEW_AUSTIN_VESTIGIUM -> Faction.VESTIGIUM
	}

@Serializable
data class FleetPresence(
	val name: String,
	val ships: Map<Id<Ship>, Ship>,
	
	val admiral: FleetPresenceAdmiral
) {
	val owner: FactionFlavor
		get() = admiral.faction
	
	val pointValue: Int
		get() = ships.values.sumOf { it.pointCost }
	
	val admiralFullName: String
		get() = admiral.fullName
}

@Serializable
sealed class FleetPresenceAdmiral {
	abstract val name: String
	abstract val isFemale: Boolean
	abstract val faction: FactionFlavor
	abstract val rank: AdmiralRank
	
	val fullName: String
		get() = "${rank.getDisplayName(faction)} $name"
	
	@Serializable
	data class NPC(
		override val name: String,
		override val isFemale: Boolean,
		override val faction: FactionFlavor,
		override val rank: AdmiralRank,
	) : FleetPresenceAdmiral()
	
	@Serializable
	data class Player(val admiral: CampaignAdmiral) : FleetPresenceAdmiral() {
		override val name: String
			get() = admiral.admiral.name
		
		override val isFemale: Boolean
			get() = admiral.admiral.isFemale
		
		override val faction: FactionFlavor
			get() = FactionFlavor.defaultForFaction(admiral.admiral.faction)
		
		override val rank: AdmiralRank
			get() = admiral.admiral.rank
	}
}
