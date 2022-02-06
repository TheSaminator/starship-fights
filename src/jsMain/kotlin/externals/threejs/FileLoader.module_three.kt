@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.ErrorEvent
import org.w3c.dom.MimeType
import org.w3c.xhr.ProgressEvent

external open class FileLoader(manager: LoadingManager = definedExternally) : Loader<Any> {
	open var mimeType: MimeType?
	open var responseType: String?
	open fun load(url: String, onLoad: (response: Any /* String | ArrayBuffer */) -> Unit = definedExternally, onProgress: (request: ProgressEvent) -> Unit = definedExternally, onError: (event: ErrorEvent) -> Unit = definedExternally): Any
	open fun setMimeType(mimeType: MimeType): FileLoader
	open fun setResponseType(responseType: String): FileLoader
}
