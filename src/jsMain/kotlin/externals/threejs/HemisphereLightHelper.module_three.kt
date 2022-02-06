@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class HemisphereLightHelper : Object3D {
	constructor(light: HemisphereLight, size: Number, color: Color = definedExternally)
	constructor(light: HemisphereLight, size: Number)
	constructor(light: HemisphereLight, size: Number, color: String = definedExternally)
	constructor(light: HemisphereLight, size: Number, color: Number = definedExternally)
	
	open var light: HemisphereLight
	override var matrix: Matrix4
	override var matrixAutoUpdate: Boolean
	open var material: MeshBasicMaterial
	open var color: dynamic /* Color? | String? | Number? */
	open fun dispose()
	open fun update()
}
