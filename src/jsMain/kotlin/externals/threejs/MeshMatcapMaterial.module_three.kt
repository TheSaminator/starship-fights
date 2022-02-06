@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface MeshMatcapMaterialParameters : MaterialParameters {
	var color: dynamic /* Color? | String? | Number? */
		get() = definedExternally
		set(value) = definedExternally
	var matcap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var map: Texture?
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
	var alphaMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var flatShading: Boolean?
		get() = definedExternally
		set(value) = definedExternally
}

external open class MeshMatcapMaterial(parameters: MeshMatcapMaterialParameters = definedExternally) : Material {
	override var type: String
	open var color: Color
	open var matcap: Texture?
	open var map: Texture?
	open var bumpMap: Texture?
	open var bumpScale: Number
	open var normalMap: Texture?
	open var normalMapType: NormalMapTypes
	open var normalScale: Vector2
	open var displacementMap: Texture?
	open var displacementScale: Number
	open var displacementBias: Number
	open var alphaMap: Texture?
	open var flatShading: Boolean
	open fun setValues(parameters: MeshMatcapMaterialParameters)
	override fun setValues(values: MaterialParameters)
}
