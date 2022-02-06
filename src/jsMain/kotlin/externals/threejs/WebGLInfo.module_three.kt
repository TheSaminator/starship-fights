@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLRenderingContext

external interface AnonymousStruct41 {
	var geometries: Number
	var textures: Number
}

external interface AnonymousStruct42 {
	var calls: Number
	var frame: Number
	var lines: Number
	var points: Number
	var triangles: Number
}

external open class WebGLInfo(gl: WebGLRenderingContext) {
	open var autoReset: Boolean
	open var memory: AnonymousStruct41
	open var programs: Array<WebGLProgram>?
	open var render: AnonymousStruct42
	open fun update(count: Number, mode: Number, instanceCount: Number)
	open fun reset()
}
