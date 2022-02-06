@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class PlaneHelper(plane: Plane, size: Number = definedExternally, hex: Number = definedExternally) : LineSegments {
	override var type: String
	open var plane: Plane
	open var size: Number
	override fun updateMatrixWorld(force: Boolean)
}
