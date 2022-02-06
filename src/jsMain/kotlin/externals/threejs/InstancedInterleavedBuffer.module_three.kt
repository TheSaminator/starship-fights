@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class InstancedInterleavedBuffer(array: ArrayLike<Number>, stride: Number, meshPerAttribute: Number = definedExternally) : InterleavedBuffer {
	open var meshPerAttribute: Number
}
