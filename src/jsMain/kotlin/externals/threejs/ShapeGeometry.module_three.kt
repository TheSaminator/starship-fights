@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class ShapeGeometry : BufferGeometry {
	constructor(shapes: Shape, curveSegments: Number = definedExternally)
	constructor(shapes: Shape)
	constructor(shapes: Array<Shape>, curveSegments: Number = definedExternally)
	constructor(shapes: Array<Shape>)
	
	override var type: String
	
	companion object {
		fun fromJSON(data: Any): ShapeGeometry
	}
}
