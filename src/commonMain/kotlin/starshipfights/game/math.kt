@file:Suppress("NOTHING_TO_INLINE")

package starshipfights.game

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.math.*

// PLAIN OLD 2D VECTORS

@Serializable
data class Vec2(val x: Double, val y: Double)

inline operator fun Vec2.plus(other: Vec2) = Vec2(x + other.x, y + other.y)
inline operator fun Vec2.minus(other: Vec2) = Vec2(x - other.x, y - other.y)
inline operator fun Vec2.times(scale: Double) = Vec2(x * scale, y * scale)
inline operator fun Vec2.div(scale: Double) = Vec2(x / scale, y / scale)

inline operator fun Double.times(vec: Vec2) = vec * this
inline operator fun Double.div(vec: Vec2) = vec / this

inline operator fun Vec2.unaryPlus() = this
inline operator fun Vec2.unaryMinus() = this * -1.0

inline infix fun Vec2.dot(other: Vec2) = x * other.x + y * other.y
inline infix fun Vec2.cross(other: Vec2) = x * other.y - y * other.x

inline infix fun Vec2.angleBetween(other: Vec2) = acos((this dot other) / (this.magnitude * other.magnitude))
inline infix fun Vec2.angleTo(other: Vec2) = atan2(this cross other, this dot other)

inline infix fun Vec2.rotatedBy(angle: Double) = normalVector(angle).let { (c, s) -> Vec2(c * x - s * y, c * y + s * x) }
inline infix fun Vec2.scaleUneven(scalarVector: Vec2) = Vec2(x * scalarVector.x, y * scalarVector.y)

inline fun normalVector(angle: Double) = Vec2(cos(angle), sin(angle))
inline fun polarVector(radius: Double, angle: Double) = Vec2(radius * cos(angle), radius * sin(angle))

inline val Vec2.magnitude: Double
	get() = hypot(x, y)

inline val Vec2.angle: Double
	get() = atan2(y, x)

inline val Vec2.normal: Vec2
	get() = this / magnitude

// AFFINE vs DISPLACEMENT QUANTITIES

@Serializable
@JvmInline
value class Position(val vector: Vec2)

@Serializable
@JvmInline
value class Distance(val vector: Vec2)

inline operator fun Position.plus(distance: Distance) = Position(vector + distance.vector)
inline operator fun Distance.plus(position: Position) = Position(vector + position.vector)
inline operator fun Distance.plus(other: Distance) = Distance(vector + other.vector)

inline operator fun Position.minus(relativeTo: Position) = Distance(vector - relativeTo.vector)
inline operator fun Position.minus(distance: Distance) = Position(vector - distance.vector)
inline operator fun Distance.minus(other: Distance) = Distance(vector - other.vector)

inline fun Position.relativeTo(origin: Position, operation: (Distance) -> Distance) = operation(this - origin) + origin

inline operator fun Distance.times(scale: Double) = Distance(vector * scale)
inline operator fun Distance.div(scale: Double) = Distance(vector / scale)

inline operator fun Double.times(dist: Distance) = dist * this
inline operator fun Double.div(dist: Distance) = dist / this

inline operator fun Distance.unaryPlus() = this
inline operator fun Distance.unaryMinus() = Distance(-vector)

inline infix fun Distance.dot(other: Distance) = vector dot other.vector
inline infix fun Distance.cross(other: Distance) = vector cross other.vector

inline infix fun Distance.angleBetween(other: Distance) = vector angleBetween other.vector
inline infix fun Distance.angleTo(other: Distance) = vector angleTo other.vector

inline infix fun Distance.rotatedBy(angle: Double) = Distance(vector rotatedBy angle)

inline fun normalDistance(angle: Double) = Distance(normalVector(angle))
inline fun polarDistance(radius: Double, angle: Double) = Distance(polarVector(radius, angle))

inline val Distance.length: Double
	get() = vector.magnitude

inline val Distance.angle: Double
	get() = vector.angle

inline val Distance.normal: Distance
	get() = Distance(vector.normal)

inline fun Position.projectOnLineSegment(a: Position, b: Position): Position? {
	val ab = b - a
	val ar = this - a
	
	val abHat = ab.normal
	val abLen = ab.length
	
	val proj = ar dot abHat
	if (proj !in 0.0..abLen)
		return null
	
	return proj * abHat + a
}

inline fun Position.clampOnLineSegment(a: Position, b: Position): Position {
	val ab = b - a
	val ar = this - a
	
	val abHat = ab.normal
	val abLen = ab.length
	
	val proj = (ar dot abHat).coerceIn(0.0..abLen)
	return proj * abHat + a
}

inline fun Position.distanceToLineSegment(a: Position, b: Position) = (this - clampOnLineSegment(a, b)).length
