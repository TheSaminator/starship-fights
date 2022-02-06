@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class SplineCurve(points: Array<Vector2> = definedExternally) : Curve<Vector2> {
	override var type: String
	open var points: Array<Vector2>
}
