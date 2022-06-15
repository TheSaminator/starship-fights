@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external object UniformsUtils {
	fun clone(uniforms_src: Any): Any
	fun merge(uniforms: Array<Any>): Any
}
