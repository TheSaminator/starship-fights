@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class WebGLCubeRenderTarget(size: Number, options: WebGLRenderTargetOptions = definedExternally) : WebGLRenderTarget {
	override var texture: Texture
	open fun fromEquirectangularTexture(renderer: WebGLRenderer, texture: Texture): WebGLCubeRenderTarget /* this */
	open fun clear(renderer: WebGLRenderer, color: Boolean, depth: Boolean, stencil: Boolean)
}
