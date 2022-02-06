@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface RenderTarget

external interface RenderItem {
	var id: Number
	var `object`: Object3D
	var geometry: BufferGeometry?
	var material: Material
	var program: WebGLProgram
	var groupOrder: Number
	var renderOrder: Number
	var z: Number
	var group: Group?
}

external open class WebGLRenderList(properties: WebGLProperties) {
	open var opaque: Array<RenderItem>
	open var transparent: Array<RenderItem>
	open fun init()
	open fun push(obj: Object3D, geometry: BufferGeometry?, material: Material, groupOrder: Number, z: Number, group: Group?)
	open fun unshift(obj: Object3D, geometry: BufferGeometry?, material: Material, groupOrder: Number, z: Number, group: Group?)
	open fun sort(opaqueSort: (a: Any, b: Any) -> Number, transparentSort: (a: Any, b: Any) -> Number)
	open fun finish()
}

external open class WebGLRenderLists(properties: WebGLProperties) {
	open fun dispose()
	open fun get(scene: Scene, camera: Camera): WebGLRenderList
}
