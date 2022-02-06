@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Light : Object3D {
	constructor(hex: Number = definedExternally, intensity: Number = definedExternally)
	constructor()
	constructor(hex: Number = definedExternally)
	constructor(hex: String = definedExternally, intensity: Number = definedExternally)
	constructor(hex: String = definedExternally)
	
	override var type: String
	open var color: Color
	open var intensity: Number
	open var isLight: Boolean
	open var shadow: LightShadow
	open var shadowCameraFov: Any
	open var shadowCameraLeft: Any
	open var shadowCameraRight: Any
	open var shadowCameraTop: Any
	open var shadowCameraBottom: Any
	open var shadowCameraNear: Any
	open var shadowCameraFar: Any
	open var shadowBias: Any
	open var shadowMapWidth: Any
	open var shadowMapHeight: Any
	open fun dispose()
}
