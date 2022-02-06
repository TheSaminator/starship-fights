@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.Float32Array

external open class Skeleton(bones: Array<Bone>, boneInverses: Array<Matrix4> = definedExternally) {
	open var uuid: String
	open var bones: Array<Bone>
	open var boneInverses: Array<Matrix4>
	open var boneMatrices: Float32Array
	open var boneTexture: DataTexture?
	open var boneTextureSize: Number
	open var frame: Number
	open fun init()
	open fun calculateInverses()
	open fun computeBoneTexture(): Skeleton /* this */
	open fun pose()
	open fun update()
	open fun clone(): Skeleton
	open fun getBoneByName(name: String): Bone?
	open fun dispose()
	open var useVertexTexture: Boolean
}
