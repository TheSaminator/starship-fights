@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct73 {
	var radius: Number
	var tube: Number
	var radialSegments: Number
	var tubularSegments: Number
	var arc: Number
}

external open class TorusGeometry(radius: Number = definedExternally, tube: Number = definedExternally, radialSegments: Number = definedExternally, tubularSegments: Number = definedExternally, arc: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct73
	
	companion object {
		fun fromJSON(data: Any): TorusGeometry
	}
}
