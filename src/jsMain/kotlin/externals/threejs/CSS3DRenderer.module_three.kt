@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.HTMLElement

external interface CSS3DRendererParameters {
	var element: HTMLElement?
		get() = definedExternally
		set(value) = definedExternally
}

external open class CSS3DRenderer(parameters: CSS3DRendererParameters = definedExternally) : Renderer {
	override var domElement: HTMLElement
	override fun render(scene: Object3D, camera: Camera)
	override fun setSize(width: Number, height: Number, updateStyle: Boolean)
}
