@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class LineCurve3(v1: Vector3, v2: Vector3) : Curve<Vector3> {
	override var type: String
	open var v1: Vector3
	open var v2: Vector3
}
