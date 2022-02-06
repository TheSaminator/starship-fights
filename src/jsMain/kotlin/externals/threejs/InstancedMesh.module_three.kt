@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class InstancedMesh(geometry: BufferGeometry?, material: dynamic, count: Number) : Mesh {
	open var count: Number
	open var instanceColor: BufferAttribute?
	open var instanceMatrix: BufferAttribute
	open var isInstancedMesh: Boolean
	open fun getColorAt(index: Number, color: Color)
	open fun getMatrixAt(index: Number, matrix: Matrix4)
	open fun setColorAt(index: Number, color: Color)
	open fun setMatrixAt(index: Number, matrix: Matrix4)
	open fun dispose()
}
