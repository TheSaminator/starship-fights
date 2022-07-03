package net.starshipfights.campaign

import net.starshipfights.data.Id
import net.starshipfights.game.FactionFlavor

fun StarClusterView.testPostProcess(): StarClusterView {
	val flavors = FactionFlavor.values().toList().shuffled()
	val ownerFlavors = sequence {
		while (true)
			for (flavor in flavors)
				yield(flavor)
	}.take(systems.size).toList()
	
	val ownedSystems = (systems.toList().shuffled() zip ownerFlavors).associate { (systemWithId, flavor) ->
		val (systemId, system) = systemWithId
		
		val numOfFleets = (0..1).random() + (0..1).random() + 1
		val fleets = (1..numOfFleets).associate { i ->
			Id<FleetPresence>("${systemId.id}-fleet-$i") to FleetPresence(
				"Test Fleet $i",
				flavor,
				emptyMap()
			)
		}
		
		systemId to system.copy(holder = flavor, fleets = fleets)
	}
	
	return copy(systems = ownedSystems)
}
