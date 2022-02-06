@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct74 {
	var radius: Number
	var tube: Number
	var tubularSegments: Number
	var radialSegments: Number
	var p: Number
	var q: Number
}

external open class TorusKnotGeometry(radius: Number = definedExternally, tube: Number = definedExternally, tubularSegments: Number = definedExternally, radialSegments: Number = definedExternally, p: Number = definedExternally, q: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct74
	
	companion object {
		fun fromJSON(data: Any): TorusKnotGeometry
	}
}
