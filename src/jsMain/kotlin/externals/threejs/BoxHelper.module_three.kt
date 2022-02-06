@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class BoxHelper : LineSegments {
	constructor(obj: Object3D, color: Color = definedExternally)
	constructor(obj: Object3D)
	constructor(obj: Object3D, color: String = definedExternally)
	constructor(obj: Object3D, color: Number = definedExternally)
	
	override var type: String
	open fun update(obj: Object3D = definedExternally)
	open fun setFromObject(obj: Object3D): BoxHelper /* this */
}
