@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class SkinnedMesh(geometry: BufferGeometry = definedExternally, material: dynamic = definedExternally, useVertexTexture: Boolean = definedExternally) : Mesh {
	open var bindMode: String
	open var bindMatrix: Matrix4
	open var bindMatrixInverse: Matrix4
	open var skeleton: Skeleton
	open var isSkinnedMesh: Boolean
	open fun bind(skeleton: Skeleton, bindMatrix: Matrix4 = definedExternally)
	open fun pose()
	open fun normalizeSkinWeights()
	override fun updateMatrixWorld(force: Boolean)
	open fun boneTransform(index: Number, target: Vector3): Vector3
}
