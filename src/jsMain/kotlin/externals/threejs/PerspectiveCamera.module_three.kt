@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct58 {
	var enabled: Boolean
	var fullWidth: Number
	var fullHeight: Number
	var offsetX: Number
	var offsetY: Number
	var width: Number
	var height: Number
}

external open class PerspectiveCamera(fov: Number = definedExternally, aspect: Number = definedExternally, near: Number = definedExternally, far: Number = definedExternally) : Camera {
	override var type: String /* "PerspectiveCamera" */
	open var isPerspectiveCamera: Boolean
	open var zoom: Number
	open var fov: Number
	open var aspect: Number
	open var near: Number
	open var far: Number
	open var focus: Number
	open var view: AnonymousStruct58?
	open var filmGauge: Number
	open var filmOffset: Number
	open fun setFocalLength(focalLength: Number)
	open fun getFocalLength(): Number
	open fun getEffectiveFOV(): Number
	open fun getFilmWidth(): Number
	open fun getFilmHeight(): Number
	open fun setViewOffset(fullWidth: Number, fullHeight: Number, x: Number, y: Number, width: Number, height: Number)
	open fun clearViewOffset()
	open fun updateProjectionMatrix()
	open fun toJSON(meta: Any = definedExternally): Any
	override fun toJSON(meta: AnonymousStruct6): Any
	open fun setLens(focalLength: Number, frameHeight: Number = definedExternally)
}
