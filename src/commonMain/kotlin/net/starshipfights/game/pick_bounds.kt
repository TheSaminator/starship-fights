package net.starshipfights.game

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id
import kotlin.math.PI
import kotlin.math.abs

fun FiringArc.getStartAngle(shipFacing: Double) = (when (this) {
	FiringArc.BOW -> Vec2(1.0, -1.0)
	FiringArc.ABEAM_PORT -> Vec2(-1.0, -1.0)
	FiringArc.ABEAM_STARBOARD -> Vec2(1.0, 1.0)
	FiringArc.STERN -> Vec2(-1.0, 1.0)
} rotatedBy shipFacing).angle

fun FiringArc.getEndAngle(shipFacing: Double) = (when (this) {
	FiringArc.BOW -> Vec2(1.0, 1.0)
	FiringArc.ABEAM_PORT -> Vec2(1.0, -1.0)
	FiringArc.ABEAM_STARBOARD -> Vec2(-1.0, 1.0)
	FiringArc.STERN -> Vec2(-1.0, -1.0)
} rotatedBy shipFacing).angle

fun GameState.isValidPick(request: PickRequest, response: PickResponse): Boolean {
	if (request.type is PickType.Ship != response is PickResponse.Ship)
		return false
	
	when (response) {
		is PickResponse.Location -> {
			if (request.type !is PickType.Location) return false
			
			if (response.position !in request.boundary) return false
			if (ships.values.any {
					it.id in request.type.excludesNearShips && (it.position.location - response.position).length <= SHIP_BASE_SIZE
				}) return false
			
			return true
		}
		is PickResponse.Ship -> {
			if (request.type !is PickType.Ship) return false
			
			if (response.id !in ships) return false
			
			val ship = ships.getValue(response.id)
			if (ship.position.location !in request.boundary) return false
			if (ship.owner !in request.type.allowSides) return false
			
			return true
		}
	}
}

@Serializable
data class PickRequest(val type: PickType, val boundary: PickBoundary)

@Serializable
sealed class PickResponse {
	@Serializable
	data class Location(val position: Position) : PickResponse()
	
	@Serializable
	data class Ship(val id: Id<ShipInstance>) : PickResponse()
}

@Serializable
sealed class PickType {
	@Serializable
	data class Location(val excludesNearShips: Set<Id<ShipInstance>>, val helper: PickHelper, val drawLineFrom: Position? = null) : PickType()
	
	@Serializable
	data class Ship(val allowSides: Set<GlobalSide>) : PickType()
}

@Serializable
sealed class PickBoundary {
	abstract operator fun contains(point: Position): Boolean
	open fun normalize(point: Position) = point
	
	@Serializable
	data class Angle(
		val center: Position,
		val midAngle: Double,
		val maxAngle: Double
	) : PickBoundary() {
		override fun contains(point: Position): Boolean {
			val midNormal = normalDistance(midAngle)
			return (point - center) angleBetween midNormal <= maxAngle
		}
	}
	
	@Serializable
	data class AlongLine(
		val pointA: Position,
		val pointB: Position
	) : PickBoundary() {
		override fun contains(point: Position) = true
		
		override fun normalize(point: Position): Position {
			return point.clampOnLineSegment(pointA, pointB)
		}
	}
	
	@Serializable
	data class Rectangle(
		val center: Position,
		val width2: Double,
		val length2: Double
	) : PickBoundary() {
		override fun contains(point: Position): Boolean {
			return (point - center).vector.let { (x, y) ->
				abs(x) <= width2 && abs(y) <= length2
			}
		}
	}
	
	@Serializable
	data class Circle(
		val center: Position,
		val radius: Double,
	) : PickBoundary() {
		override fun contains(point: Position): Boolean {
			return (point - center).length < radius
		}
	}
	
	@Serializable
	data class WeaponsFire(
		val center: Position,
		val facing: Double,
		val minDistance: Double,
		val maxDistance: Double,
		val firingArcs: Set<FiringArc>,
		
		val canSelfSelect: Boolean = false
	) : PickBoundary() {
		override fun contains(point: Position): Boolean {
			if (canSelfSelect && (point - center).length < EPSILON)
				return true
			
			val r = point - center
			if (r.length !in minDistance..maxDistance)
				return false
			
			val rHat = r.normal
			val thetaHat = normalDistance(facing)
			
			val deltaTheta = thetaHat angleTo rHat
			val firingArc: FiringArc = when {
				abs(deltaTheta) < PI / 4 -> FiringArc.BOW
				abs(deltaTheta) > PI * 3 / 4 -> FiringArc.STERN
				deltaTheta < 0 -> FiringArc.ABEAM_PORT
				else -> FiringArc.ABEAM_STARBOARD
			}
			
			return firingArc in firingArcs
		}
	}
}

@Serializable
sealed class PickHelper {
	@Serializable
	object None : PickHelper()
	
	@Serializable
	data class Ship(val type: ShipType, val facing: Double) : PickHelper()
	
	@Serializable
	data class Circle(val radius: Double) : PickHelper()
}

fun PickBoundary.closestPointTo(position: Position): Position = when (this) {
	is PickBoundary.AlongLine -> position.clampOnLineSegment(pointA, pointB)
	is PickBoundary.Angle -> {
		val distance = position - center
		val midNormal = normalDistance(midAngle)
		
		if ((distance angleBetween midNormal) <= maxAngle)
			position
		else
			((midNormal rotatedBy (midNormal angleTo distance).coerceIn(-maxAngle..maxAngle)) * distance.length) + center
	}
	is PickBoundary.Circle -> {
		val distance = position - center
		if (distance.length <= radius)
			position
		else
			(distance.normal * radius) + center
	}
	is PickBoundary.Rectangle -> {
		Distance((position - center).vector.let { (x, y) ->
			Vec2(x.coerceIn(-width2..width2), y.coerceIn(-length2..length2))
		}) + center
	}
	is PickBoundary.WeaponsFire -> {
		val distance = position - center
		
		val thetaHat = normalDistance(facing)
		
		val deltaTheta = thetaHat angleTo distance
		val firingArc: FiringArc = when {
			abs(deltaTheta) < PI / 4 -> FiringArc.BOW
			abs(deltaTheta) > PI * 3 / 4 -> FiringArc.STERN
			deltaTheta < 0 -> FiringArc.ABEAM_PORT
			else -> FiringArc.ABEAM_STARBOARD
		}
		
		if (firingArc in firingArcs) {
			if (distance.length in minDistance..maxDistance)
				position
			else
				(distance.normal * (if (distance.length < minDistance) minDistance else maxDistance)) + center
		} else
			firingArcs.flatMap {
				val startNormal = normalDistance(it.getStartAngle(facing))
				val endNormal = normalDistance(it.getEndAngle(facing))
				
				listOf(
					(startNormal * minDistance) + center,
					(endNormal * minDistance) + center,
					(startNormal * maxDistance) + center,
					(endNormal * maxDistance) + center,
				)
			}.minByOrNull { (it - position).length } ?: position
	}
}
