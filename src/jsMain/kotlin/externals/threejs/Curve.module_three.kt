@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface AnonymousStruct60 {
	var tangents: Array<Vector3>
	var normals: Array<Vector3>
	var binormals: Array<Vector3>
}

external open class Curve<T : Vector> {
	open var type: String
	open var arcLengthDivisions: Number
	open fun getPoint(t: Number, optionalTarget: T = definedExternally): T
	open fun getPointAt(u: Number, optionalTarget: T = definedExternally): T
	open fun getPoints(divisions: Number = definedExternally): Array<T>
	open fun getSpacedPoints(divisions: Number = definedExternally): Array<T>
	open fun getLength(): Number
	open fun getLengths(divisions: Number = definedExternally): Array<Number>
	open fun updateArcLengths()
	open fun getUtoTmapping(u: Number, distance: Number): Number
	open fun getTangent(t: Number, optionalTarget: T = definedExternally): T
	open fun getTangentAt(u: Number, optionalTarget: T = definedExternally): T
	open fun computeFrenetFrames(segments: Number, closed: Boolean = definedExternally): AnonymousStruct60
	open fun clone(): Curve<T> /* this */
	open fun copy(source: Curve<T>): Curve<T> /* this */
	open fun toJSON(): Any?
	open fun fromJSON(json: Any?): Curve<T> /* this */
	
	companion object {
		fun create(constructorFunc: () -> Unit, getPointFunc: () -> Unit): () -> Unit
	}
}
