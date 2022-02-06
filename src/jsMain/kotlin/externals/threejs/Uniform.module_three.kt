@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Uniform {
	constructor(value: Any)
	constructor(type: String, value: Any)
	
	open var type: String
	open var value: Any
	open var dynamic: Boolean
	open var onUpdateCallback: () -> Unit
	open fun onUpdate(callback: () -> Unit): Uniform
}
