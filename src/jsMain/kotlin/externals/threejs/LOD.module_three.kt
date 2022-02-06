@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct83 {
	var distance: Number
	var `object`: Object3D
}

external open class LOD : Object3D {
	override var type: String /* "LOD" */
	open var levels: Array<AnonymousStruct83>
	open var autoUpdate: Boolean
	open var isLOD: Boolean
	open fun addLevel(obj: Object3D, distance: Number = definedExternally): LOD /* this */
	open fun getCurrentLevel(): Number
	open fun getObjectForDistance(distance: Number): Object3D?
	override fun raycast(raycaster: Raycaster, intersects: Array<Intersection>)
	open fun update(camera: Camera)
	open fun toJSON(meta: Any): Any
	override fun toJSON(meta: AnonymousStruct6): Any
	open var objects: Array<Any>
}
