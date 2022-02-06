@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.ArrayBufferView

@Suppress("EXTERNAL_DELEGATION", "NESTED_CLASS_IN_EXTERNAL_INTERFACE")
external interface LoaderUtils {
	fun decodeText(array: ArrayBufferView): String
	fun decodeText(array: ArrayBuffer): String
	fun extractUrlBase(url: String): String
	
	companion object : LoaderUtils by definedExternally
}
