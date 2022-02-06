@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct8 {
	@nativeGetter
	operator fun get(uniform: String): IUniform__0?
	
	@nativeSetter
	operator fun set(uniform: String, value: IUniform__0)
}

external interface Shader {
	var uniforms: AnonymousStruct8
	var vertexShader: String
	var fragmentShader: String
}

external object ShaderLib {
	@nativeGetter
	operator fun get(name: String): Shader?
	
	@nativeSetter
	operator fun set(name: String, value: Shader)
	var basic: Shader
	var lambert: Shader
	var phong: Shader
	var standard: Shader
	var matcap: Shader
	var points: Shader
	var dashed: Shader
	var depth: Shader
	var normal: Shader
	var sprite: Shader
	var background: Shader
	var cube: Shader
	var equirect: Shader
	var distanceRGBA: Shader
	var shadow: Shader
	var physical: Shader
}
