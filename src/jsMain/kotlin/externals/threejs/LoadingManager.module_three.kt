@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import kotlin.js.RegExp

external var DefaultLoadingManager: LoadingManager

external open class LoadingManager(onLoad: () -> Unit = definedExternally, onProgress: (url: String, loaded: Number, total: Number) -> Unit = definedExternally, onError: (url: String) -> Unit = definedExternally) {
	open var onStart: ((url: String, loaded: Number, total: Number) -> Unit)?
	open var onLoad: () -> Unit
	open var onProgress: (url: String, loaded: Number, total: Number) -> Unit
	open var onError: (url: String) -> Unit
	open fun setURLModifier(callback: (url: String) -> String = definedExternally): LoadingManager /* this */
	open fun resolveURL(url: String): String
	open fun itemStart(url: String)
	open fun itemEnd(url: String)
	open fun itemError(url: String)
	open fun addHandler(regex: RegExp, loader: Loader<*>): LoadingManager /* this */
	open fun removeHandler(regex: RegExp): LoadingManager /* this */
	open fun getHandler(file: String): Loader<*>?
}
