@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct69 {
	var width: Number
	var height: Number
	var widthSegments: Number
	var heightSegments: Number
}

external open class PlaneGeometry(width: Number = definedExternally, height: Number = definedExternally, widthSegments: Number = definedExternally, heightSegments: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct69
	
	companion object {
		fun fromJSON(data: Any): PlaneGeometry
	}
}
