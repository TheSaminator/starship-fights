@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Mesh(geometry: BufferGeometry = definedExternally, material: dynamic = definedExternally) : Object3D {
	open var geometry: BufferGeometry
	open var material: dynamic
	open var morphTargetInfluences: Array<Number>?
	open var morphTargetDictionary: AnonymousStruct55?
	open var isMesh: Boolean
	override var type: String
	open fun updateMorphTargets()
	override fun raycast(raycaster: Raycaster, intersects: Array<Intersection>)
}
