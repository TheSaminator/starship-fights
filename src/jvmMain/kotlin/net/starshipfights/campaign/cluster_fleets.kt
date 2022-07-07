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
		ships = admiralFleet,
		admiral = FleetPresenceAdmiral.NPC(
			name = AdmiralNames.randomName(AdmiralNameFlavor.forFactionFlavor(owner).random(), admiralIsFemale),
			isFemale = admiralIsFemale,
			faction = owner,
			rank = admiralRank
		)
	)
}
