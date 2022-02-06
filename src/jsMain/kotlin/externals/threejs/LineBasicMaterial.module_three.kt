@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface LineBasicMaterialParameters : MaterialParameters {
	var color: dynamic /* Color? | String? | Number? */
		get() = definedExternally
		set(value) = definedExternally
	var linewidth: Number?
		get() = definedExternally
		set(value) = definedExternally
	var linecap: String?
		get() = definedExternally
		set(value) = definedExternally
	var linejoin: String?
		get() = definedExternally
		set(value) = definedExternally
}

external open class LineBasicMaterial(parameters: LineBasicMaterialParameters = definedExternally) : Material {
	override var type: String
	open var color: Color
	open var linewidth: Number
	open var linecap: String
	open var linejoin: String
	open fun setValues(parameters: LineBasicMaterialParameters)
	override fun setValues(values: MaterialParameters)
}
