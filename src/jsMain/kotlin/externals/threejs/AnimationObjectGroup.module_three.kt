@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct56 {
	var total: Number
	var inUse: Number
}

external interface AnonymousStruct57 {
	var bindingsPerObject: Number
	var objects: AnonymousStruct56
}

external open class AnimationObjectGroup(vararg args: Any) {
	open var uuid: String
	open var stats: AnonymousStruct57
	open var isAnimationObjectGroup: Boolean
	open fun add(vararg args: Any)
	open fun remove(vararg args: Any)
	open fun uncache(vararg args: Any)
}
