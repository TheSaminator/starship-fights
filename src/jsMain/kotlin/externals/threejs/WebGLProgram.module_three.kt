@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLShader

external open class WebGLProgram(renderer: WebGLRenderer, cacheKey: String, parameters: Any?) {
	open var name: String
	open var id: Number
	open var cacheKey: String
	open var usedTimes: Number
	open var program: Any
	open var vertexShader: WebGLShader
	open var fragmentShader: WebGLShader
	open var uniforms: Any
	open var attributes: Any
	open fun getUniforms(): WebGLUniforms
	open fun getAttributes(): Any
	open fun destroy()
}
