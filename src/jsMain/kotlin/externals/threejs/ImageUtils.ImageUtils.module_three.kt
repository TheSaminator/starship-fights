@file:JsQualifier("THREE.ImageUtils")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs.ImageUtils

import externals.threejs.Mapping
import externals.threejs.Texture

external fun getDataURL(image: Any): String

external var crossOrigin: String

external fun loadTexture(url: String, mapping: Mapping = definedExternally, onLoad: (texture: Texture) -> Unit = definedExternally, onError: (message: String) -> Unit = definedExternally): Texture

external fun loadTextureCube(array: Array<String>, mapping: Mapping = definedExternally, onLoad: (texture: Texture) -> Unit = definedExternally, onError: (message: String) -> Unit = definedExternally): Texture
