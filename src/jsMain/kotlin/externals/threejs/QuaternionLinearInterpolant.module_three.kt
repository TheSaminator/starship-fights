@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class QuaternionLinearInterpolant(parameterPositions: Any, samplesValues: Any, sampleSize: Number, resultBuffer: Any = definedExternally) : Interpolant {
	open fun interpolate_(i1: Number, t0: Number, t: Number, t1: Number): Any
}
