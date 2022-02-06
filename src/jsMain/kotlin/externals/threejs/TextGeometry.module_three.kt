@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface TextGeometryParameters {
	var font: Font
	var size: Number?
		get() = definedExternally
		set(value) = definedExternally
	var height: Number?
		get() = definedExternally
		set(value) = definedExternally
	var curveSegments: Number?
		get() = definedExternally
		set(value) = definedExternally
	var bevelEnabled: Boolean?
		get() = definedExternally
		set(value) = definedExternally
	var bevelThickness: Number?
		get() = definedExternally
		set(value) = definedExternally
	var bevelSize: Number?
		get() = definedExternally
		set(value) = definedExternally
	var bevelOffset: Number?
		get() = definedExternally
		set(value) = definedExternally
	var bevelSegments: Number?
		get() = definedExternally
		set(value) = definedExternally
}

external interface AnonymousStruct72 {
	var font: Font
	var size: Number
	var height: Number
	var curveSegments: Number
	var bevelEnabled: Boolean
	var bevelThickness: Number
	var bevelSize: Number
	var bevelOffset: Number
	var bevelSegments: Number
}

external open class TextGeometry(text: String, parameters: TextGeometryParameters) : ExtrudeGeometry {
	override var type: String
	open var parameters: AnonymousStruct72
}
