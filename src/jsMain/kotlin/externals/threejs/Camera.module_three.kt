@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Camera : Object3D {
	open var matrixWorldInverse: Matrix4
	open var projectionMatrix: Matrix4
	open var projectionMatrixInverse: Matrix4
	open var isCamera: Boolean
	override fun getWorldDirection(target: Vector3): Vector3
	override fun updateMatrixWorld(force: Boolean)
}
