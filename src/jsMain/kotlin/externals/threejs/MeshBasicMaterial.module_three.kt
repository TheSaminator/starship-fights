@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface MeshBasicMaterialParameters : MaterialParameters {
	var color: dynamic /* Color? | String? | Number? */
		get() = definedExternally
		set(value) = definedExternally
	override var opacity: Number?
		get() = definedExternally
		set(value) = definedExternally
	var map: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var lightMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var lightMapIntensity: Number?
		get() = definedExternally
		set(value) = definedExternally
	var aoMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var aoMapIntensity: Number?
		get() = definedExternally
		set(value) = definedExternally
	var specularMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var alphaMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var envMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var combine: Combine?
		get() = definedExternally
		set(value) = definedExternally
	var reflectivity: Number?
		get() = definedExternally
		set(value) = definedExternally
	var refractionRatio: Number?
		get() = definedExternally
		set(value) = definedExternally
	var wireframe: Boolean?
		get() = definedExternally
		set(value) = definedExternally
	var wireframeLinewidth: Number?
		get() = definedExternally
		set(value) = definedExternally
	var wireframeLinecap: String?
		get() = definedExternally
		set(value) = definedExternally
	var wireframeLinejoin: String?
		get() = definedExternally
		set(value) = definedExternally
}

external open class MeshBasicMaterial(parameters: MeshBasicMaterialParameters = definedExternally) : Material {
	override var type: String
	open var color: Color
	open var map: Texture?
	open var lightMap: Texture?
	open var lightMapIntensity: Number
	open var aoMap: Texture?
	open var aoMapIntensity: Number
	open var specularMap: Texture?
	open var alphaMap: Texture?
	open var envMap: Texture?
	open var combine: Combine
	open var reflectivity: Number
	open var refractionRatio: Number
	open var wireframe: Boolean
	open var wireframeLinewidth: Number
	open var wireframeLinecap: String
	open var wireframeLinejoin: String
	open fun setValues(parameters: MeshBasicMaterialParameters)
	override fun setValues(values: MaterialParameters)
}
