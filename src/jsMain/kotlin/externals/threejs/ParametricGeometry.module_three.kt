@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct68 {
	var func: (u: Number, v: Number, dest: Vector3) -> Unit
	var slices: Number
	var stacks: Number
}

external open class ParametricGeometry(func: (u: Number, v: Number, dest: Vector3) -> Unit, slices: Number, stacks: Number) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct68
}
