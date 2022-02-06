@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.xhr.ProgressEvent
import kotlin.js.Promise

external interface AnonymousStruct77 {
	@nativeGetter
	operator fun get(header: String): String?
	
	@nativeSetter
	operator fun set(header: String, value: String)
}

external open class Loader<T : Any>(manager: LoadingManager = definedExternally) {
	open var crossOrigin: String
	open var withCredentials: Boolean
	open var path: String
	open var resourcePath: String
	open var manager: LoadingManager
	open var requestHeader: AnonymousStruct77
	open fun loadAsync(url: String, onProgress: (event: ProgressEvent) -> Unit = definedExternally): Promise<T>
	open fun setCrossOrigin(crossOrigin: String): Loader<T> /* this */
	open fun setWithCredentials(value: Boolean): Loader<T> /* this */
	open fun setPath(path: String): Loader<T> /* this */
	open fun setResourcePath(resourcePath: String): Loader<T> /* this */
	open fun setRequestHeader(requestHeader: AnonymousStruct77): Loader<T> /* this */
}
