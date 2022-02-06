@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLRenderingContext

external open class WebGLGeometries(gl: WebGLRenderingContext, attributes: WebGLAttributes, info: WebGLInfo) {
	open fun get(obj: Object3D, geometry: BufferGeometry): BufferGeometry
	open fun update(geometry: BufferGeometry)
	open fun getWireframeAttribute(geometry: BufferGeometry): BufferAttribute
}
