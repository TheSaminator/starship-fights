@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLShader

external fun WebGLShader(gl: WebGLRenderingContext, type: String, string: String): WebGLShader
