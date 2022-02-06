@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct62 {
	var width: Number
	var height: Number
	var depth: Number
	var widthSegments: Number
	var heightSegments: Number
	var depthSegments: Number
}

external open class BoxGeometry(width: Number = definedExternally, height: Number = definedExternally, depth: Number = definedExternally, widthSegments: Number = definedExternally, heightSegments: Number = definedExternally, depthSegments: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct62
	
	companion object {
		fun fromJSON(data: Any): BoxGeometry
	}
}
