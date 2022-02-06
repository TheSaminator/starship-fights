@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Font(jsondata: Any) {
	open var type: String
	open var data: String
	open fun generateShapes(text: String, size: Number): Array<Shape>
}
