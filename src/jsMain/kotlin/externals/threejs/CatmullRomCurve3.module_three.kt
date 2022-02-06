@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class CatmullRomCurve3(points: Array<Vector3> = definedExternally, closed: Boolean = definedExternally, curveType: String = definedExternally, tension: Number = definedExternally) : Curve<Vector3> {
	override var type: String
	open var points: Array<Vector3>
}
