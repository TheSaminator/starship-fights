package net.starshipfights.data.space

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.starshipfights.campaign.*
import net.starshipfights.data.DataDocument
import net.starshipfights.data.DocumentTable
import net.starshipfights.data.Id
import net.starshipfights.data.admiralty.Admiral
import net.starshipfights.data.admiralty.ShipInDrydock
import net.starshipfights.data.admiralty.getInGameAdmiral
import net.starshipfights.data.invoke
import net.starshipfights.game.FactionFlavor
import net.starshipfights.game.Position
import net.starshipfights.game.Ship
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

@Serializable
data class StarCluster(
	override val id: Id<StarCluster>,
	val host: Id<Admiral>,
	
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
	
	val fleetPresence: FleetPresenceData
) : DataDocument<ClusterFleetPresence> {
	companion object Table : DocumentTable<ClusterFleetPresence> by DocumentTable.create({
		index(ClusterFleetPresence::starSystemId)
	})
}

@Serializable
sealed class FleetPresenceData {
	@Serializable
	data class NPC(
		val name: String,
		val ships: Map<Id<Ship>, Ship>,
		val admiral: FleetPresenceAdmiral.NPC
	) : FleetPresenceData()
	
	@Serializable
	data class Player(
		val admiralId: Id<Admiral>
	) : FleetPresenceData()
}

suspend fun getCampaignStatus(admiral: Admiral, clusterId: Id<StarCluster>): CampaignAdmiralStatus? {
	val cluster = StarCluster.get(clusterId) ?: return null
	
	admiral.inCluster?.let { inCluster ->
		if (inCluster == clusterId) {
			return if (cluster.host == admiral.id)
				CampaignAdmiralStatus.HOST
			else
				CampaignAdmiralStatus.MEMBER
		} else if (cluster.host == admiral.id) {
			Admiral.set(admiral.id, setValue(Admiral::inCluster, clusterId))
			return CampaignAdmiralStatus.HOST
		}
	}
	
	return if (clusterId in admiral.invitedToClusters)
		CampaignAdmiralStatus.INVITED
	else null
}

suspend fun FleetPresenceData.resolve(inCluster: Id<StarCluster>): FleetPresence? {
	return when (this) {
		is FleetPresenceData.NPC -> FleetPresence(
			name = name,
			ships = ships,
			admiral = admiral
		)
		is FleetPresenceData.Player -> {
			val (admiral, ships) = coroutineScope {
				val admiralAsync = async { Admiral.get(admiralId) }
				val shipsAsync = async { ShipInDrydock.filter(ShipInDrydock::owningAdmiral eq admiralId).toList() }
				
				admiralAsync.await() to shipsAsync.await()
			}
			
			admiral ?: return null
			val inGameAdmiral = getInGameAdmiral(admiral) ?: return null
			val campaignStatus = getCampaignStatus(admiral, inCluster) ?: return null
			
			FleetPresence(
				name = "Fleet of ${admiral.fullName}",
				ships = ships.associate { inDrydock ->
					inDrydock.id.reinterpret<Ship>() to inDrydock.shipData
				},
				admiral = FleetPresenceAdmiral.Player(
					CampaignAdmiral(inGameAdmiral, campaignStatus)
				)
			)
		}
	}
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

suspend fun createCluster(clusterView: StarClusterView, forAdmiral: Id<Admiral>): Id<StarCluster> {
	val cluster = StarCluster(
		id = Id(),
		host = forAdmiral,
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
								fleetPresence = when (val admiral = fleet.admiral) {
									is FleetPresenceAdmiral.NPC -> FleetPresenceData.NPC(
										name = fleet.name,
										ships = fleet.ships,
										admiral = admiral
									)
									is FleetPresenceAdmiral.Player -> FleetPresenceData.Player(
										admiral.admiral.admiral.id.reinterpret()
									)
								}
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
						ClusterFleetPresence.filter(ClusterFleetPresence::starSystemId eq cSystem.id).map { fleet ->
							async {
								fleet.fleetPresence.resolve(clusterId)?.let { fleet.id.reinterpret<FleetPresence>() to it }
							}
						}.mapNotNull { it.await() }.toList().toMap()
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
			}.map { it.await() }.toList().toMap()
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
