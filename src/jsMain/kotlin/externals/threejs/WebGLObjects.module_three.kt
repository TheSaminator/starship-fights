@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLRenderingContext

external open class WebGLObjects(gl: WebGLRenderingContext, geometries: Any, attributes: Any, info: Any) {
	open fun update(obj: Any): Any
	open fun dispose()
}
