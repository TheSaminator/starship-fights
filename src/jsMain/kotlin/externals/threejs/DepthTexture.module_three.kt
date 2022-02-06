@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct50 {
	var width: Number
	var height: Number
}

external open class DepthTexture(width: Number, height: Number, type: TextureDataType = definedExternally, mapping: Mapping = definedExternally, wrapS: Wrapping = definedExternally, wrapT: Wrapping = definedExternally, magFilter: TextureFilter = definedExternally, minFilter: TextureFilter = definedExternally, anisotropy: Number = definedExternally) : Texture {
	override var image: Any
	override var flipY: Boolean
	override var generateMipmaps: Boolean
	open var isDepthTexture: Boolean
}
