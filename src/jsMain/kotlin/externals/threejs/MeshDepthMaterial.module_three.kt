@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface MeshDepthMaterialParameters : MaterialParameters {
	var map: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var alphaMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var depthPacking: DepthPackingStrategies?
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
	var wireframe: Boolean?
		get() = definedExternally
		set(value) = definedExternally
	var wireframeLinewidth: Number?
		get() = definedExternally
		set(value) = definedExternally
}

external open class MeshDepthMaterial(parameters: MeshDepthMaterialParameters = definedExternally) : Material {
	override var type: String
	open var map: Texture?
	open var alphaMap: Texture?
	open var depthPacking: DepthPackingStrategies
	open var displacementMap: Texture?
	open var displacementScale: Number
	open var displacementBias: Number
	open var wireframe: Boolean
	open var wireframeLinewidth: Number
	override var fog: Boolean
	open fun setValues(parameters: MeshDepthMaterialParameters)
	override fun setValues(values: MaterialParameters)
}
