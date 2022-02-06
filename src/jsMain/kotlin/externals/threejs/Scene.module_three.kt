@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Scene : Object3D {
	override var type: String /* "Scene" */
	open var fog: FogBase?
	open var overrideMaterial: Material?
	open var autoUpdate: Boolean
	open var background: dynamic /* Color? | Texture? */
	open var environment: Texture?
	open var isScene: Boolean
	open fun toJSON(meta: Any = definedExternally): Any
	override fun toJSON(meta: AnonymousStruct6): Any
}
