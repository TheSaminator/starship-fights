@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct84 {
	var value: Any
	var needsUpdate: Boolean
}

external open class WebGLClipping(properties: WebGLProperties) {
	open var uniform: AnonymousStruct84
	open var numPlanes: Number
	open var numIntersection: Number
	open fun init(planes: Array<Any>, enableLocalClipping: Boolean, camera: Camera): Boolean
	open fun beginShadows()
	open fun endShadows()
	open fun setState(material: Material, camera: Camera, useCache: Boolean)
}
