@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct70 {
	var innerRadius: Number
	var outerRadius: Number
	var thetaSegments: Number
	var phiSegments: Number
	var thetaStart: Number
	var thetaLength: Number
}

external open class RingGeometry(innerRadius: Number = definedExternally, outerRadius: Number = definedExternally, thetaSegments: Number = definedExternally, phiSegments: Number = definedExternally, thetaStart: Number = definedExternally, thetaLength: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct70
	
	companion object {
		fun fromJSON(data: Any): RingGeometry
	}
}
