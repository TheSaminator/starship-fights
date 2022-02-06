@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.ErrorEvent
import org.w3c.xhr.ProgressEvent

external open class TextureLoader(manager: LoadingManager = definedExternally) : Loader<Texture> {
	open fun load(url: String, onLoad: (texture: Texture) -> Unit = definedExternally, onProgress: (event: ProgressEvent) -> Unit = definedExternally, onError: (event: ErrorEvent) -> Unit = definedExternally): Texture
}
