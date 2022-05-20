@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "DEPRECATION")

package externals.threejs

external interface StringDict<Type> {
	@nativeGetter
	operator fun get(key: String): Type?
	
	@nativeSetter
	operator fun set(key: String, value: Type)
}

external interface ArrayLike<Type> {
	@nativeGetter
	operator fun get(key: Number): Type?
	
	@nativeSetter
	operator fun set(key: Number, value: Type)
}
