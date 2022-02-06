@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class WebGLCubeUVMaps(renderer: WebGLRenderer) {
	open fun <T> get(texture: T): Any
	open fun dispose()
}
