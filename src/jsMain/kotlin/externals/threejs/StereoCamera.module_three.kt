@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class StereoCamera : Camera {
	override var type: String /* "StereoCamera" */
	open var aspect: Number
	open var eyeSep: Number
	open var cameraL: PerspectiveCamera
	open var cameraR: PerspectiveCamera
	open fun update(camera: PerspectiveCamera)
}
