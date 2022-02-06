@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct67 {
	var points: Array<Vector2>
	var segments: Number
	var phiStart: Number
	var phiLength: Number
}

external open class LatheGeometry(points: Array<Vector2>, segments: Number = definedExternally, phiStart: Number = definedExternally, phiLength: Number = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct67
	
	companion object {
		fun fromJSON(data: Any): LatheGeometry
	}
}
