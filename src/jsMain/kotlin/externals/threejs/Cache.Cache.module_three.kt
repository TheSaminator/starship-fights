@file:JsQualifier("THREE.Cache")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs.Cache

external var enabled: Boolean

external var files: Any

external fun add(key: String, file: Any)

external fun get(key: String): Any

external fun remove(key: String)

external fun clear()
