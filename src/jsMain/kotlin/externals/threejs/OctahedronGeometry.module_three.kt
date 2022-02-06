@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class OctahedronGeometry(radius: Number = definedExternally, detail: Number = definedExternally) : PolyhedronGeometry {
	override var type: String
	
	companion object {
		fun fromJSON(data: Any): OctahedronGeometry
	}
}
