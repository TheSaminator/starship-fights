@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class WebGLPrograms(renderer: WebGLRenderer, cubemaps: WebGLCubeMaps, extensions: WebGLExtensions, capabilities: WebGLCapabilities, bindingStates: WebGLBindingStates, clipping: WebGLClipping) {
	open var programs: Array<WebGLProgram>
	open fun getParameters(material: Material, lights: Any, shadows: Array<Any?>, scene: Scene, obj: Any): Any
	open fun getProgramCacheKey(parameters: Any): String
	open fun getUniforms(material: Material): Any?
	open fun acquireProgram(parameters: Any, cacheKey: String): WebGLProgram
	open fun releaseProgram(program: WebGLProgram)
}
