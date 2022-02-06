@file:JsQualifier("THREE.ShapeUtils")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs.ShapeUtils

import externals.threejs.Vec2

external fun area(contour: Array<Vec2>): Number

external fun triangulateShape(contour: Array<Vec2>, holes: Array<Array<Vec2>>): Array<Array<Number>>

external fun isClockWise(pts: Array<Vec2>): Boolean
