@file:JsQualifier("THREE.AnimationUtils")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs.AnimationUtils

import externals.threejs.AnimationClip

external fun arraySlice(array: Any, from: Number, to: Number): Any

external fun convertArray(array: Any, type: Any, forceClone: Boolean): Any

external fun isTypedArray(obj: Any): Boolean

external fun getKeyFrameOrder(times: Array<Number>): Array<Number>

external fun sortedArray(values: Array<Any>, stride: Number, order: Array<Number>): Array<Any>

external fun flattenJSON(jsonKeys: Array<String>, times: Array<Any>, values: Array<Any>, valuePropertyName: String)

external fun subclip(sourceClip: AnimationClip, name: String, startFrame: Number, endFrame: Number, fps: Number = definedExternally): AnimationClip

external fun makeClipAdditive(targetClip: AnimationClip, referenceFrame: Number = definedExternally, referenceClip: AnimationClip = definedExternally, fps: Number = definedExternally): AnimationClip
