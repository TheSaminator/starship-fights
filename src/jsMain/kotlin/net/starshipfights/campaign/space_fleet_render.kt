package net.starshipfights.campaign

import externals.threejs.Object3D
import externals.threejs.Vector3
import net.starshipfights.game.Faction
import net.starshipfights.game.FactionFlavor
import net.starshipfights.game.IntColor
import net.starshipfights.game.ShipType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class FleetRenderPosition(
	val worldPos: Vector3,
	val rotation: Double
)

fun Object3D.applyRenderPosition(renderPosition: FleetRenderPosition) {
	position.copy(renderPosition.worldPos)
	CampaignScaling.toWorldRotation(renderPosition.rotation, this)
}

enum class FleetSide {
	FRIEND,
	ENEMY;
	
	fun getPositions(numFleets: Int, worldRadius: Double, worldCenter: Vector3): List<FleetRenderPosition> {
		val marks = (1..numFleets).map { it / (numFleets + 1.0) }
		
		val angles = marks.map { (it + 0.5) * PI }
		
		val cosFactor = when (this) {
			FRIEND -> -1.0
			ENEMY -> 1.0
		}
		
		return angles.map { theta ->
			val position = Vector3(cosFactor * cos(theta) * worldRadius, 14.4, sin(theta) * worldRadius).add(worldCenter)
			
			val rotation = atan2(-cos(theta) * cosFactor, -sin(theta))
			
			FleetRenderPosition(position, rotation)
		}
	}
}

fun getFleetSide(admiralFaction: Faction, fleetOwner: FactionFlavor) =
	if (admiralFaction in fleetOwner.loyalties)
		FleetSide.FRIEND
	else
		FleetSide.ENEMY

fun getFleetSide(fleetOwner: FactionFlavor) =
	mySide?.admiral?.faction?.let { admiralFaction ->
		getFleetSide(admiralFaction, fleetOwner)
	} ?: FleetSide.ENEMY

val FactionFlavor.mapColor: IntColor
	get() = when (loyalties.first()) {
		Faction.MECHYRDIA -> IntColor(255, 204, 51)
		Faction.NDRC -> IntColor(255, 204, 51)
		Faction.MASRA_DRAETSEN -> IntColor(204, 34, 34)
		Faction.FELINAE_FELICES -> IntColor(204, 102, 153)
		Faction.ISARNAREYKK -> IntColor(34, 221, 34)
		Faction.VESTIGIUM -> IntColor(108, 96, 153)
	}

val FactionFlavor.mapCounterShipClass: ShipType
	get() = when (shipSource) {
		Faction.MECHYRDIA -> ShipType.VENSCA
		Faction.NDRC -> ShipType.KRIJGSCHUIT
		Faction.MASRA_DRAETSEN -> ShipType.AZATHOTH
		Faction.FELINAE_FELICES -> ShipType.BOBCAT
		Faction.ISARNAREYKK -> ShipType.KHORR
		Faction.VESTIGIUM -> ShipType.IOWA
	}
