@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class SpotLightShadow(camera: Camera) : LightShadow {
	override var camera: Camera
	open var isSpotLightShadow: Boolean
	open var focus: Number
}
