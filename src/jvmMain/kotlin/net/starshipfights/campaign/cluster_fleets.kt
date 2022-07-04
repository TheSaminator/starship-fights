package net.starshipfights.campaign

import net.starshipfights.data.Id
import net.starshipfights.data.admiralty.newShipName
import net.starshipfights.data.invoke
import net.starshipfights.game.*
import net.starshipfights.game.ai.weightedRandom

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
	}

fun genNPCFleet(owner: FactionFlavor, rank: AdmiralRank): Map<Id<Ship>, Ship> {
	val battleSize = BattleSize.values().filter { rank >= it.minRank }.associateWith { 100.0 / it.numPoints }.weightedRandom()
	
	val possibleShips = ShipType.values().filter { it.faction == owner.shipSource && it.weightClass.tier <= battleSize.maxTier }
	val maxPoints = battleSize.numPoints
	
	val chosenShipTypes = buildList {
		while (true)
			this += possibleShips.filter { ship ->
				this.sumOf { it.pointCost } + ship.pointCost <= maxPoints
			}.randomOrNull() ?: break
	}
	
	val chosenNames = mutableSetOf<String>()
	return chosenShipTypes.mapNotNull { shipType ->
		newShipName(owner.shipSource, shipType.weightClass, chosenNames)?.let { name ->
			Ship(
				id = Id(),
				name = name,
				shipType = shipType,
				shipFlavor = owner
			)
		}
	}.associateBy { it.id }
}
