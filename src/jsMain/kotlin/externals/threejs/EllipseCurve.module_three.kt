@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class EllipseCurve(aX: Number, aY: Number, xRadius: Number, yRadius: Number, aStartAngle: Number, aEndAngle: Number, aClockwise: Boolean, aRotation: Number) : Curve<Vector2> {
	override var type: String
	open var aX: Number
	open var aY: Number
	open var xRadius: Number
	open var yRadius: Number
	open var aStartAngle: Number
	open var aEndAngle: Number
	open var aClockwise: Boolean
	open var aRotation: Number
}
