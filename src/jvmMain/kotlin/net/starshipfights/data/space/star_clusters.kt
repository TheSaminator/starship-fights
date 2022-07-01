package net.starshipfights.data.space

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.starshipfights.campaign.*
import net.starshipfights.data.DataDocument
import net.starshipfights.data.DocumentTable
import net.starshipfights.data.Id
import net.starshipfights.game.FactionFlavor
import net.starshipfights.game.Position

@Serializable
data class StarCluster(
	override val id: Id<StarCluster>,
	
	val background: StarClusterBackground,
	val systems: Set<Id<ClusterStarSystem>>,
	val lanes: Set<WarpLane>
) : DataDocument<StarCluster> {
	companion object Table : DocumentTable<StarCluster> by DocumentTable.create()
}

@Serializable
data class ClusterStarSystem(
	override val id: Id<ClusterStarSystem>,
	
	val name: String,
	val holder: FactionFlavor?,
	val fleets: Set<Id<ClusterFleetPresence>>,
	
	val position: Position,
	val radius: Double,
	val bodies: Set<Id<ClusterCelestialObject>>,
) : DataDocument<ClusterStarSystem> {
	companion object Table : DocumentTable<ClusterStarSystem> by DocumentTable.create()
}

@Serializable
data class ClusterCelestialObject(
	override val id: Id<ClusterCelestialObject>,
	
	val celestialObject: CelestialObject
) : DataDocument<ClusterCelestialObject> {
	companion object Table : DocumentTable<ClusterCelestialObject> by DocumentTable.create()
}

@Serializable
data class ClusterFleetPresence(
	override val id: Id<ClusterFleetPresence>,
	
	val fleetPresence: FleetPresence
) : DataDocument<ClusterFleetPresence> {
	companion object Table : DocumentTable<ClusterFleetPresence> by DocumentTable.create()
}

suspend fun deleteCluster(clusterId: Id<StarCluster>) {
	coroutineScope {
		val cluster = StarCluster.get(clusterId) ?: return@coroutineScope
		
		for (systemId in cluster.systems)
			launch {
				val system = ClusterStarSystem.get(systemId) ?: return@launch
				
				for (bodyId in system.bodies)
					launch {
						ClusterCelestialObject.del(bodyId)
					}
				
				for (fleetId in system.fleets)
					launch {
						ClusterFleetPresence.del(fleetId)
					}
				
				ClusterStarSystem.del(systemId)
			}
		
		StarCluster.del(clusterId)
	}
}

suspend fun view(clusterId: Id<StarCluster>): StarClusterView? {
	return coroutineScope {
		val cluster = StarCluster.get(clusterId) ?: return@coroutineScope null
		
		val systems = cluster.systems.map { systemId ->
			systemId.reinterpret<StarSystem>() to async {
				val system = ClusterStarSystem.get(systemId) ?: return@async null
				
				val bodiesAsync = async {
					system.bodies
						.map { bodyId ->
							bodyId to async { ClusterCelestialObject.get(bodyId) }
						}
						.mapNotNull { (id, deferred) ->
							deferred.await()?.let { id to it }
						}
						.associate { (id, clusterObject) ->
							id.reinterpret<CelestialObject>() to clusterObject.celestialObject
						}
				}
				
				val fleetsAsync = async {
					system.fleets
						.map { fleetId ->
							fleetId to async { ClusterFleetPresence.get(fleetId) }
						}
						.mapNotNull { (id, deferred) ->
							deferred.await()?.let { id to it }
						}
						.associate { (id, clusterFleet) ->
							id.reinterpret<FleetPresence>() to clusterFleet.fleetPresence
						}
				}
				
				val bodies = bodiesAsync.await()
				val fleets = fleetsAsync.await()
				
				StarSystem(
					name = system.name,
					holder = system.holder,
					fleets = fleets,
					position = system.position,
					radius = system.radius,
					bodies = bodies
				)
			}
		}.mapNotNull { (id, deferred) ->
			deferred.await()?.let { id to it }
		}.toMap()
		
		StarClusterView(
			background = cluster.background,
			systems = systems,
			lanes = cluster.lanes
		)
	}
}
