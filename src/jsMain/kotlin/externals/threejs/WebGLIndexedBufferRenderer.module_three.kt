@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLRenderingContext

external open class WebGLIndexedBufferRenderer(gl: WebGLRenderingContext, extensions: Any, info: Any, capabilities: Any) {
	open fun setMode(value: Any)
	open fun setIndex(index: Any)
	open fun render(start: Any, count: Number)
	open fun renderInstances(start: Any, count: Number, primcount: Number)
}
