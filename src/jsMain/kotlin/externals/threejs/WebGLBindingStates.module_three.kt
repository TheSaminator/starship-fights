@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLRenderingContext

external open class WebGLBindingStates(gl: WebGLRenderingContext, extensions: WebGLExtensions, attributes: WebGLAttributes, capabilities: WebGLCapabilities) {
	open fun setup(obj: Object3D, material: Material, program: WebGLProgram, geometry: BufferGeometry, index: BufferAttribute)
	open fun reset()
	open fun resetDefaultState()
	open fun dispose()
	open fun releaseStatesOfGeometry()
	open fun releaseStatesOfProgram()
	open fun initAttributes()
	open fun enableAttribute(attribute: Number)
	open fun disableUnusedAttributes()
}
