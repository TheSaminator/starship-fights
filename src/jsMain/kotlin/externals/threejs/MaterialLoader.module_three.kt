@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.xhr.ProgressEvent

external interface AnonymousStruct80 {
	@nativeGetter
	operator fun get(key: String): Texture?
	
	@nativeSetter
	operator fun set(key: String, value: Texture)
}

external open class MaterialLoader(manager: LoadingManager = definedExternally) : Loader<Material> {
	open var textures: AnonymousStruct80
	open fun load(url: String, onLoad: (material: Material) -> Unit, onProgress: (event: ProgressEvent) -> Unit = definedExternally, onError: (event: Any /* Error | ErrorEvent */) -> Unit = definedExternally)
	open fun setTextures(textures: AnonymousStruct80): MaterialLoader /* this */
	open fun parse(json: Any): Material
}
