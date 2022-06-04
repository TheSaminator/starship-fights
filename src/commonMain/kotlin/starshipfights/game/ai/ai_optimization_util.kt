package starshipfights.game.ai

import starshipfights.game.EPSILON
import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

@JvmInline
value class VecN(val values: List<Double>)

val VecN.dimension: Int
	get() = values.size

// close enough
fun Random.nextGaussian() = (1..12).sumOf { nextDouble() } - 6

fun Random.nextUnitVector(size: Int): VecN {
	if (size <= 0)
		throw IllegalArgumentException("Cannot have vector of zero or negative dimension!")
	
	if (size == 1)
		return VecN(listOf(if (nextBoolean()) 1.0 else -1.0))
	
	val vector = VecN((1..size).map { nextGaussian() })
	
	if (vector.isNullVector) // try again
		return nextUnitVector(size)
	
	return vector.normalize()
}

fun Random.nextOrthonormalBasis(size: Int): List<VecN> {
	if (size <= 0)
		throw IllegalArgumentException("Cannot have orthonormal basis of zero or negative dimension!")
	
	if (size == 1)
		return listOf(VecN(listOf(if (nextBoolean()) 1.0 else -1.0)))
	
	val orthogonalBasis = mutableListOf<VecN>()
	while (orthogonalBasis.size < size) {
		val vector = nextUnitVector(size)
		var orthogonal = vector
		for (prevVector in orthogonalBasis)
			orthogonal -= (vector project prevVector)
		
		if (!orthogonal.isNullVector)
			orthogonalBasis.add(orthogonal)
	}
	
	orthogonalBasis.shuffle(this)
	return orthogonalBasis.map { it.normalize() }
}

val VecN.isNullVector: Boolean
	get() {
		return values.all { abs(it) < EPSILON }
	}

fun VecN.normalize(): VecN {
	if (isNullVector)
		throw IllegalArgumentException("Cannot normalize the zero vector!")
	
	val magnitude = sqrt(this dot this)
	
	return this / magnitude
}

infix fun VecN.dot(other: VecN): Double {
	if (dimension != other.dimension)
		throw IllegalArgumentException("Cannot take inner product of vectors of unequal dimensions!")
	
	return (this.values zip other.values).sumOf { (a, b) -> a * b }
}

infix fun VecN.project(onto: VecN): VecN {
	return this * ((this dot onto) / (this dot this))
}

operator fun VecN.plus(other: VecN): VecN {
	if (dimension != other.dimension)
		throw IllegalArgumentException("Cannot take sum of vectors of unequal dimensions!")
	
	return VecN((this.values zip other.values).map { (a, b) -> a + b })
}

operator fun VecN.minus(other: VecN) = this + (other * -1.0)

operator fun VecN.times(scale: Double): VecN = VecN(values.map { it * scale })
operator fun VecN.div(scale: Double): VecN = VecN(values.map { it / scale })

fun Instinct.denormalize(normalValue: Double): Double {
	val zeroToOne = (normalValue + 1) / 2
	return (zeroToOne * (randRange.endInclusive - randRange.start)) + randRange.start
}
