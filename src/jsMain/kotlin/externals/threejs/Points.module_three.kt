@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Points<TGeometry : BufferGeometry, TMaterial>(geometry: TGeometry = definedExternally, material: TMaterial = definedExternally) : Object3D {
	override var type: String /* "Points" */
	open var morphTargetInfluences: Array<Number>?
	open var morphTargetDictionary: AnonymousStruct55?
	open var isPoints: Boolean
	open var geometry: TGeometry
	open var material: TMaterial
	override fun raycast(raycaster: Raycaster, intersects: Array<Intersection>)
	open fun updateMorphTargets()
}
