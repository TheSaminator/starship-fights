@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class WebGLShadowMap(_renderer: WebGLRenderer, _objects: WebGLObjects, _capabilities: WebGLCapabilities) {
	open var enabled: Boolean
	open var autoUpdate: Boolean
	open var needsUpdate: Boolean
	open var type: ShadowMapType
	open fun render(shadowsArray: Array<Light>, scene: Scene, camera: Camera)
	open var cullFace: Any
}
