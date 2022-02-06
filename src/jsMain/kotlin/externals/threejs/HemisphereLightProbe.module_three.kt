@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class HemisphereLightProbe : LightProbe {
	constructor(skyColor: Color = definedExternally, groundColor: Color = definedExternally, intensity: Number = definedExternally)
	constructor()
	constructor(skyColor: Color = definedExternally)
	constructor(skyColor: Color = definedExternally, groundColor: Color = definedExternally)
	constructor(skyColor: Color = definedExternally, groundColor: String = definedExternally, intensity: Number = definedExternally)
	constructor(skyColor: Color = definedExternally, groundColor: String = definedExternally)
	constructor(skyColor: Color = definedExternally, groundColor: Number = definedExternally, intensity: Number = definedExternally)
	constructor(skyColor: Color = definedExternally, groundColor: Number = definedExternally)
	constructor(skyColor: String = definedExternally, groundColor: Color = definedExternally, intensity: Number = definedExternally)
	constructor(skyColor: String = definedExternally)
	constructor(skyColor: String = definedExternally, groundColor: Color = definedExternally)
	constructor(skyColor: String = definedExternally, groundColor: String = definedExternally, intensity: Number = definedExternally)
	constructor(skyColor: String = definedExternally, groundColor: String = definedExternally)
	constructor(skyColor: String = definedExternally, groundColor: Number = definedExternally, intensity: Number = definedExternally)
	constructor(skyColor: String = definedExternally, groundColor: Number = definedExternally)
	constructor(skyColor: Number = definedExternally, groundColor: Color = definedExternally, intensity: Number = definedExternally)
	constructor(skyColor: Number = definedExternally)
	constructor(skyColor: Number = definedExternally, groundColor: Color = definedExternally)
	constructor(skyColor: Number = definedExternally, groundColor: String = definedExternally, intensity: Number = definedExternally)
	constructor(skyColor: Number = definedExternally, groundColor: String = definedExternally)
	constructor(skyColor: Number = definedExternally, groundColor: Number = definedExternally, intensity: Number = definedExternally)
	constructor(skyColor: Number = definedExternally, groundColor: Number = definedExternally)
	
	open var isHemisphereLightProbe: Boolean
}
