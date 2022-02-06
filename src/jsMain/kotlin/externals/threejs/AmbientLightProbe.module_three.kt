@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class AmbientLightProbe : LightProbe {
	constructor(color: Color = definedExternally, intensity: Number = definedExternally)
	constructor()
	constructor(color: Color = definedExternally)
	constructor(color: String = definedExternally, intensity: Number = definedExternally)
	constructor(color: String = definedExternally)
	constructor(color: Number = definedExternally, intensity: Number = definedExternally)
	constructor(color: Number = definedExternally)
	
	open var isAmbientLightProbe: Boolean
}
