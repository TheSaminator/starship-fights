@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface StringDict<Type> {
	@nativeGetter
	operator fun get(key: String): Type?
	
	@nativeSetter
	operator fun set(key: String, value: Type)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Map<String, T>.toTSStringDict(): StringDict<T> = js("{}").unsafeCast<StringDict<T>>().also { sd ->
	forEach { (key, value) ->
		sd[key] = value
	}
}

inline fun <T> StringDict<T>.keys(): Array<String> {
	return (js("Object.keys"))(this).unsafeCast<Array<String>>()
}

external interface ArrayLike<Type> {
	@nativeGetter
	operator fun get(key: Number): Type?
	
	@nativeSetter
	operator fun set(key: Number, value: Type)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Array<T>.toTSArrayLike(): ArrayLike<T> = unsafeCast<ArrayLike<T>>()
