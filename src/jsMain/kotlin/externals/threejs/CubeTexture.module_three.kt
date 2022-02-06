@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class CubeTexture(images: Array<Any> = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, format: PixelFormat = definedExternally, type: TextureDataType = definedExternally, anisotropy: Number = definedExternally, encoding: TextureEncoding = definedExternally) : Texture {
	open var images: Any
	override var flipY: Boolean
	open var isCubeTexture: Boolean
}
