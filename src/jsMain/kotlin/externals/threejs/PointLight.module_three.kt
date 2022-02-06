@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class PointLight : Light {
	constructor(color: Color = definedExternally, intensity: Number = definedExternally, distance: Number = definedExternally, decay: Number = definedExternally)
	constructor()
	constructor(color: Color = definedExternally)
	constructor(color: Color = definedExternally, intensity: Number = definedExternally)
	constructor(color: Color = definedExternally, intensity: Number = definedExternally, distance: Number = definedExternally)
	constructor(color: String = definedExternally, intensity: Number = definedExternally, distance: Number = definedExternally, decay: Number = definedExternally)
	constructor(color: String = definedExternally)
	constructor(color: String = definedExternally, intensity: Number = definedExternally)
	constructor(color: String = definedExternally, intensity: Number = definedExternally, distance: Number = definedExternally)
	constructor(color: Number = definedExternally, intensity: Number = definedExternally, distance: Number = definedExternally, decay: Number = definedExternally)
	constructor(color: Number = definedExternally)
	constructor(color: Number = definedExternally, intensity: Number = definedExternally)
	constructor(color: Number = definedExternally, intensity: Number = definedExternally, distance: Number = definedExternally)
	
	override var type: String
	override var intensity: Number
	open var distance: Number
	open var decay: Number
	override var shadow: LightShadow
	open var power: Number
}
