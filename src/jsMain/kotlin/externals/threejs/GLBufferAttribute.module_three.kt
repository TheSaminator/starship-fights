@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLBuffer

external open class GLBufferAttribute(buffer: WebGLBuffer, type: Number, itemSize: Number, elementSize: Number /* 1 | 2 | 4 */, count: Number) {
	open var buffer: WebGLBuffer
	open var type: Number
	open var itemSize: Number
	open var elementSize: Number /* 1 | 2 | 4 */
	open var count: Number
	open var version: Number
	open var isGLBufferAttribute: Boolean
	open fun setBuffer(buffer: WebGLBuffer): GLBufferAttribute /* this */
	open fun setType(type: Number, elementSize: Number /* 1 | 2 | 4 */): GLBufferAttribute /* this */
	open fun setItemSize(itemSize: Number): GLBufferAttribute /* this */
	open fun setCount(count: Number): GLBufferAttribute /* this */
}
