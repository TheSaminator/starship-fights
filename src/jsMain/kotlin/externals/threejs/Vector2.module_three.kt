@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface Vector

external open class Vector2(x: Number = definedExternally, y: Number = definedExternally) : Vector {
	open var x: Number
	open var y: Number
	open var width: Number
	open var height: Number
	open var isVector2: Boolean
	open fun set(x: Number, y: Number): Vector2 /* this */
	open fun setScalar(scalar: Number): Vector2 /* this */
	open fun setX(x: Number): Vector2 /* this */
	open fun setY(y: Number): Vector2 /* this */
	open fun setComponent(index: Number, value: Number): Vector2 /* this */
	open fun getComponent(index: Number): Number
	open fun clone(): Vector2 /* this */
	open fun copy(v: Vector2): Vector2 /* this */
	open fun copy(v: Vector): Vector /* this */
	open fun add(v: Vector2, w: Vector2 = definedExternally): Vector2 /* this */
	open fun addScalar(s: Number): Vector2 /* this */
	open fun addVectors(a: Vector2, b: Vector2): Vector2 /* this */
	open fun addVectors(a: Vector, b: Vector): Vector /* this */
	open fun addScaledVector(v: Vector2, s: Number): Vector2 /* this */
	open fun addScaledVector(vector: Vector, scale: Number): Vector /* this */
	open fun sub(v: Vector2): Vector2 /* this */
	open fun sub(v: Vector): Vector /* this */
	open fun subScalar(s: Number): Vector2 /* this */
	open fun subVectors(a: Vector2, b: Vector2): Vector2 /* this */
	open fun subVectors(a: Vector, b: Vector): Vector /* this */
	open fun multiply(v: Vector2): Vector2 /* this */
	open fun multiplyScalar(scalar: Number): Vector2 /* this */
	open fun divide(v: Vector2): Vector2 /* this */
	open fun divideScalar(s: Number): Vector2 /* this */
	open fun applyMatrix3(m: Matrix3): Vector2 /* this */
	open fun min(v: Vector2): Vector2 /* this */
	open fun max(v: Vector2): Vector2 /* this */
	open fun clamp(min: Vector2, max: Vector2): Vector2 /* this */
	open fun clampScalar(min: Number, max: Number): Vector2 /* this */
	open fun clampLength(min: Number, max: Number): Vector2 /* this */
	open fun floor(): Vector2 /* this */
	open fun ceil(): Vector2 /* this */
	open fun round(): Vector2 /* this */
	open fun roundToZero(): Vector2 /* this */
	open fun negate(): Vector2 /* this */
	open fun dot(v: Vector2): Number
	open fun dot(v: Vector): Number
	open fun cross(v: Vector2): Number
	open fun lengthSq(): Number
	open fun length(): Number
	open fun lengthManhattan(): Number
	open fun manhattanLength(): Number
	open fun normalize(): Vector2 /* this */
	open fun angle(): Number
	open fun distanceTo(v: Vector2): Number
	open fun distanceToSquared(v: Vector2): Number
	open fun distanceToManhattan(v: Vector2): Number
	open fun manhattanDistanceTo(v: Vector2): Number
	open fun setLength(length: Number): Vector2 /* this */
	open fun lerp(v: Vector2, alpha: Number): Vector2 /* this */
	open fun lerp(v: Vector, alpha: Number): Vector /* this */
	open fun lerpVectors(v1: Vector2, v2: Vector2, alpha: Number): Vector2 /* this */
	open fun equals(v: Vector2): Boolean
	open fun equals(v: Vector): Boolean
	open fun fromArray(array: Array<Number>, offset: Number = definedExternally): Vector2 /* this */
	open fun fromArray(array: Array<Number>): Vector2 /* this */
	open fun fromArray(array: ArrayLike<Number>, offset: Number = definedExternally): Vector2 /* this */
	open fun fromArray(array: ArrayLike<Number>): Vector2 /* this */
	open fun toArray(array: Array<Number> = definedExternally, offset: Number = definedExternally): Array<Number>
	open fun toArray(): dynamic /* Array */
	open fun toArray(array: Array<Number> = definedExternally): Array<Number>
	open fun toArray(array: Any /* JsTuple<Number, Number> */ = definedExternally, offset: Number /* 0 */ = definedExternally): dynamic /* JsTuple<Number, Number> */
	open fun toArray(array: Any /* JsTuple<Number, Number> */ = definedExternally): dynamic /* JsTuple<Number, Number> */
	open fun toArray(array: ArrayLike<Number>, offset: Number = definedExternally): ArrayLike<Number>
	open fun toArray(array: ArrayLike<Number>): ArrayLike<Number>
	open fun fromBufferAttribute(attribute: BufferAttribute, index: Number): Vector2 /* this */
	open fun rotateAround(center: Vector2, angle: Number): Vector2 /* this */
	open fun random(): Vector2 /* this */
}
