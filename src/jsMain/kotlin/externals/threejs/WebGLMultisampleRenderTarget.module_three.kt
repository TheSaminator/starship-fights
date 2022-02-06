@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class WebGLMultisampleRenderTarget(width: Number, height: Number, options: WebGLRenderTargetOptions = definedExternally) : WebGLRenderTarget {
	open var isWebGLMultisampleRenderTarget: Boolean
	open var samples: Number
}
