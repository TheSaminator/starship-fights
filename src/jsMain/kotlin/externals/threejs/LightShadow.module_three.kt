@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class LightShadow(camera: Camera) {
	open var camera: Camera
	open var bias: Number
	open var normalBias: Number
	open var radius: Number
	open var mapSize: Vector2
	open var map: RenderTarget
	open var mapPass: RenderTarget
	open var matrix: Matrix4
	open var autoUpdate: Boolean
	open var needsUpdate: Boolean
	open fun copy(source: LightShadow): LightShadow /* this */
	open fun clone(recursive: Boolean = definedExternally): LightShadow /* this */
	open fun toJSON(): Any
	open fun getFrustum(): Number
	open fun updateMatrices(light: Light, viewportIndex: Number = definedExternally)
	open fun getViewport(viewportIndex: Number): Vector4
	open fun getFrameExtents(): Vector2
	open fun dispose()
}
