@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLRenderingContext

external open class WebGLExtensions(gl: WebGLRenderingContext) {
	open fun has(name: String): Boolean
	open fun init(capabilities: WebGLCapabilities)
	open fun get(name: String): Any
}
