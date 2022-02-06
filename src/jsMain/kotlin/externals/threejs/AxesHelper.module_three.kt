@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class AxesHelper(size: Number = definedExternally) : LineSegments {
	override var type: String
	open fun setColors(xAxisColor: Color, yAxisColor: Color, zAxisColor: Color): AxesHelper /* this */
	open fun dispose()
}
