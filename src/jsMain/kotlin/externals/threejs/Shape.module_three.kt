@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct61 {
	var shape: Array<Vector2>
	var holes: Array<Array<Vector2>>
}

external open class Shape(points: Array<Vector2> = definedExternally) : Path {
	override var type: String
	open var uuid: String
	open var holes: Array<Path>
	open fun getPointsHoles(divisions: Number): Array<Array<Vector2>>
	open fun extractPoints(divisions: Number): AnonymousStruct61
}
