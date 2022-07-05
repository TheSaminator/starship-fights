package net.starshipfights.campaign

import net.starshipfights.data.Id
import net.starshipfights.data.admiralty.AdmiralNameFlavor
import net.starshipfights.data.admiralty.AdmiralNames
import net.starshipfights.data.admiralty.newShipName
import net.starshipfights.data.invoke
import net.starshipfights.data.space.generateFleetName
import net.starshipfights.game.*
import net.starshipfights.game.ai.weightedRandom
import kotlin.math.roundToInt
import kotlin.random.Random

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

fun generateNPCFleet(owner: FactionFlavor, rank: AdmiralRank, sizeMult: Double): Map<Id<Ship>, Ship> {
	val battleSize = BattleSize.values().filter { rank.maxShipTier >= it.maxTier }.associateWith { 100.0 / it.numPoints }.weightedRandom()
	
	val possibleShips = ShipType.values().filter { it.faction == owner.shipSource && it.weightClass.tier <= battleSize.maxTier }
	val maxPoints = (battleSize.numPoints * sizeMult).roundToInt()
	
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

fun generateFleetPresences(owner: FactionFlavor, maxFleets: Int, sizeMult: Double): Map<Id<FleetPresence>, FleetPresence> = (1..(maxFleets - Random.nextDiminishingInteger(maxFleets))).associate { _ ->
	val admiralRank = AdmiralRank.values()[Random.nextIrwinHallInteger(AdmiralRank.values().size)]
	val admiralIsFemale = owner == FactionFlavor.FELINAE_FELICES || Random.nextBoolean()
	val admiralFleet = generateNPCFleet(owner, admiralRank, sizeMult)
	
	Id<FleetPresence>() to FleetPresence(
		name = owner.generateFleetName(),
		owner = owner,
		ships = admiralFleet,
		admiralName = AdmiralNames.randomName(AdmiralNameFlavor.forFactionFlavor(owner).random(), admiralIsFemale),
		admiralIsFemale = admiralIsFemale,
		admiralRank = admiralRank
	)
}
