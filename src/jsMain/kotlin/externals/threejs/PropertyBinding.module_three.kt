@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface ParseTrackNameResults {
	var nodeName: String
	var objectName: String
	var objectIndex: String
	var propertyName: String
	var propertyIndex: String
}

external interface AnonymousStruct55 {
	@nativeGetter
	operator fun get(bindingType: String): Number?
	
	@nativeSetter
	operator fun set(bindingType: String, value: Number)
}

external open class PropertyBinding(rootNode: Any, path: String, parsedPath: Any = definedExternally) {
	open var path: String
	open var parsedPath: Any
	open var node: Any
	open var rootNode: Any
	open fun getValue(targetArray: Any, offset: Number): Any
	open fun setValue(sourceArray: Any, offset: Number)
	open fun bind()
	open fun unbind()
	open var BindingType: AnonymousStruct55
	open var Versioning: AnonymousStruct55
	open var GetterByBindingType: Array<() -> Unit>
	open var SetterByBindingTypeAndVersioning: Array<Array<() -> Unit>>
	
	open class Composite(targetGroup: Any, path: Any, parsedPath: Any = definedExternally) {
		open fun getValue(array: Any, offset: Number): Any
		open fun setValue(array: Any, offset: Number)
		open fun bind()
		open fun unbind()
	}
	
	companion object {
		fun create(root: Any, path: Any, parsedPath: Any = definedExternally): dynamic /* PropertyBinding | PropertyBinding.Composite */
		fun sanitizeNodeName(name: String): String
		fun parseTrackName(trackName: String): ParseTrackNameResults
		fun findNode(root: Any, nodeName: String): Any
	}
}
