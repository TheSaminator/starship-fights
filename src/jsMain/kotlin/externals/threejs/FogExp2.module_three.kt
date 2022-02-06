@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class FogExp2 : FogBase {
	constructor(hex: Number, density: Number = definedExternally)
	constructor(hex: Number)
	constructor(hex: String, density: Number = definedExternally)
	constructor(hex: String)
	
	override var name: String
	override var color: Color
	open var density: Number
	open var isFogExp2: Boolean
	override fun clone(): FogExp2
	override fun toJSON(): Any
}
