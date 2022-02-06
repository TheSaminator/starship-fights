@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.ArrayBufferView

external open class DataTexture : Texture {
	constructor(data: ArrayBufferView, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, anisotropy: Number = definedExternally, encoding: TextureEncoding = definedExternally)
	constructor(data: ArrayBufferView, width: Number, height: Number)
	constructor(data: ArrayBufferView, width: Number, height: Number, format: PixelFormat = definedExternally)
	constructor(data: ArrayBufferView, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally)
	constructor(data: ArrayBufferView, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally)
	constructor(data: ArrayBufferView, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally)
	constructor(data: ArrayBufferView, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally)
	constructor(data: ArrayBufferView, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally)
	constructor(data: ArrayBufferView, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally)
	constructor(data: ArrayBufferView, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, anisotropy: Number = definedExternally)
	constructor(data: ArrayBuffer, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, anisotropy: Number = definedExternally, encoding: TextureEncoding = definedExternally)
	constructor(data: ArrayBuffer, width: Number, height: Number)
	constructor(data: ArrayBuffer, width: Number, height: Number, format: PixelFormat = definedExternally)
	constructor(data: ArrayBuffer, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally)
	constructor(data: ArrayBuffer, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally)
	constructor(data: ArrayBuffer, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally)
	constructor(data: ArrayBuffer, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally)
	constructor(data: ArrayBuffer, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally)
	constructor(data: ArrayBuffer, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally)
	constructor(data: ArrayBuffer, width: Number, height: Number, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, anisotropy: Number = definedExternally)
	
	override var image: Any
	override var flipY: Boolean
	override var generateMipmaps: Boolean
	override var unpackAlignment: Number
	override var format: PixelFormat
	open var isDataTexture: Boolean
}
