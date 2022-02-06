@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLBuffer
import org.khronos.webgl.WebGLRenderingContext

external interface AnonymousStruct85 {
	var buffer: WebGLBuffer
	var type: Number
	var bytesPerElement: Number
	var version: Number
}

external open class WebGLAttributes(gl: WebGLRenderingContext, capabilities: WebGLCapabilities) {
	open fun get(attribute: BufferAttribute): AnonymousStruct85
	open fun get(attribute: InterleavedBufferAttribute): AnonymousStruct85
	open fun remove(attribute: BufferAttribute)
	open fun remove(attribute: InterleavedBufferAttribute)
	open fun update(attribute: BufferAttribute, bufferType: Number)
	open fun update(attribute: InterleavedBufferAttribute, bufferType: Number)
}
