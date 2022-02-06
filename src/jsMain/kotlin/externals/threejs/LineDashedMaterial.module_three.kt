@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface LineDashedMaterialParameters : LineBasicMaterialParameters {
	var scale: Number?
		get() = definedExternally
		set(value) = definedExternally
	var dashSize: Number?
		get() = definedExternally
		set(value) = definedExternally
	var gapSize: Number?
		get() = definedExternally
		set(value) = definedExternally
}

external open class LineDashedMaterial(parameters: LineDashedMaterialParameters = definedExternally) : LineBasicMaterial {
	override var type: String
	open var scale: Number
	open var dashSize: Number
	open var gapSize: Number
	open var isLineDashedMaterial: Boolean
	open fun setValues(parameters: LineDashedMaterialParameters)
	override fun setValues(parameters: LineBasicMaterialParameters)
	override fun setValues(values: MaterialParameters)
}
