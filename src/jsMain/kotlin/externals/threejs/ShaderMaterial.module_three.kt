@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct81 {
	var derivatives: Boolean?
		get() = definedExternally
		set(value) = definedExternally
	var fragDepth: Boolean?
		get() = definedExternally
		set(value) = definedExternally
	var drawBuffers: Boolean?
		get() = definedExternally
		set(value) = definedExternally
	var shaderTextureLOD: Boolean?
		get() = definedExternally
		set(value) = definedExternally
}

external interface ShaderMaterialParameters : MaterialParameters {
	var uniforms: AnonymousStruct8?
		get() = definedExternally
		set(value) = definedExternally
	var vertexShader: String?
		get() = definedExternally
		set(value) = definedExternally
	var fragmentShader: String?
		get() = definedExternally
		set(value) = definedExternally
	var linewidth: Number?
		get() = definedExternally
		set(value) = definedExternally
	var wireframe: Boolean?
		get() = definedExternally
		set(value) = definedExternally
	var wireframeLinewidth: Number?
		get() = definedExternally
		set(value) = definedExternally
	var lights: Boolean?
		get() = definedExternally
		set(value) = definedExternally
	var clipping: Boolean?
		get() = definedExternally
		set(value) = definedExternally
	var extensions: AnonymousStruct81?
		get() = definedExternally
		set(value) = definedExternally
	var glslVersion: GLSLVersion?
		get() = definedExternally
		set(value) = definedExternally
}

external interface AnonymousStruct82 {
	var derivatives: Boolean
	var fragDepth: Boolean
	var drawBuffers: Boolean
	var shaderTextureLOD: Boolean
}

external open class ShaderMaterial(parameters: ShaderMaterialParameters = definedExternally) : Material {
	override var type: String
	open var uniforms: AnonymousStruct8
	open var vertexShader: String
	open var fragmentShader: String
	open var linewidth: Number
	open var wireframe: Boolean
	open var wireframeLinewidth: Number
	override var fog: Boolean
	open var lights: Boolean
	open var clipping: Boolean
	open var derivatives: Any
	open var extensions: AnonymousStruct82
	open var defaultAttributeValues: Any
	open var index0AttributeName: String?
	open var uniformsNeedUpdate: Boolean
	open var glslVersion: GLSLVersion?
	open var isShaderMaterial: Boolean
	open fun setValues(parameters: ShaderMaterialParameters)
	override fun setValues(values: MaterialParameters)
	override fun toJSON(meta: Any): Any
}
