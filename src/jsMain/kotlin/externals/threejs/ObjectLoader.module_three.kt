@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.HTMLImageElement
import org.w3c.xhr.ProgressEvent
import kotlin.js.Promise

external interface AnonymousStruct78 {
	@nativeGetter
	operator fun get(key: String): dynamic /* InstancedBufferGeometry? | BufferGeometry? */
	
	@nativeSetter
	operator fun set(key: String, value: InstancedBufferGeometry)
	
	@nativeSetter
	operator fun set(key: String, value: BufferGeometry)
}

external interface AnonymousStruct79 {
	@nativeGetter
	operator fun get(key: String): HTMLImageElement?
	
	@nativeSetter
	operator fun set(key: String, value: HTMLImageElement)
}

external open class ObjectLoader(manager: LoadingManager = definedExternally) : Loader<Object3D> {
	open fun <ObjectType : Object3D> load(url: String, onLoad: (obj: ObjectType) -> Unit = definedExternally, onProgress: (event: ProgressEvent) -> Unit = definedExternally, onError: (event: Any /* Error | ErrorEvent */) -> Unit = definedExternally)
	open fun <ObjectType : Object3D> loadAsync(url: String, onProgress: (event: ProgressEvent) -> Unit = definedExternally): Promise<ObjectType>
	open fun <T : Object3D> parse(json: Any, onLoad: (obj: Object3D) -> Unit = definedExternally): T
	open fun <T : Object3D> parseAsync(json: Any): Promise<T>
	open fun parseGeometries(json: Any): AnonymousStruct78
	open fun parseMaterials(json: Any, textures: Array<Texture>): Array<Material>
	open fun parseAnimations(json: Any): Array<AnimationClip>
	open fun parseImages(json: Any, onLoad: () -> Unit): AnonymousStruct79
	open fun parseImagesAsync(json: Any): Promise<AnonymousStruct79>
	open fun parseTextures(json: Any, images: Any): Array<Texture>
	open fun <T : Object3D> parseObject(data: Any, geometries: Array<Any>, materials: Array<Material>, animations: Array<AnimationClip>): T
}
