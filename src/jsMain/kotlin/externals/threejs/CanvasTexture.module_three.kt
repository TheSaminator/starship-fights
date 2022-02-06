@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.ImageBitmap

external open class CanvasTexture : Texture {
	constructor(canvas: HTMLImageElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, anisotropy: Number = definedExternally)
	constructor(canvas: HTMLImageElement)
	constructor(canvas: HTMLImageElement, mapping: Mapping = definedExternally)
	constructor(canvas: HTMLImageElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally)
	constructor(canvas: HTMLImageElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally)
	constructor(canvas: HTMLImageElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally)
	constructor(canvas: HTMLImageElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally)
	constructor(canvas: HTMLImageElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally)
	constructor(canvas: HTMLImageElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally)
	constructor(canvas: HTMLCanvasElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, anisotropy: Number = definedExternally)
	constructor(canvas: HTMLCanvasElement)
	constructor(canvas: HTMLCanvasElement, mapping: Mapping = definedExternally)
	constructor(canvas: HTMLCanvasElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally)
	constructor(canvas: HTMLCanvasElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally)
	constructor(canvas: HTMLCanvasElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally)
	constructor(canvas: HTMLCanvasElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally)
	constructor(canvas: HTMLCanvasElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally)
	constructor(canvas: HTMLCanvasElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally)
	constructor(canvas: HTMLVideoElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, anisotropy: Number = definedExternally)
	constructor(canvas: HTMLVideoElement)
	constructor(canvas: HTMLVideoElement, mapping: Mapping = definedExternally)
	constructor(canvas: HTMLVideoElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally)
	constructor(canvas: HTMLVideoElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally)
	constructor(canvas: HTMLVideoElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally)
	constructor(canvas: HTMLVideoElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally)
	constructor(canvas: HTMLVideoElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally)
	constructor(canvas: HTMLVideoElement, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally)
	constructor(canvas: ImageBitmap, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, anisotropy: Number = definedExternally)
	constructor(canvas: ImageBitmap)
	constructor(canvas: ImageBitmap, mapping: Mapping = definedExternally)
	constructor(canvas: ImageBitmap, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally)
	constructor(canvas: ImageBitmap, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally)
	constructor(canvas: ImageBitmap, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally)
	constructor(canvas: ImageBitmap, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally)
	constructor(canvas: ImageBitmap, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally)
	constructor(canvas: ImageBitmap, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally)
	
	open var isCanvasTexture: Boolean
}
