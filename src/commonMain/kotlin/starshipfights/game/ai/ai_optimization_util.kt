package starshipfights.game.ai

import starshipfights.game.EPSILON
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

// close enough
fun Random.nextGaussian() = (1..12).sumOf { nextDouble() } - 6

fun Random.nextUnitVector(size: Int): List<Double> {
	if (size <= 0)
		throw IllegalArgumentException("Cannot have vector of zero or negative dimension!")
	
	if (size == 1)
		return listOf(if (nextBoolean()) 1.0 else -1.0)
	
	return (1..size).map { nextGaussian() }.normalize()
}

fun Random.nextOrthonormalBasis(size: Int): List<List<Double>> {
	if (size <= 0)
		throw IllegalArgumentException("Cannot have orthonormal basis of zero or negative dimension!")
	
	if (size == 1)
		return listOf(listOf(if (nextBoolean()) 1.0 else -1.0))
	
	val orthogonalBasis = mutableListOf<List<Double>>()
	while (orthogonalBasis.size < size) {
		val vector = nextUnitVector(size)
		var orthogonal = vector
		for (prevVector in orthogonalBasis)
			orthogonal = orthogonal minus (vector project prevVector)
		
		if (!orthogonal.isNullVector)
			orthogonalBasis.add(orthogonal)
	}
	
	orthogonalBasis.shuffle(this)
	return orthogonalBasis.map { it.normalize() }
}

val Iterable<Double>.isNullVector: Boolean
	get() {
		return all { abs(it) < EPSILON }
	}

fun Iterable<Double>.normalize(): List<Double> {
	val magnitude = sqrt(sumOf { it * it })
	if (magnitude < EPSILON)
		throw IllegalArgumentException("Cannot normalize the zero vector!")
	
	return this div magnitude
}

infix fun Iterable<Double>.dot(other: Iterable<Double>): Double {
	if (count() != other.count())
		throw IllegalArgumentException("Cannot take inner product of vectors of unequal dimensions!")
	
	return (this zip other).sumOf { (a, b) -> a * b }
}

infix fun Iterable<Double>.project(onto: Iterable<Double>): List<Double> {
	if (count() != onto.count())
		throw IllegalArgumentException("Cannot take inner product of vectors of unequal dimensions!")
	
	return this times ((this dot onto) / (this dot this))
}

infix fun Iterable<Double>.plus(other: Iterable<Double>): List<Double> {
	if (count() != other.count())
		throw IllegalArgumentException("Cannot take sum of vectors of unequal dimensions!")
	
	return (this zip other).map { (a, b) -> a + b }
}

infix fun Iterable<Double>.minus(other: Iterable<Double>) = this plus (other times -1.0)

infix fun Iterable<Double>.times(scale: Double): List<Double> = map { it * scale }
infix fun Iterable<Double>.div(scale: Double): List<Double> = map { it / scale }

fun Instinct.denormalize(normalValue: Double): Double {
	val zeroToOne = (normalValue + 1) / 2
	return (zeroToOne * (randRange.endInclusive - randRange.start)) + randRange.start
}
