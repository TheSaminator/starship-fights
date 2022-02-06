@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct71 {
	var radius: Number
	var widthSegments: Number
	var heightSegments: Number
	var phiStart: Number
	var phiLength: Number
	var thetaStart: Number
	var thetaLength: Number
}

external open class SphereGeometry(radius: Number = definedExternally, widthSegments: Number = definedExternally, heightSegments: Number = definedExternally, phiStart: Number = definedExternally, phiLength: Number = definedExternally, thetaStart: Number = definedExternally, thetaLength: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct71
	
	companion object {
		fun fromJSON(data: Any): SphereGeometry
	}
}
