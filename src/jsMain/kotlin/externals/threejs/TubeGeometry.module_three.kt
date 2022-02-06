@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct75 {
	var path: Curve<Vector3>
	var tubularSegments: Number
	var radius: Number
	var radialSegments: Number
	var closed: Boolean
}

external open class TubeGeometry(path: Curve<Vector3>, tubularSegments: Number = definedExternally, radius: Number = definedExternally, radiusSegments: Number = definedExternally, closed: Boolean = definedExternally) : BufferGeometry {
	override var type: String
	open var parameters: AnonymousStruct75
	open var tangents: Array<Vector3>
	open var normals: Array<Vector3>
	open var binormals: Array<Vector3>
	
	companion object {
		fun fromJSON(data: Any): TubeGeometry
	}
}
