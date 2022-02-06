@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.events.EventTarget

external interface AnonymousStruct44 {
	@nativeGetter
	operator fun get(name: String): dynamic /* BufferAttribute? | InterleavedBufferAttribute? */
	
	@nativeSetter
	operator fun set(name: String, value: BufferAttribute)
	
	@nativeSetter
	operator fun set(name: String, value: InterleavedBufferAttribute)
}

external interface AnonymousStruct45 {
	@nativeGetter
	operator fun get(name: String): Array<dynamic /* BufferAttribute | InterleavedBufferAttribute */>?
	
	@nativeSetter
	operator fun set(name: String, value: Array<dynamic /* BufferAttribute | InterleavedBufferAttribute */>)
}

external interface AnonymousStruct46 {
	var start: Number
	var count: Number
	var materialIndex: Number?
		get() = definedExternally
		set(value) = definedExternally
}

external interface AnonymousStruct47 {
	var start: Number
	var count: Number
}

external open class BufferGeometry : EventTarget {
	open var id: Number
	open var uuid: String
	open var name: String
	open var type: String
	open var index: BufferAttribute?
	open var attributes: AnonymousStruct44
	open var morphAttributes: AnonymousStruct45
	open var morphTargetsRelative: Boolean
	open var groups: Array<AnonymousStruct46>
	open var boundingBox: Box3?
	open var boundingSphere: Sphere?
	open var drawRange: AnonymousStruct47
	open var userData: dynamic
	open var isBufferGeometry: Boolean
	open fun getIndex(): BufferAttribute?
	open fun setIndex(index: BufferAttribute?): BufferGeometry
	open fun setIndex(index: Array<Number>?): BufferGeometry
	open fun setAttribute(name: String /* "position" | "normal" | "uv" | "color" | "skinIndex" | "skinWeight" | "instanceMatrix" | "morphTarget0" | "morphTarget1" | "morphTarget2" | "morphTarget3" | "morphTarget4" | "morphTarget5" | "morphTarget6" | "morphTarget7" | "morphNormal0" | "morphNormal1" | "morphNormal2" | "morphNormal3" | String & Any */, attribute: Any /* BufferAttribute | InterleavedBufferAttribute */): BufferGeometry
	open fun getAttribute(name: String /* "position" | "normal" | "uv" | "color" | "skinIndex" | "skinWeight" | "instanceMatrix" | "morphTarget0" | "morphTarget1" | "morphTarget2" | "morphTarget3" | "morphTarget4" | "morphTarget5" | "morphTarget6" | "morphTarget7" | "morphNormal0" | "morphNormal1" | "morphNormal2" | "morphNormal3" | String & Any */): dynamic /* BufferAttribute | InterleavedBufferAttribute */
	open fun deleteAttribute(name: String /* "position" | "normal" | "uv" | "color" | "skinIndex" | "skinWeight" | "instanceMatrix" | "morphTarget0" | "morphTarget1" | "morphTarget2" | "morphTarget3" | "morphTarget4" | "morphTarget5" | "morphTarget6" | "morphTarget7" | "morphNormal0" | "morphNormal1" | "morphNormal2" | "morphNormal3" | String & Any */): BufferGeometry
	open fun hasAttribute(name: String /* "position" | "normal" | "uv" | "color" | "skinIndex" | "skinWeight" | "instanceMatrix" | "morphTarget0" | "morphTarget1" | "morphTarget2" | "morphTarget3" | "morphTarget4" | "morphTarget5" | "morphTarget6" | "morphTarget7" | "morphNormal0" | "morphNormal1" | "morphNormal2" | "morphNormal3" | String & Any */): Boolean
	open fun addGroup(start: Number, count: Number, materialIndex: Number = definedExternally)
	open fun clearGroups()
	open fun setDrawRange(start: Number, count: Number)
	open fun applyMatrix4(matrix: Matrix4): BufferGeometry
	open fun applyQuaternion(q: Quaternion): BufferGeometry
	open fun rotateX(angle: Number): BufferGeometry
	open fun rotateY(angle: Number): BufferGeometry
	open fun rotateZ(angle: Number): BufferGeometry
	open fun translate(x: Number, y: Number, z: Number): BufferGeometry
	open fun scale(x: Number, y: Number, z: Number): BufferGeometry
	open fun lookAt(v: Vector3)
	open fun center(): BufferGeometry
	open fun setFromPoints(points: Array<Vector3>): BufferGeometry
	open fun setFromPoints(points: Array<Vector2>): BufferGeometry
	open fun computeBoundingBox()
	open fun computeBoundingSphere()
	open fun computeTangents()
	open fun computeVertexNormals()
	open fun merge(geometry: BufferGeometry, offset: Number = definedExternally): BufferGeometry
	open fun normalizeNormals()
	open fun toNonIndexed(): BufferGeometry
	open fun toJSON(): Any
	open fun clone(): BufferGeometry
	open fun copy(source: BufferGeometry): BufferGeometry /* this */
	open fun dispose()
	open var drawcalls: Any
	open var offsets: Any
	open fun addIndex(index: Any)
	open fun addDrawCall(start: Any, count: Any, indexOffset: Any = definedExternally)
	open fun clearDrawCalls()
	open fun addAttribute(name: String, attribute: BufferAttribute): BufferGeometry
	open fun addAttribute(name: String, attribute: InterleavedBufferAttribute): BufferGeometry
	open fun addAttribute(name: Any, array: Any, itemSize: Any): Any
	open fun removeAttribute(name: String): BufferGeometry
	
	companion object {
		var MaxIndex: Number
	}
}
