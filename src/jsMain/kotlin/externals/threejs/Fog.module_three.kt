@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface FogBase {
	var name: String
	var color: Color
	fun clone(): FogBase
	fun toJSON(): Any
}

external open class Fog : FogBase {
	constructor(color: Color, near: Number = definedExternally, far: Number = definedExternally)
	constructor(color: Color)
	constructor(color: Color, near: Number = definedExternally)
	constructor(color: String, near: Number = definedExternally, far: Number = definedExternally)
	constructor(color: String)
	constructor(color: String, near: Number = definedExternally)
	constructor(color: Number, near: Number = definedExternally, far: Number = definedExternally)
	constructor(color: Number)
	constructor(color: Number, near: Number = definedExternally)
	
	override var name: String
	override var color: Color
	open var near: Number
	open var far: Number
	open var isFog: Boolean
	override fun clone(): Fog
	override fun toJSON(): Any
}
