package net.starshipfights.campaign

import externals.threejs.Vector3
import net.starshipfights.game.Faction
import net.starshipfights.game.FactionFlavor
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class FleetRenderPosition(
	val worldPos: Vector3,
	val rotation: Double
)

enum class FleetSide {
	FRIEND,
	ENEMY;
	
	fun getPositions(numFleets: Int, worldRadius: Double): List<FleetRenderPosition> {
		val marks = (1..numFleets).map { it / (numFleets + 1.0) }
		
		val angles = marks.map { (it + 0.5) * PI }
		
		val cosFactor = when (this) {
			FRIEND -> 1.0
			ENEMY -> -1.0
		}
		
		return angles.map { theta ->
			val position = Vector3(cosFactor * cos(theta) * worldRadius, 0, sin(theta) * worldRadius)
			
			val rotation = atan2(cos(theta) * cosFactor, sin(theta))
			
			FleetRenderPosition(position, rotation)
		}
	}
}

fun getFleetSide(admiralFaction: Faction, fleetOwner: FactionFlavor) =
	if (admiralFaction in fleetOwner.loyalties)
		FleetSide.FRIEND
	else
		FleetSide.ENEMY
