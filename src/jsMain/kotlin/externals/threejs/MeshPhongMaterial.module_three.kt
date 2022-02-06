@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface MeshPhongMaterialParameters : MaterialParameters {
	var color: dynamic /* Color? | String? | Number? */
		get() = definedExternally
		set(value) = definedExternally
	var specular: dynamic /* Color? | String? | Number? */
		get() = definedExternally
		set(value) = definedExternally
	var shininess: Number?
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
	var emissive: dynamic /* Color? | String? | Number? */
		get() = definedExternally
		set(value) = definedExternally
	var emissiveIntensity: Number?
		get() = definedExternally
		set(value) = definedExternally
	var emissiveMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var bumpMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var bumpScale: Number?
		get() = definedExternally
		set(value) = definedExternally
	var normalMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var normalMapType: NormalMapTypes?
		get() = definedExternally
		set(value) = definedExternally
	var normalScale: Vector2?
		get() = definedExternally
		set(value) = definedExternally
	var displacementMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var displacementScale: Number?
		get() = definedExternally
		set(value) = definedExternally
	var displacementBias: Number?
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
	var flatShading: Boolean?
		get() = definedExternally
		set(value) = definedExternally
}

external open class MeshPhongMaterial(parameters: MeshPhongMaterialParameters = definedExternally) : Material {
	override var type: String
	open var color: Color
	open var specular: Color
	open var shininess: Number
	open var map: Texture?
	open var lightMap: Texture?
	open var lightMapIntensity: Number
	open var aoMap: Texture?
	open var aoMapIntensity: Number
	open var emissive: Color
	open var emissiveIntensity: Number
	open var emissiveMap: Texture?
	open var bumpMap: Texture?
	open var bumpScale: Number
	open var normalMap: Texture?
	open var normalMapType: NormalMapTypes
	open var normalScale: Vector2
	open var displacementMap: Texture?
	open var displacementScale: Number
	open var displacementBias: Number
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
	open var flatShading: Boolean
	open var metal: Boolean
	open fun setValues(parameters: MeshPhongMaterialParameters)
	override fun setValues(values: MaterialParameters)
}
