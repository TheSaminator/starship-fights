@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class DirectionalLightHelper : Object3D {
	constructor(light: DirectionalLight, size: Number = definedExternally, color: Color = definedExternally)
	constructor(light: DirectionalLight)
	constructor(light: DirectionalLight, size: Number = definedExternally)
	constructor(light: DirectionalLight, size: Number = definedExternally, color: String = definedExternally)
	constructor(light: DirectionalLight, size: Number = definedExternally, color: Number = definedExternally)
	
	open var light: DirectionalLight
	open var lightPlane: Line
	open var targetLine: Line
	open var color: dynamic /* Color? | String? | Number? */
	override var matrix: Matrix4
	override var matrixAutoUpdate: Boolean
	open fun dispose()
	open fun update()
}
