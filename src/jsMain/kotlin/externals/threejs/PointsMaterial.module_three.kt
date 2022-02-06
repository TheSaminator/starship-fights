@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface PointsMaterialParameters : MaterialParameters {
	var color: dynamic /* Color? | String? | Number? */
		get() = definedExternally
		set(value) = definedExternally
	var map: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var alphaMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var size: Number?
		get() = definedExternally
		set(value) = definedExternally
	var sizeAttenuation: Boolean?
		get() = definedExternally
		set(value) = definedExternally
}

external open class PointsMaterial(parameters: PointsMaterialParameters = definedExternally) : Material {
	override var type: String
	open var color: Color
	open var map: Texture?
	open var alphaMap: Texture?
	open var size: Number
	open var sizeAttenuation: Boolean
	open fun setValues(parameters: PointsMaterialParameters)
	override fun setValues(values: MaterialParameters)
}
