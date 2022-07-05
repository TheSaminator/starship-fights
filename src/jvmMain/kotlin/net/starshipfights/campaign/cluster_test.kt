package net.starshipfights.campaign

import net.starshipfights.data.Id
import net.starshipfights.data.admiralty.AdmiralNameFlavor
import net.starshipfights.data.admiralty.AdmiralNames
import net.starshipfights.data.invoke
import net.starshipfights.data.space.generateFleetName
import net.starshipfights.game.*
import kotlin.random.Random

fun StarClusterView.testPostProcess(): StarClusterView {
	val ownerFlavors = FactionFlavor.values()
		.toList()
		.shuffled()
		.repeatForever()
		.take(systems.size)
		.toList()
	
	val ownedSystems = (systems.toList().shuffled() zip ownerFlavors).associate { (systemWithId, flavor) ->
		val (systemId, system) = systemWithId
		
		val numOfFleets = 3 - Random.nextDiminishingInteger(4)
		if (numOfFleets == 0)
			return@associate systemId to system
		
		val fleets = (1..numOfFleets).associate { _ ->
			val admiralRank = AdmiralRank.values()[Random.nextIrwinHallInteger(AdmiralRank.values().size)]
			val admiralIsFemale = flavor == FactionFlavor.FELINAE_FELICES || Random.nextBoolean()
			val admiralFleet = generateNPCFleet(flavor, admiralRank)
			
			Id<FleetPresence>() to FleetPresence(
				name = flavor.generateFleetName(),
				owner = flavor,
				ships = admiralFleet,
				admiralName = AdmiralNames.randomName(AdmiralNameFlavor.forFactionFlavor(flavor).random(), admiralIsFemale),
				admiralIsFemale = admiralIsFemale,
				admiralRank = admiralRank
			)
		}
		
		systemId to system.copy(holder = flavor, fleets = fleets)
	}
	
	return copy(systems = ownedSystems)
}
