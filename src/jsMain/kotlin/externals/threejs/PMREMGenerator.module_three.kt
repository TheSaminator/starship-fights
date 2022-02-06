@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class PMREMGenerator(renderer: WebGLRenderer) {
	open fun fromScene(scene: Scene, sigma: Number = definedExternally, near: Number = definedExternally, far: Number = definedExternally): WebGLRenderTarget
	open fun fromEquirectangular(equirectangular: Texture): WebGLRenderTarget
	open fun fromCubemap(cubemap: CubeTexture): WebGLRenderTarget
	open fun compileCubemapShader()
	open fun compileEquirectangularShader()
	open fun dispose()
}
