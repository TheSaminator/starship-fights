@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct64 {
	var radiusTop: Number
	var radiusBottom: Number
	var height: Number
	var radialSegments: Number
	var heightSegments: Number
	var openEnded: Boolean
	var thetaStart: Number
	var thetaLength: Number
}

external open class CylinderGeometry(radiusTop: Number = definedExternally, radiusBottom: Number = definedExternally, height: Number = definedExternally, radialSegments: Number = definedExternally, heightSegments: Number = definedExternally, openEnded: Boolean = definedExternally, thetaStart: Number = definedExternally, thetaLength: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct64
	
	companion object {
		fun fromJSON(data: Any): CylinderGeometry
	}
}
