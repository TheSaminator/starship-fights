@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class DirectionalLight : Light {
	constructor(color: Color = definedExternally, intensity: Number = definedExternally)
	constructor()
	constructor(color: Color = definedExternally)
	constructor(color: String = definedExternally, intensity: Number = definedExternally)
	constructor(color: String = definedExternally)
	constructor(color: Number = definedExternally, intensity: Number = definedExternally)
	constructor(color: Number = definedExternally)
	
	override var type: String
	override var position: Vector3
	open var target: Object3D
	override var intensity: Number
	override var shadow: LightShadow
	open var isDirectionalLight: Boolean
}
