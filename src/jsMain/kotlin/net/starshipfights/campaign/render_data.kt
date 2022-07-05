package net.starshipfights.campaign

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id

fun WarpLane.resolve(cluster: StarClusterView): WarpLaneData? =
	systemA.resolve(cluster)?.let { a ->
		systemB.resolve(cluster)?.let { b ->
			WarpLaneData(a, b)
		}
	}

fun WarpLane.withData(cluster: StarClusterView): WarpLaneWithData? =
	systemA.resolve(cluster)?.let { a ->
		systemB.resolve(cluster)?.let { b ->
			WarpLaneWithData(StarSystemWithId(systemA, a), StarSystemWithId(systemB, b))
		}
	}

@Serializable
data class WarpLaneData(
	val systemA: StarSystem,
	val systemB: StarSystem,
)

@Serializable
data class WarpLaneWithData(
	val systemA: StarSystemWithId,
	val systemB: StarSystemWithId,
) {
	val lane: WarpLane
		get() = WarpLane(systemA.id, systemB.id)
	
	val data: WarpLaneData
		get() = WarpLaneData(systemA.starSystem, systemB.starSystem)
}

fun Id<StarSystem>.resolve(cluster: StarClusterView): StarSystem? = cluster.systems[this]

@Serializable
data class StarSystemWithId(
	val id: Id<StarSystem>,
	val starSystem: StarSystem
)

@Serializable
data class CelestialObjectPointer(
	val starSystem: Id<StarSystem>,
	val celestialObject: Id<CelestialObject>
)

fun CelestialObjectPointer.resolve(cluster: StarClusterView): CelestialObject? = starSystem.resolve(cluster)?.bodies?.get(celestialObject)

@Serializable
data class CelestialObjectWithPointer<T : CelestialObject>(
	val id: CelestialObjectPointer,
	val celestialObject: T
)

@Serializable
data class FleetPresencePointer(
	val starSystem: Id<StarSystem>,
	val fleetPresence: Id<FleetPresence>
)

fun FleetPresencePointer.resolve(cluster: StarClusterView): FleetPresence? = starSystem.resolve(cluster)?.fleets?.get(fleetPresence)

@Serializable
data class FleetPresenceWithPointer(
	val id: FleetPresencePointer,
	val fleetPresence: FleetPresence
)
