@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.ErrorEvent
import org.w3c.xhr.ProgressEvent
import kotlin.js.Promise

external open class CubeTextureLoader(manager: LoadingManager = definedExternally) : Loader<CubeTexture> {
	open fun load(urls: Array<String>, onLoad: (texture: CubeTexture) -> Unit = definedExternally, onProgress: (event: ProgressEvent) -> Unit = definedExternally, onError: (event: ErrorEvent) -> Unit = definedExternally): CubeTexture
	open fun loadAsync(urls: Array<String>, onProgress: (event: ProgressEvent) -> Unit = definedExternally): Promise<CubeTexture>
}
