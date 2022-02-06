@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Sprite(material: SpriteMaterial = definedExternally) : Object3D {
	override var type: String /* "Sprite" */
	open var isSprite: Boolean
	open var geometry: BufferGeometry
	open var material: SpriteMaterial
	open var center: Vector2
	override fun raycast(raycaster: Raycaster, intersects: Array<Intersection>)
	open fun copy(source: Sprite /* this */): Sprite /* this */
}
