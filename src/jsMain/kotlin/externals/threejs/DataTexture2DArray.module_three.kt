@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.ArrayBufferView

external open class DataTexture2DArray : Texture {
	constructor(data: ArrayBufferView = definedExternally, width: Number = definedExternally, height: Number = definedExternally, depth: Number = definedExternally)
	constructor()
	constructor(data: ArrayBufferView = definedExternally)
	constructor(data: ArrayBufferView = definedExternally, width: Number = definedExternally)
	constructor(data: ArrayBufferView = definedExternally, width: Number = definedExternally, height: Number = definedExternally)
	constructor(data: ArrayBuffer = definedExternally, width: Number = definedExternally, height: Number = definedExternally, depth: Number = definedExternally)
	constructor(data: ArrayBuffer = definedExternally)
	constructor(data: ArrayBuffer = definedExternally, width: Number = definedExternally)
	constructor(data: ArrayBuffer = definedExternally, width: Number = definedExternally, height: Number = definedExternally)
	
	override var magFilter: TextureFilter
	override var minFilter: TextureFilter
	open var wrapR: Boolean
	override var flipY: Boolean
	override var generateMipmaps: Boolean
	open var isDataTexture2DArray: Boolean
}
