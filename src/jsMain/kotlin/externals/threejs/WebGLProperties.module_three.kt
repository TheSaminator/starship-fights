@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class WebGLProperties {
	open fun get(obj: Any): Any
	open fun remove(obj: Any)
	open fun update(obj: Any, key: Any, value: Any): Any
	open fun dispose()
}
