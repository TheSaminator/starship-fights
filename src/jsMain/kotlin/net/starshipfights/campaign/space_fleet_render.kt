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
	get() = when (this) {
		FactionFlavor.MECHYRDIA -> IntColor(255, 204, 51)
		FactionFlavor.TYLA -> IntColor(255, 204, 51)
		FactionFlavor.OLYMPIA -> IntColor(255, 204, 51)
		FactionFlavor.TEXANDRIA -> IntColor(255, 204, 51)
		
		FactionFlavor.NDRC -> IntColor(255, 204, 51)
		FactionFlavor.CCC -> IntColor(255, 204, 51)
		FactionFlavor.MJOLNIR_ENERGY -> IntColor(255, 204, 51)
		
		FactionFlavor.MASRA_DRAETSEN -> IntColor(204, 34, 34)
		FactionFlavor.AEDON_CULTISTS -> IntColor(204, 34, 34)
		FactionFlavor.FERTHLON_EXILES -> IntColor(204, 34, 34)
		
		FactionFlavor.RES_NOSTRA -> IntColor(204, 102, 153)
		FactionFlavor.CORSAIRS -> IntColor(204, 102, 153)
		FactionFlavor.FELINAE_FELICES -> IntColor(204, 102, 153)
		
		FactionFlavor.ISARNAREYKK -> IntColor(34, 221, 34)
		FactionFlavor.SWARTAREYKK -> IntColor(34, 221, 34)
		FactionFlavor.THEUDAREYKK -> IntColor(255, 204, 51) // Mechyrdia
		FactionFlavor.STAHLAREYKK -> IntColor(255, 204, 51) // Also Mechyrdia
		FactionFlavor.LYUDAREYKK -> IntColor(34, 221, 34)
		FactionFlavor.NEUIA_FULKREYKK -> IntColor(34, 221, 34)
		
		FactionFlavor.CORVUS_CLUSTER_VESTIGIUM -> IntColor(108, 96, 153)
		FactionFlavor.COLEMAN_SF_BASE_VESTIGIUM -> IntColor(108, 96, 153)
	}

val FactionFlavor.mapCounterShipClass: ShipType
	get() = when (this) {
		FactionFlavor.MECHYRDIA -> ShipType.VENSCA
		FactionFlavor.TYLA -> ShipType.VENSCA
		FactionFlavor.OLYMPIA -> ShipType.VENSCA
		FactionFlavor.TEXANDRIA -> ShipType.VENSCA
		
		FactionFlavor.NDRC -> ShipType.KRIJGSCHUIT
		FactionFlavor.CCC -> ShipType.KRIJGSCHUIT
		FactionFlavor.MJOLNIR_ENERGY -> ShipType.KRIJGSCHUIT
		
		FactionFlavor.MASRA_DRAETSEN -> ShipType.AZATHOTH
		FactionFlavor.AEDON_CULTISTS -> ShipType.AZATHOTH
		FactionFlavor.FERTHLON_EXILES -> ShipType.AZATHOTH
		
		FactionFlavor.RES_NOSTRA -> ShipType.BOBCAT
		FactionFlavor.CORSAIRS -> ShipType.BOBCAT
		FactionFlavor.FELINAE_FELICES -> ShipType.BOBCAT
		
		FactionFlavor.ISARNAREYKK -> ShipType.KHORR
		FactionFlavor.SWARTAREYKK -> ShipType.KHORR
		FactionFlavor.THEUDAREYKK -> ShipType.KHORR
		FactionFlavor.STAHLAREYKK -> ShipType.KHORR
		FactionFlavor.LYUDAREYKK -> ShipType.KHORR
		FactionFlavor.NEUIA_FULKREYKK -> ShipType.KHORR
		
		FactionFlavor.CORVUS_CLUSTER_VESTIGIUM -> ShipType.IOWA
		FactionFlavor.COLEMAN_SF_BASE_VESTIGIUM -> ShipType.IOWA
	}
