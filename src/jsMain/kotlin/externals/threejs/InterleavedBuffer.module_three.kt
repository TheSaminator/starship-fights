@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct49 {
	var uuid: String
	var buffer: String
	var type: String
	var stride: Number
}

external open class InterleavedBuffer(array: ArrayLike<Number>, stride: Number) {
	open var array: ArrayLike<Number>
	open var stride: Number
	open var usage: Usage
	open var updateRange: AnonymousStruct0
	open var version: Number
	open var length: Number
	open var count: Number
	open var needsUpdate: Boolean
	open var uuid: String
	open fun setUsage(usage: Usage): InterleavedBuffer
	open fun clone(data: Any?): InterleavedBuffer
	open fun copy(source: InterleavedBuffer): InterleavedBuffer /* this */
	open fun copyAt(index1: Number, attribute: InterleavedBufferAttribute, index2: Number): InterleavedBuffer
	open fun set(value: ArrayLike<Number>, index: Number): InterleavedBuffer
	open fun toJSON(data: Any?): AnonymousStruct49
}
