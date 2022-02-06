@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.ErrorEvent
import org.w3c.xhr.ProgressEvent

external open class BufferGeometryLoader(manager: LoadingManager = definedExternally) : Loader<Any> {
	open fun load(url: String, onLoad: (bufferGeometry: Any /* InstancedBufferGeometry | BufferGeometry */) -> Unit, onProgress: (request: ProgressEvent) -> Unit = definedExternally, onError: (event: ErrorEvent) -> Unit = definedExternally)
	open fun parse(json: Any): dynamic /* InstancedBufferGeometry | BufferGeometry */
}
