@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct48 {
	var isInterleavedBufferAttribute: Boolean
	var itemSize: Number
	var data: String
	var offset: Number
	var normalized: Boolean
}

external open class InterleavedBufferAttribute(interleavedBuffer: InterleavedBuffer, itemSize: Number, offset: Number, normalized: Boolean = definedExternally) {
	open var name: String
	open var data: InterleavedBuffer
	open var itemSize: Number
	open var offset: Number
	open var normalized: Boolean
	open var isInterleavedBufferAttribute: Boolean
	open fun applyMatrix4(m: Matrix4): InterleavedBufferAttribute /* this */
	open fun clone(data: Any? = definedExternally): BufferAttribute
	open fun getX(index: Number): Number
	open fun setX(index: Number, x: Number): InterleavedBufferAttribute /* this */
	open fun getY(index: Number): Number
	open fun setY(index: Number, y: Number): InterleavedBufferAttribute /* this */
	open fun getZ(index: Number): Number
	open fun setZ(index: Number, z: Number): InterleavedBufferAttribute /* this */
	open fun getW(index: Number): Number
	open fun setW(index: Number, z: Number): InterleavedBufferAttribute /* this */
	open fun setXY(index: Number, x: Number, y: Number): InterleavedBufferAttribute /* this */
	open fun setXYZ(index: Number, x: Number, y: Number, z: Number): InterleavedBufferAttribute /* this */
	open fun setXYZW(index: Number, x: Number, y: Number, z: Number, w: Number): InterleavedBufferAttribute /* this */
	open fun toJSON(data: Any? = definedExternally): AnonymousStruct48
	open fun applyNormalMatrix(matrix: Matrix): InterleavedBufferAttribute /* this */
	open fun transformDirection(matrix: Matrix): InterleavedBufferAttribute /* this */
}
