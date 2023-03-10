@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct86 {
	var directionalLength: Number
	var pointLength: Number
	var spotLength: Number
	var rectAreaLength: Number
	var hemiLength: Number
	var numDirectionalShadows: Number
	var numPointShadows: Number
	var numSpotShadows: Number
}

external interface AnonymousStruct87 {
	var version: Number
	var hash: AnonymousStruct86
	var ambient: Array<Number>
	var probe: Array<Any>
	var directional: Array<Any>
	var directionalShadow: Array<Any>
	var directionalShadowMap: Array<Any>
	var directionalShadowMatrix: Array<Any>
	var spot: Array<Any>
	var spotShadow: Array<Any>
	var spotShadowMap: Array<Any>
	var spotShadowMatrix: Array<Any>
	var rectArea: Array<Any>
	var point: Array<Any>
	var pointShadow: Array<Any>
	var pointShadowMap: Array<Any>
	var pointShadowMatrix: Array<Any>
	var hemi: Array<Any>
}

external open class WebGLLights(extensions: WebGLExtensions, capabilities: WebGLCapabilities) {
	open var state: AnonymousStruct87
	open fun get(light: Any): Any
	open fun setup(lights: Any)
	open fun setupView(lights: Any, camera: Any)
}
