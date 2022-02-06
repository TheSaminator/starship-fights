@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class QuadraticBezierCurve(v0: Vector2, v1: Vector2, v2: Vector2) : Curve<Vector2> {
	override var type: String
	open var v0: Vector2
	open var v1: Vector2
	open var v2: Vector2
}
