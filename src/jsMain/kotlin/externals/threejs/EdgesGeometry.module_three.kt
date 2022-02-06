@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct66 {
	var thresholdAngle: Number
}

external open class EdgesGeometry(geometry: BufferGeometry, thresholdAngle: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct66
}
