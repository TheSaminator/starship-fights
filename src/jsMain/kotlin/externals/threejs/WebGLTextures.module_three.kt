@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLRenderingContext

external open class WebGLTextures(gl: WebGLRenderingContext, extensions: WebGLExtensions, state: WebGLState, properties: WebGLProperties, capabilities: WebGLCapabilities, utils: WebGLUtils, info: WebGLInfo) {
	open fun allocateTextureUnit()
	open fun resetTextureUnits()
	open fun setTexture2D(texture: Any, slot: Number)
	open fun setTexture2DArray(texture: Any, slot: Number)
	open fun setTexture3D(texture: Any, slot: Number)
	open fun setTextureCube(texture: Any, slot: Number)
	open fun setupRenderTarget(renderTarget: Any)
	open fun updateRenderTargetMipmap(renderTarget: Any)
	open fun updateMultisampleRenderTarget(renderTarget: Any)
	open fun safeSetTexture2D(texture: Any, slot: Number)
	open fun safeSetTextureCube(texture: Any, slot: Number)
}
