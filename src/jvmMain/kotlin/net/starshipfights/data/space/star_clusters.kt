package net.starshipfights.data.space

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.starshipfights.campaign.*
import net.starshipfights.data.DataDocument
import net.starshipfights.data.DocumentTable
import net.starshipfights.data.Id
import net.starshipfights.data.admiralty.Admiral
import net.starshipfights.data.invoke
import net.starshipfights.game.FactionFlavor
import net.starshipfights.game.Position
import net.starshipfights.game.Ship
import org.litote.kmongo.eq

@Serializable
data class StarCluster(
	override val id: Id<StarCluster>,
	
	val background: StarClusterBackground,
	val lanes: Set<WarpLane>
) : DataDocument<StarCluster> {
	companion object Table : DocumentTable<StarCluster> by DocumentTable.create()
}

@Serializable
data class ClusterStarSystem(
	override val id: Id<ClusterStarSystem>,
	val clusterId: Id<StarCluster>,
	
	val name: String,
	val holder: FactionFlavor?,
	
	val position: Position,
	val radius: Double,
) : DataDocument<ClusterStarSystem> {
	companion object Table : DocumentTable<ClusterStarSystem> by DocumentTable.create({
		index(ClusterStarSystem::clusterId)
	})
}

@Serializable
data class ClusterCelestialObject(
	override val id: Id<ClusterCelestialObject>,
	val starSystemId: Id<ClusterStarSystem>,
	
	val celestialObject: CelestialObject
) : DataDocument<ClusterCelestialObject> {
	companion object Table : DocumentTable<ClusterCelestialObject> by DocumentTable.create({
		index(ClusterCelestialObject::starSystemId)
	})
}

@Serializable
data class ClusterFleetPresence(
	override val id: Id<ClusterFleetPresence>,
	val starSystemId: Id<ClusterStarSystem>,
	
	val fleetPresence: FleetPresence
) : DataDocument<ClusterFleetPresence> {
	companion object Table : DocumentTable<ClusterFleetPresence> by DocumentTable.create({
		index(ClusterFleetPresence::starSystemId)
	})
}

suspend fun deleteCluster(clusterId: Id<StarCluster>) {
	coroutineScope {
		launch { StarCluster.del(clusterId) }
		launch {
			ClusterStarSystem.filter(ClusterStarSystem::clusterId eq clusterId).collect { cSystem ->
				launch {
					ClusterCelestialObject.remove(ClusterCelestialObject::starSystemId eq cSystem.id)
				}
				launch {
					ClusterFleetPresence.remove(ClusterFleetPresence::starSystemId eq cSystem.id)
				}
				launch {
					ClusterStarSystem.del(cSystem.id)
				}
			}
		}
	}
}

suspend fun createCluster(clusterView: StarClusterView): Id<StarCluster> {
	val cluster = StarCluster(
		id = Id(),
		background = clusterView.background,
		lanes = clusterView.lanes
	)
	
	coroutineScope {
		launch { StarCluster.put(cluster) }
		launch {
			for ((systemId, system) in clusterView.systems) {
				val clusterSystem = ClusterStarSystem(
					id = systemId.reinterpret(),
					clusterId = cluster.id,
					name = system.name,
					holder = system.holder,
					position = system.position,
					radius = system.radius
				)
				
				launch { ClusterStarSystem.put(clusterSystem) }
				launch {
					ClusterCelestialObject.put(
						system.bodies.map { (bodyId, body) ->
							ClusterCelestialObject(
								id = bodyId.reinterpret(),
								starSystemId = clusterSystem.id,
								celestialObject = body
							)
						}
					)
				}
				launch {
					ClusterFleetPresence.put(
						system.fleets.map { (fleetId, fleet) ->
							ClusterFleetPresence(
								id = fleetId.reinterpret(),
								starSystemId = clusterSystem.id,
								fleetPresence = fleet
							)
						}
					)
				}
			}
		}
	}
	
	return cluster.id
}

suspend fun viewCluster(clusterId: Id<StarCluster>): StarClusterView? {
	return coroutineScope {
		val clusterAsync = async { StarCluster.get(clusterId) }
		val systemsAsync = async {
			ClusterStarSystem.filter(ClusterStarSystem::clusterId eq clusterId).map { cSystem ->
				async {
					val bodiesAsync = async {
						val bodies = ClusterCelestialObject.filter(ClusterCelestialObject::starSystemId eq cSystem.id).toList()
						
						bodies.associate { cBody ->
							cBody.id.reinterpret<CelestialObject>() to cBody.celestialObject
						}
					}
					val fleetsAsync = async {
						val fleets = ClusterFleetPresence.filter(ClusterFleetPresence::starSystemId eq cSystem.id).toList()
						
						fleets.associate { cFleet ->
							cFleet.id.reinterpret<FleetPresence>() to cFleet.fleetPresence
						}
					}
					
					cSystem.id.reinterpret<StarSystem>() to StarSystem(
						cSystem.name,
						cSystem.holder,
						fleetsAsync.await(),
						cSystem.position,
						cSystem.radius,
						bodiesAsync.await()
					)
				}
			}.toList().associate { it.await() }
		}
		
		val cluster = clusterAsync.await() ?: return@coroutineScope null
		val systems = systemsAsync.await()
		
		StarClusterView(
			background = cluster.background,
			systems = systems,
			lanes = cluster.lanes
		)
	}
}

suspend fun deployableFleet(systemId: Id<ClusterStarSystem>, admiral: Admiral): Map<Id<Ship>, Ship> {
	return ClusterFleetPresence.filter(ClusterFleetPresence::starSystemId eq systemId)
		.toList()
		.filter { admiral.faction in it.fleetPresence.owner.loyalties }
		.flatMap {
			it.fleetPresence.ships.toList().filter { (_, ship) ->
				ship.shipType.weightClass.tier <= admiral.rank.maxShipTier
			}
		}
		.toMap()
}
