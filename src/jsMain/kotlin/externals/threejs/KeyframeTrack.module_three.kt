@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.Float32Array

external open class KeyframeTrack(name: String, times: ArrayLike<Any>, values: ArrayLike<Any>, interpolation: InterpolationModes = definedExternally) {
	open var name: String
	open var times: Float32Array
	open var values: Float32Array
	open var ValueTypeName: String
	open var TimeBufferType: Float32Array
	open var ValueBufferType: Float32Array
	open var DefaultInterpolation: InterpolationModes
	open fun InterpolantFactoryMethodDiscrete(result: Any): DiscreteInterpolant
	open fun InterpolantFactoryMethodLinear(result: Any): LinearInterpolant
	open fun InterpolantFactoryMethodSmooth(result: Any): CubicInterpolant
	open fun setInterpolation(interpolation: InterpolationModes): KeyframeTrack
	open fun getInterpolation(): InterpolationModes
	open fun getValueSize(): Number
	open fun shift(timeOffset: Number): KeyframeTrack
	open fun scale(timeScale: Number): KeyframeTrack
	open fun trim(startTime: Number, endTime: Number): KeyframeTrack
	open fun validate(): Boolean
	open fun optimize(): KeyframeTrack
	open fun clone(): KeyframeTrack /* this */
	
	companion object {
		fun toJSON(track: KeyframeTrack): Any
	}
}
