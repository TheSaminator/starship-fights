@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Plane(normal: Vector3 = definedExternally, constant: Number = definedExternally) {
	open var normal: Vector3
	open var constant: Number
	open var isPlane: Boolean
	open fun set(normal: Vector3, constant: Number): Plane
	open fun setComponents(x: Number, y: Number, z: Number, w: Number): Plane
	open fun setFromNormalAndCoplanarPoint(normal: Vector3, point: Vector3): Plane
	open fun setFromCoplanarPoints(a: Vector3, b: Vector3, c: Vector3): Plane
	open fun clone(): Plane /* this */
	open fun copy(plane: Plane): Plane /* this */
	open fun normalize(): Plane
	open fun negate(): Plane
	open fun distanceToPoint(point: Vector3): Number
	open fun distanceToSphere(sphere: Sphere): Number
	open fun projectPoint(point: Vector3, target: Vector3): Vector3
	open fun orthoPoint(point: Vector3, target: Vector3): Vector3
	open fun intersectLine(line: Line3, target: Vector3): Vector3?
	open fun intersectsLine(line: Line3): Boolean
	open fun intersectsBox(box: Box3): Boolean
	open fun intersectsSphere(sphere: Sphere): Boolean
	open fun coplanarPoint(target: Vector3): Vector3
	open fun applyMatrix4(matrix: Matrix4, optionalNormalMatrix: Matrix3 = definedExternally): Plane
	open fun translate(offset: Vector3): Plane
	open fun equals(plane: Plane): Boolean
	open fun isIntersectionLine(l: Any): Any
}
