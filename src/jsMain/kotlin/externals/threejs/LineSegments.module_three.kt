@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external var LineStrip: Number

external var LinePieces: Number

external open class LineSegments(geometry: BufferGeometry = definedExternally, material: dynamic = definedExternally) : Line {
	override var type: String /* "LineSegments" | String */
	open var isLineSegments: Boolean
}
