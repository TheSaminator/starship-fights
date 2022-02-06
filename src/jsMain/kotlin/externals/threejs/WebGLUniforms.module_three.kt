@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLRenderingContext

external open class WebGLUniforms(gl: WebGLRenderingContext, program: WebGLProgram) {
	open fun setValue(gl: WebGLRenderingContext, name: String, value: Any, textures: WebGLTextures)
	open fun setOptional(gl: WebGLRenderingContext, obj: Any, name: String)
	
	companion object {
		fun upload(gl: WebGLRenderingContext, seq: Any, values: Array<Any>, textures: WebGLTextures)
		fun seqWithValue(seq: Any, values: Array<Any>): Array<Any>
	}
}
