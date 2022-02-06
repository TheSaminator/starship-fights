@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Line(geometry: BufferGeometry = definedExternally, material: dynamic = definedExternally) : Object3D {
	open var geometry: BufferGeometry
	open var material: dynamic
	override var type: String /* "Line" | "LineLoop" | "LineSegments" | String */
	open var isLine: Boolean
	open var morphTargetInfluences: Array<Number>?
	open var morphTargetDictionary: AnonymousStruct55?
	open fun computeLineDistances(): Line /* this */
	override fun raycast(raycaster: Raycaster, intersects: Array<Intersection>)
	open fun updateMorphTargets()
}
