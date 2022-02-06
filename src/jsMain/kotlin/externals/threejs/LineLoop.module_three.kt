@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class LineLoop(geometry: BufferGeometry = definedExternally, material: dynamic = definedExternally) : Line {
	override var type: String /* "LineLoop" */
	open var isLineLoop: Boolean
}
