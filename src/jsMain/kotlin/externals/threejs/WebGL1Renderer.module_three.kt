@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class WebGL1Renderer(parameters: WebGLRendererParameters = definedExternally) : WebGLRenderer {
	open var isWebGL1Renderer: Boolean
}
