@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.Float32Array

external open class ImmediateRenderObject(material: Material) : Object3D {
	open var isImmediateRenderObject: Boolean
	open var material: Material
	open var hasPositions: Boolean
	open var hasNormals: Boolean
	open var hasColors: Boolean
	open var hasUvs: Boolean
	open var positionArray: Float32Array?
	open var normalArray: Float32Array?
	open var colorArray: Float32Array?
	open var uvArray: Float32Array?
	open var count: Number
	open fun render(renderCallback: () -> Unit)
}
