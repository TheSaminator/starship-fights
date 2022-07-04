package net.starshipfights.campaign

import net.starshipfights.data.Id
import net.starshipfights.data.admiralty.AdmiralNameFlavor
import net.starshipfights.data.admiralty.AdmiralNames
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
		
		val numOfFleets = (0..2).random() + (0..2).random() + 1
		val fleets = (1..numOfFleets).associate { i ->
			val admiralIsFemale = flavor == FactionFlavor.FELINAE_FELICES || Random.nextBoolean()
			
			Id<FleetPresence>("${systemId.id}-fleet-$i") to FleetPresence(
				name = "Test Fleet $i",
				owner = flavor,
				ships = emptyMap(),
				admiralName = AdmiralNames.randomName(AdmiralNameFlavor.forFactionFlavor(flavor).random(), admiralIsFemale),
				admiralIsFemale = admiralIsFemale,
				admiralRank = AdmiralRank.values().random()
			)
		}
		
		systemId to system.copy(holder = flavor, fleets = fleets)
	}
	
	return copy(systems = ownedSystems)
}
