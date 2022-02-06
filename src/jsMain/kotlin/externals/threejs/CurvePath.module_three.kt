@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class CurvePath<T : Vector> : Curve<T> {
	override var type: String
	open var curves: Array<Curve<T>>
	open var autoClose: Boolean
	open fun add(curve: Curve<T>)
	open fun closePath()
	open fun getPoint(t: Number): T
	open fun getCurveLengths(): Array<Number>
}
