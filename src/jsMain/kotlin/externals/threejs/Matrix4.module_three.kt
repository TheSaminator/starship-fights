@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external open class Matrix4 : Matrix {
	override var elements: Array<Number>
	open fun set(n11: Number, n12: Number, n13: Number, n14: Number, n21: Number, n22: Number, n23: Number, n24: Number, n31: Number, n32: Number, n33: Number, n34: Number, n41: Number, n42: Number, n43: Number, n44: Number): Matrix4
	override fun identity(): Matrix4
	override fun clone(): Matrix4
	open fun copy(m: Matrix4): Matrix4 /* this */
	override fun copy(m: Matrix /* this */): Matrix /* this */
	open fun copyPosition(m: Matrix4): Matrix4
	open fun extractBasis(xAxis: Vector3, yAxis: Vector3, zAxis: Vector3): Matrix4
	open fun makeBasis(xAxis: Vector3, yAxis: Vector3, zAxis: Vector3): Matrix4
	open fun extractRotation(m: Matrix4): Matrix4
	open fun makeRotationFromEuler(euler: Euler): Matrix4
	open fun makeRotationFromQuaternion(q: Quaternion): Matrix4
	open fun lookAt(eye: Vector3, target: Vector3, up: Vector3): Matrix4
	open fun multiply(m: Matrix4): Matrix4
	open fun premultiply(m: Matrix4): Matrix4
	open fun multiplyMatrices(a: Matrix4, b: Matrix4): Matrix4
	open fun multiplyToArray(a: Matrix4, b: Matrix4, r: Array<Number>): Matrix4
	override fun multiplyScalar(s: Number): Matrix4
	override fun determinant(): Number
	override fun transpose(): Matrix4
	open fun setPosition(v: Vector3, y: Number = definedExternally, z: Number = definedExternally): Matrix4
	open fun setPosition(v: Vector3): Matrix4
	open fun setPosition(v: Vector3, y: Number = definedExternally): Matrix4
	open fun setPosition(v: Number, y: Number = definedExternally, z: Number = definedExternally): Matrix4
	open fun setPosition(v: Number): Matrix4
	open fun setPosition(v: Number, y: Number = definedExternally): Matrix4
	override fun invert(): Matrix4
	open fun scale(v: Vector3): Matrix4
	open fun getMaxScaleOnAxis(): Number
	open fun makeTranslation(x: Number, y: Number, z: Number): Matrix4
	open fun makeRotationX(theta: Number): Matrix4
	open fun makeRotationY(theta: Number): Matrix4
	open fun makeRotationZ(theta: Number): Matrix4
	open fun makeRotationAxis(axis: Vector3, angle: Number): Matrix4
	open fun makeScale(x: Number, y: Number, z: Number): Matrix4
	open fun makeShear(xy: Number, xz: Number, yx: Number, yz: Number, zx: Number, zy: Number): Matrix4
	open fun compose(translation: Vector3, rotation: Quaternion, scale: Vector3): Matrix4
	open fun decompose(translation: Vector3, rotation: Quaternion, scale: Vector3): Matrix4
	open fun makePerspective(left: Number, right: Number, bottom: Number, top: Number, near: Number, far: Number): Matrix4
	open fun makePerspective(fov: Number, aspect: Number, near: Number, far: Number): Matrix4
	open fun makeOrthographic(left: Number, right: Number, top: Number, bottom: Number, near: Number, far: Number): Matrix4
	open fun equals(matrix: Matrix4): Boolean
	open fun fromArray(array: Array<Number>, offset: Number = definedExternally): Matrix4
	open fun fromArray(array: Array<Number>): Matrix4
	open fun fromArray(array: ArrayLike<Number>, offset: Number = definedExternally): Matrix4
	open fun fromArray(array: ArrayLike<Number>): Matrix4
	open fun toArray(array: Array<Number> = definedExternally, offset: Number = definedExternally): Array<Number>
	open fun toArray(): ArrayLike<Number>
	open fun toArray(array: Array<Number> = definedExternally): Array<Number>
	open fun toArray(array: Any /* JsTuple<Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number> */ = definedExternally, offset: Number /* 0 */ = definedExternally): dynamic /* JsTuple<Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number> */
	open fun toArray(array: Any /* JsTuple<Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number> */ = definedExternally): dynamic /* JsTuple<Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number, Number> */
	open fun toArray(array: ArrayLike<Number> = definedExternally, offset: Number = definedExternally): ArrayLike<Number>
	open fun toArray(array: ArrayLike<Number> = definedExternally): ArrayLike<Number>
	open fun setFromMatrix3(m: Matrix3): Matrix4
	open fun extractPosition(m: Matrix4): Matrix4
	open fun setRotationFromQuaternion(q: Quaternion): Matrix4
	open fun multiplyVector3(v: Any): Any
	open fun multiplyVector4(v: Any): Any
	open fun multiplyVector3Array(array: Array<Number>): Array<Number>
	open fun rotateAxis(v: Any)
	open fun crossVector(v: Any)
	open fun flattenToArrayOffset(array: Array<Number>, offset: Number): Array<Number>
	open fun getInverse(matrix: Matrix): Matrix
}
