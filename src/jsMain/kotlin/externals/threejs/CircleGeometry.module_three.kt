@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct63 {
	var radius: Number
	var segments: Number
	var thetaStart: Number
	var thetaLength: Number
}

external open class CircleGeometry(radius: Number = definedExternally, segments: Number = definedExternally, thetaStart: Number = definedExternally, thetaLength: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct63
	
	companion object {
		fun fromJSON(data: Any): CircleGeometry
	}
}
