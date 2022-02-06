@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class ArcCurve(aX: Number, aY: Number, aRadius: Number, aStartAngle: Number, aEndAngle: Number, aClockwise: Boolean) : EllipseCurve {
	override var type: String
}
