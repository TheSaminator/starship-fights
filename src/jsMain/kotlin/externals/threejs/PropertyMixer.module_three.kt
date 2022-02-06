@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class PropertyMixer(binding: Any, typeName: String, valueSize: Number) {
	open var binding: Any
	open var valueSize: Number
	open var buffer: Any
	open var cumulativeWeight: Number
	open var cumulativeWeightAdditive: Number
	open var useCount: Number
	open var referenceCount: Number
	open fun accumulate(accuIndex: Number, weight: Number)
	open fun accumulateAdditive(weight: Number)
	open fun apply(accuIndex: Number)
	open fun saveOriginalState()
	open fun restoreOriginalState()
}
