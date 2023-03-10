@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.ArrayBufferView

external interface AnonymousStruct0 {
	var offset: Number
	var count: Number
}

external interface AnonymousStruct1 {
	var r: Number
	var g: Number
	var b: Number
}

external interface AnonymousStruct2 {
	var x: Number
	var y: Number
}

external interface AnonymousStruct3 {
	var x: Number
	var y: Number
	var z: Number
}

external interface AnonymousStruct4 {
	var x: Number
	var y: Number
	var z: Number
	var w: Number
}

external interface AnonymousStruct5 {
	var itemSize: Number
	var type: String
	var array: Array<Number>
	var normalized: Boolean
}

external open class BufferAttribute(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally) {
	open var name: String
	open var array: ArrayLike<Number>
	open var itemSize: Number
	open var usage: Usage
	open var updateRange: AnonymousStruct0
	open var version: Number
	open var normalized: Boolean
	open var count: Number
	open var isBufferAttribute: Boolean
	open var onUploadCallback: () -> Unit
	open fun onUpload(callback: () -> Unit): BufferAttribute /* this */
	open fun setUsage(usage: Usage): BufferAttribute /* this */
	open fun clone(): BufferAttribute /* this */
	open fun copy(source: BufferAttribute): BufferAttribute /* this */
	open fun copyAt(index1: Number, attribute: BufferAttribute, index2: Number): BufferAttribute /* this */
	open fun copyArray(array: ArrayLike<Number>): BufferAttribute /* this */
	open fun copyColorsArray(colors: Array<AnonymousStruct1>): BufferAttribute /* this */
	open fun copyVector2sArray(vectors: Array<AnonymousStruct2>): BufferAttribute /* this */
	open fun copyVector3sArray(vectors: Array<AnonymousStruct3>): BufferAttribute /* this */
	open fun copyVector4sArray(vectors: Array<AnonymousStruct4>): BufferAttribute /* this */
	open fun applyMatrix3(m: Matrix3): BufferAttribute /* this */
	open fun applyMatrix4(m: Matrix4): BufferAttribute /* this */
	open fun applyNormalMatrix(m: Matrix3): BufferAttribute /* this */
	open fun transformDirection(m: Matrix4): BufferAttribute /* this */
	open fun set(value: ArrayLike<Number>, offset: Number = definedExternally): BufferAttribute /* this */
	open fun set(value: ArrayLike<Number>): BufferAttribute /* this */
	open fun set(value: ArrayBufferView, offset: Number = definedExternally): BufferAttribute /* this */
	open fun set(value: ArrayBufferView): BufferAttribute /* this */
	open fun getX(index: Number): Number
	open fun setX(index: Number, x: Number): BufferAttribute /* this */
	open fun getY(index: Number): Number
	open fun setY(index: Number, y: Number): BufferAttribute /* this */
	open fun getZ(index: Number): Number
	open fun setZ(index: Number, z: Number): BufferAttribute /* this */
	open fun getW(index: Number): Number
	open fun setW(index: Number, z: Number): BufferAttribute /* this */
	open fun setXY(index: Number, x: Number, y: Number): BufferAttribute /* this */
	open fun setXYZ(index: Number, x: Number, y: Number, z: Number): BufferAttribute /* this */
	open fun setXYZW(index: Number, x: Number, y: Number, z: Number, w: Number): BufferAttribute /* this */
	open fun toJSON(): AnonymousStruct5
}

external open class Int8Attribute(array: Any, itemSize: Number) : BufferAttribute

external open class Uint8Attribute(array: Any, itemSize: Number) : BufferAttribute

external open class Uint8ClampedAttribute(array: Any, itemSize: Number) : BufferAttribute

external open class Int16Attribute(array: Any, itemSize: Number) : BufferAttribute

external open class Uint16Attribute(array: Any, itemSize: Number) : BufferAttribute

external open class Int32Attribute(array: Any, itemSize: Number) : BufferAttribute

external open class Uint32Attribute(array: Any, itemSize: Number) : BufferAttribute

external open class Float32Attribute(array: Any, itemSize: Number) : BufferAttribute

external open class Float64Attribute(array: Any, itemSize: Number) : BufferAttribute

external open class Int8BufferAttribute : BufferAttribute {
	constructor(array: Iterable<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Iterable<Number>, itemSize: Number)
	constructor(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayLike<Number>, itemSize: Number)
	constructor(array: ArrayBuffer, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayBuffer, itemSize: Number)
	constructor(array: Number, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Number, itemSize: Number)
}

external open class Uint8BufferAttribute : BufferAttribute {
	constructor(array: Iterable<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Iterable<Number>, itemSize: Number)
	constructor(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayLike<Number>, itemSize: Number)
	constructor(array: ArrayBuffer, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayBuffer, itemSize: Number)
	constructor(array: Number, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Number, itemSize: Number)
}

external open class Uint8ClampedBufferAttribute : BufferAttribute {
	constructor(array: Iterable<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Iterable<Number>, itemSize: Number)
	constructor(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayLike<Number>, itemSize: Number)
	constructor(array: ArrayBuffer, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayBuffer, itemSize: Number)
	constructor(array: Number, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Number, itemSize: Number)
}

external open class Int16BufferAttribute : BufferAttribute {
	constructor(array: Iterable<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Iterable<Number>, itemSize: Number)
	constructor(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayLike<Number>, itemSize: Number)
	constructor(array: ArrayBuffer, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayBuffer, itemSize: Number)
	constructor(array: Number, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Number, itemSize: Number)
}

external open class Uint16BufferAttribute : BufferAttribute {
	constructor(array: Iterable<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Iterable<Number>, itemSize: Number)
	constructor(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayLike<Number>, itemSize: Number)
	constructor(array: ArrayBuffer, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayBuffer, itemSize: Number)
	constructor(array: Number, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Number, itemSize: Number)
}

external open class Int32BufferAttribute : BufferAttribute {
	constructor(array: Iterable<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Iterable<Number>, itemSize: Number)
	constructor(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayLike<Number>, itemSize: Number)
	constructor(array: ArrayBuffer, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayBuffer, itemSize: Number)
	constructor(array: Number, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Number, itemSize: Number)
}

external open class Uint32BufferAttribute : BufferAttribute {
	constructor(array: Iterable<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Iterable<Number>, itemSize: Number)
	constructor(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayLike<Number>, itemSize: Number)
	constructor(array: ArrayBuffer, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayBuffer, itemSize: Number)
	constructor(array: Number, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Number, itemSize: Number)
}

external open class Float16BufferAttribute : BufferAttribute {
	constructor(array: Iterable<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Iterable<Number>, itemSize: Number)
	constructor(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayLike<Number>, itemSize: Number)
	constructor(array: ArrayBuffer, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayBuffer, itemSize: Number)
	constructor(array: Number, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Number, itemSize: Number)
}

external open class Float32BufferAttribute : BufferAttribute {
	constructor(array: Iterable<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Iterable<Number>, itemSize: Number)
	constructor(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayLike<Number>, itemSize: Number)
	constructor(array: ArrayBuffer, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayBuffer, itemSize: Number)
	constructor(array: Number, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Number, itemSize: Number)
}

external open class Float64BufferAttribute : BufferAttribute {
	constructor(array: Iterable<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Iterable<Number>, itemSize: Number)
	constructor(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayLike<Number>, itemSize: Number)
	constructor(array: ArrayBuffer, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: ArrayBuffer, itemSize: Number)
	constructor(array: Number, itemSize: Number, normalized: Boolean = definedExternally)
	constructor(array: Number, itemSize: Number)
}
