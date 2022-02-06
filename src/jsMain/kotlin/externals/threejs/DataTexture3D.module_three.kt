@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.ArrayBufferView

external open class DataTexture3D : Texture {
	constructor(data: ArrayBufferView, width: Number, height: Number, depth: Number)
	constructor(data: ArrayBuffer, width: Number, height: Number, depth: Number)
	
	override var magFilter: TextureFilter
	override var minFilter: TextureFilter
	open var wrapR: Boolean
	override var flipY: Boolean
	override var generateMipmaps: Boolean
	open var isDataTexture3D: Boolean
}
