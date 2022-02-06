@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class InstancedBufferGeometry : BufferGeometry {
	override var type: String
	open var isInstancedBufferGeometry: Boolean
	open var instanceCount: Number
	override fun addGroup(start: Number, count: Number, instances: Number)
}
