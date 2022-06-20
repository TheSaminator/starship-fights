package net.starshipfights.game

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id

@Serializable
data class GameStart(
	val battlefieldWidth: Double,
	val battlefieldLength: Double,
	
	val hostStarts: Map<String, PlayerStart>,
	val guestStarts: Map<String, PlayerStart>
)

fun GameStart.playerStart(side: GlobalShipController) = when (side.side) {
	GlobalSide.HOST -> hostStarts.getValue(side.disambiguation)
	GlobalSide.GUEST -> guestStarts.getValue(side.disambiguation)
}

@Serializable
data class PlayerStart(
	val cameraPosition: Position,
	val cameraFacing: Double,
	
	val deployZone: PickBoundary.Rectangle,
	val deployFacing: Double,
	
	val deployableFleet: Map<Id<Ship>, Ship>,
	val deployPointsFactor: Double = 1.0,
)
