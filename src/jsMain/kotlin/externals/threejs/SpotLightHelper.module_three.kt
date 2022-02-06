@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class SpotLightHelper : Object3D {
	constructor(light: Light, color: Color = definedExternally)
	constructor(light: Light)
	constructor(light: Light, color: String = definedExternally)
	constructor(light: Light, color: Number = definedExternally)
	
	open var light: Light
	override var matrix: Matrix4
	override var matrixAutoUpdate: Boolean
	open var color: dynamic /* Color? | String? | Number? */
	open var cone: LineSegments
	open fun dispose()
	open fun update()
}
