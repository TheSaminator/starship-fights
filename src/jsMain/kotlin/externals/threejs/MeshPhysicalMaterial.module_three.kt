@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface MeshPhysicalMaterialParameters : MeshStandardMaterialParameters {
	var clearcoat: Number?
		get() = definedExternally
		set(value) = definedExternally
	var clearcoatMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var clearcoatRoughness: Number?
		get() = definedExternally
		set(value) = definedExternally
	var clearcoatRoughnessMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var clearcoatNormalScale: Vector2?
		get() = definedExternally
		set(value) = definedExternally
	var clearcoatNormalMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var reflectivity: Number?
		get() = definedExternally
		set(value) = definedExternally
	var ior: Number?
		get() = definedExternally
		set(value) = definedExternally
	var sheen: Color?
		get() = definedExternally
		set(value) = definedExternally
	var transmission: Number?
		get() = definedExternally
		set(value) = definedExternally
	var transmissionMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var attenuationDistance: Number?
		get() = definedExternally
		set(value) = definedExternally
	var attenuationTint: Color?
		get() = definedExternally
		set(value) = definedExternally
	var specularIntensity: Number?
		get() = definedExternally
		set(value) = definedExternally
	var specularTint: Color?
		get() = definedExternally
		set(value) = definedExternally
	var specularIntensityMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
	var specularTintMap: Texture?
		get() = definedExternally
		set(value) = definedExternally
}

external open class MeshPhysicalMaterial(parameters: MeshPhysicalMaterialParameters = definedExternally) : MeshStandardMaterial {
	override var type: String
	override var defines: dynamic
	open var clearcoat: Number
	open var clearcoatMap: Texture?
	open var clearcoatRoughness: Number
	open var clearcoatRoughnessMap: Texture?
	open var clearcoatNormalScale: Vector2
	open var clearcoatNormalMap: Texture?
	open var reflectivity: Number
	open var ior: Number
	open var sheen: Color?
	open var transmission: Number
	open var transmissionMap: Texture?
	open var thickness: Number
	open var thicknessMap: Texture?
	open var attenuationDistance: Number
	open var attenuationColor: Color
	open var specularIntensity: Number
	open var specularTint: Color
	open var specularIntensityMap: Texture?
	open var specularTintMap: Texture?
}
