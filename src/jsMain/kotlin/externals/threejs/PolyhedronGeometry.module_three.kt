@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct65 {
	var vertices: Array<Number>
	var indices: Array<Number>
	var radius: Number
	var detail: Number
}

external open class PolyhedronGeometry(vertices: Array<Number>, indices: Array<Number>, radius: Number = definedExternally, detail: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct65
	
	companion object {
		fun fromJSON(data: Any): PolyhedronGeometry
	}
}
