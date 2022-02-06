@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class QuaternionKeyframeTrack(name: String, times: Array<Any>, values: Array<Any>, interpolation: InterpolationModes = definedExternally) : KeyframeTrack {
	override var ValueTypeName: String
}
