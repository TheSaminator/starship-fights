@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class RectAreaLight : Light {
	constructor(color: Color = definedExternally, intensity: Number = definedExternally, width: Number = definedExternally, height: Number = definedExternally)
	constructor()
	constructor(color: Color = definedExternally)
	constructor(color: Color = definedExternally, intensity: Number = definedExternally)
	constructor(color: Color = definedExternally, intensity: Number = definedExternally, width: Number = definedExternally)
	constructor(color: String = definedExternally, intensity: Number = definedExternally, width: Number = definedExternally, height: Number = definedExternally)
	constructor(color: String = definedExternally)
	constructor(color: String = definedExternally, intensity: Number = definedExternally)
	constructor(color: String = definedExternally, intensity: Number = definedExternally, width: Number = definedExternally)
	constructor(color: Number = definedExternally, intensity: Number = definedExternally, width: Number = definedExternally, height: Number = definedExternally)
	constructor(color: Number = definedExternally)
	constructor(color: Number = definedExternally, intensity: Number = definedExternally)
	constructor(color: Number = definedExternally, intensity: Number = definedExternally, width: Number = definedExternally)
	
	override var type: String
	open var width: Number
	open var height: Number
	override var intensity: Number
	open var isRectAreaLight: Boolean
}
