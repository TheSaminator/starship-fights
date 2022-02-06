@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class CubeCamera(near: Number, far: Number, renderTarget: WebGLCubeRenderTarget) : Object3D {
	override var type: String /* "CubeCamera" */
	open var renderTarget: WebGLCubeRenderTarget
	open fun update(renderer: WebGLRenderer, scene: Scene)
}
