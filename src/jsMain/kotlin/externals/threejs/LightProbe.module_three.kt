@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class LightProbe(sh: SphericalHarmonics3 = definedExternally, intensity: Number = definedExternally) : Light {
	override var type: String
	open var isLightProbe: Boolean
	open var sh: SphericalHarmonics3
	open fun fromJSON(json: Any?): LightProbe
}
