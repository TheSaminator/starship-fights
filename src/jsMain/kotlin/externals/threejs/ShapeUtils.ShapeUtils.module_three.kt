@file:JsQualifier("THREE.ShapeUtils")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs.ShapeUtils

import externals.threejs.VecXY

external fun area(contour: Array<VecXY>): Number

external fun triangulateShape(contour: Array<VecXY>, holes: Array<Array<VecXY>>): Array<Array<Number>>

external fun isClockWise(pts: Array<VecXY>): Boolean
