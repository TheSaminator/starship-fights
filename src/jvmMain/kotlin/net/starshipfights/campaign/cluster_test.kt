package net.starshipfights.campaign

import net.starshipfights.data.Id
import net.starshipfights.data.admiralty.AdmiralNameFlavor
import net.starshipfights.data.admiralty.AdmiralNames
import net.starshipfights.data.invoke
import net.starshipfights.data.space.generateFleetName
import net.starshipfights.game.AdmiralRank
import net.starshipfights.game.FactionFlavor
import kotlin.random.Random

fun StarClusterView.testPostProcess(): StarClusterView {
	val flavors = FactionFlavor.values().toList().shuffled()
	val ownerFlavors = sequence {
		while (true)
			for (flavor in flavors)
				yield(flavor)
	}.take(systems.size).toList()
	
	val ownedSystems = (systems.toList().shuffled() zip ownerFlavors).associate { (systemWithId, flavor) ->
		val (systemId, system) = systemWithId
		
		val numOfFleets = (0..1).random() + (0..1).random() + (0..1).random()
		if (numOfFleets == 0)
			return@associate systemId to system
		
		val fleets = (1..numOfFleets).associate { _ ->
			val admiralRank = AdmiralRank.values().random()
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
