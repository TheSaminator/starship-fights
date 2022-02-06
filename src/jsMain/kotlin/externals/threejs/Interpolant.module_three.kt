@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Interpolant(parameterPositions: Any, sampleValues: Any, sampleSize: Number, resultBuffer: Any = definedExternally) {
	open var parameterPositions: Any
	open var sampleValues: Any
	open var valueSize: Number
	open var resultBuffer: Any
	open fun evaluate(time: Number): Any
}
