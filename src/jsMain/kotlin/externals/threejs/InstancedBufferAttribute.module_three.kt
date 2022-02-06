@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class InstancedBufferAttribute(array: ArrayLike<Number>, itemSize: Number, normalized: Boolean = definedExternally, meshPerAttribute: Number = definedExternally) : BufferAttribute {
	open var meshPerAttribute: Number
}
