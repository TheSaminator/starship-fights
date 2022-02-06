@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct76 {
	@nativeGetter
	operator fun get(id: String): Array<Number>?
	
	@nativeSetter
	operator fun set(id: String, value: Array<Number>)
}

external open class CameraHelper(camera: Camera) : LineSegments {
	open var camera: Camera
	open var pointMap: AnonymousStruct76
	override var type: String
	open fun update()
	open fun dispose()
}
