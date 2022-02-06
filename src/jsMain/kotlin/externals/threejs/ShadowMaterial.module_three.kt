@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface ShadowMaterialParameters : MaterialParameters {
	var color: dynamic /* Color? | String? | Number? */
		get() = definedExternally
		set(value) = definedExternally
}

external open class ShadowMaterial(parameters: ShadowMaterialParameters = definedExternally) : Material {
	override var type: String
	open var color: Color
	override var transparent: Boolean
}
