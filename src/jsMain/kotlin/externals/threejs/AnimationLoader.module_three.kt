@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.ErrorEvent
import org.w3c.xhr.ProgressEvent

external open class AnimationLoader(manager: LoadingManager = definedExternally) : Loader<Array<AnimationClip>> {
	open fun load(url: String, onLoad: (response: Array<AnimationClip>) -> Unit, onProgress: (request: ProgressEvent) -> Unit = definedExternally, onError: (event: ErrorEvent) -> Unit = definedExternally)
	open fun parse(json: Any): Array<AnimationClip>
}
