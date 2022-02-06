@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.events.EventTarget

external open class WebGLMultipleRenderTargets(width: Number, height: Number, count: Number) : EventTarget {
	open var texture: Array<Texture>
	open var isWebGLMultipleRenderTargets: Any
	open fun setSize(width: Number, height: Number, depth: Number = definedExternally): WebGLMultipleRenderTargets /* this */
	open fun copy(source: WebGLMultipleRenderTargets): WebGLMultipleRenderTargets /* this */
	open fun clone(): WebGLMultipleRenderTargets /* this */
	open fun dispose()
	open fun setTexture(texture: Texture)
}
