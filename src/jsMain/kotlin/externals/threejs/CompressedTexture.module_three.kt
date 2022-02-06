@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.ImageData

external open class CompressedTexture(mipmaps: Array<ImageData>, width: Number, height: Number, format: CompressedPixelFormat = definedExternally, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, anisotropy: Number = definedExternally, encoding: TextureEncoding = definedExternally) : Texture {
	override var image: Any
	override var mipmaps: Array<Any>
	override var flipY: Boolean
	override var generateMipmaps: Boolean
	open var isCompressedTexture: Boolean
}
