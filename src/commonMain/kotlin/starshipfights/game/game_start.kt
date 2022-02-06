package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id

@Serializable
data class GameStart(
	val battlefieldWidth: Double,
	val battlefieldLength: Double,
	
	val hostStart: PlayerStart,
	val guestStart: PlayerStart
)

fun GameStart.playerStart(side: GlobalSide) = when (side) {
	GlobalSide.HOST -> hostStart
	GlobalSide.GUEST -> guestStart
}

@Serializable
data class PlayerStart(
	val cameraPosition: Position,
	val cameraFacing: Double,
	
	val deployZone: PickBoundary.Rectangle,
	val deployFacing: Double,
	
	val deployableFleet: Map<Id<Ship>, Ship>
)
