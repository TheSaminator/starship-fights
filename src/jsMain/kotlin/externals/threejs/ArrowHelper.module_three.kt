@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class ArrowHelper : Object3D {
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally, length: Number = definedExternally, color: Color = definedExternally, headLength: Number = definedExternally, headWidth: Number = definedExternally)
	constructor()
	constructor(dir: Vector3 = definedExternally)
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally)
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally, length: Number = definedExternally)
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally, length: Number = definedExternally, color: Color = definedExternally)
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally, length: Number = definedExternally, color: Color = definedExternally, headLength: Number = definedExternally)
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally, length: Number = definedExternally, color: String = definedExternally, headLength: Number = definedExternally, headWidth: Number = definedExternally)
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally, length: Number = definedExternally, color: String = definedExternally)
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally, length: Number = definedExternally, color: String = definedExternally, headLength: Number = definedExternally)
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally, length: Number = definedExternally, color: Number = definedExternally, headLength: Number = definedExternally, headWidth: Number = definedExternally)
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally, length: Number = definedExternally, color: Number = definedExternally)
	constructor(dir: Vector3 = definedExternally, origin: Vector3 = definedExternally, length: Number = definedExternally, color: Number = definedExternally, headLength: Number = definedExternally)
	
	override var type: String
	open var line: Line
	open var cone: Mesh
	open fun setDirection(dir: Vector3)
	open fun setLength(length: Number, headLength: Number = definedExternally, headWidth: Number = definedExternally)
	open fun setColor(color: Color)
	open fun setColor(color: String)
	open fun setColor(color: Number)
}
