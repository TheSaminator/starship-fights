@file:JsQualifier("THREE")
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

external interface Matrix {
	var elements: Array<Number>
	fun identity(): Matrix
	fun copy(m: Matrix /* this */): Matrix /* this */
	fun multiplyScalar(s: Number): Matrix
	fun determinant(): Number
	fun transpose(): Matrix
	fun invert(): Matrix
	fun clone(): Matrix
}

external open class Matrix3 : Matrix {
	override var elements: Array<Number>
	open fun set(n11: Number, n12: Number, n13: Number, n21: Number, n22: Number, n23: Number, n31: Number, n32: Number, n33: Number): Matrix3
	override fun identity(): Matrix3
	override fun clone(): Matrix3 /* this */
	open fun copy(m: Matrix3): Matrix3 /* this */
	override fun copy(m: Matrix /* this */): Matrix /* this */
	open fun extractBasis(xAxis: Vector3, yAxis: Vector3, zAxis: Vector3): Matrix3
	open fun setFromMatrix4(m: Matrix4): Matrix3
	override fun multiplyScalar(s: Number): Matrix3
	override fun determinant(): Number
	override fun invert(): Matrix3
	override fun transpose(): Matrix3
	open fun getNormalMatrix(matrix4: Matrix4): Matrix3
	open fun transposeIntoArray(r: Array<Number>): Matrix3
	open fun setUvTransform(tx: Number, ty: Number, sx: Number, sy: Number, rotation: Number, cx: Number, cy: Number): Matrix3
	open fun scale(sx: Number, sy: Number): Matrix3
	open fun rotate(theta: Number): Matrix3
	open fun translate(tx: Number, ty: Number): Matrix3
	open fun equals(matrix: Matrix3): Boolean
	open fun fromArray(array: Array<Number>, offset: Number = definedExternally): Matrix3
	open fun fromArray(array: Array<Number>): Matrix3
	open fun fromArray(array: ArrayLike<Number>, offset: Number = definedExternally): Matrix3
	open fun fromArray(array: ArrayLike<Number>): Matrix3
	open fun toArray(array: Array<Number> = definedExternally, offset: Number = definedExternally): Array<Number>
	open fun toArray(): dynamic /* ArrayLike */
	open fun toArray(array: Array<Number> = definedExternally): Array<Number>
	open fun toArray(array: Any /* JsTuple<Number, Number, Number, Number, Number, Number, Number, Number, Number> */ = definedExternally, offset: Number /* 0 */ = definedExternally): dynamic /* JsTuple<Number, Number, Number, Number, Number, Number, Number, Number, Number> */
	open fun toArray(array: Any /* JsTuple<Number, Number, Number, Number, Number, Number, Number, Number, Number> */ = definedExternally): dynamic /* JsTuple<Number, Number, Number, Number, Number, Number, Number, Number, Number> */
	open fun toArray(array: ArrayLike<Number> = definedExternally, offset: Number = definedExternally): ArrayLike<Number>
	open fun toArray(array: ArrayLike<Number> = definedExternally): ArrayLike<Number>
	open fun multiply(m: Matrix3): Matrix3
	open fun premultiply(m: Matrix3): Matrix3
	open fun multiplyMatrices(a: Matrix3, b: Matrix3): Matrix3
	open fun multiplyVector3(vector: Vector3): Any
	open fun multiplyVector3Array(a: Any): Any
	open fun getInverse(matrix: Matrix4, throwOnDegenerate: Boolean = definedExternally): Matrix3
	open fun getInverse(matrix: Matrix4): Matrix3
	open fun getInverse(matrix: Matrix): Matrix
	open fun flattenToArrayOffset(array: Array<Number>, offset: Number): Array<Number>
}
