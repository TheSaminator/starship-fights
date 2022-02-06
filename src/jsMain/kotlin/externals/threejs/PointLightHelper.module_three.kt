@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class PointLightHelper : Object3D {
	constructor(light: PointLight, sphereSize: Number = definedExternally, color: Color = definedExternally)
	constructor(light: PointLight)
	constructor(light: PointLight, sphereSize: Number = definedExternally)
	constructor(light: PointLight, sphereSize: Number = definedExternally, color: String = definedExternally)
	constructor(light: PointLight, sphereSize: Number = definedExternally, color: Number = definedExternally)
	
	override var type: String
	open var light: PointLight
	open var color: dynamic /* Color? | String? | Number? */
	override var matrix: Matrix4
	override var matrixAutoUpdate: Boolean
	open fun dispose()
	open fun update()
}
