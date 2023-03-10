package net.starshipfights.game.ai

import net.starshipfights.game.EPSILON
import net.starshipfights.game.Vec2
import net.starshipfights.game.div
import kotlin.math.absoluteValue
import kotlin.math.nextUp
import kotlin.math.pow
import kotlin.math.sign
import kotlin.random.Random

expect fun logDebug(message: Any?)
expect fun logInfo(message: Any?)
expect fun logWarning(message: Any?)
expect fun logError(message: Any?)

fun ClosedFloatingPointRange<Double>.random(random: Random = Random) = random.nextDouble(start, endInclusive.nextUp())

val ClosedFloatingPointRange<Double>.size: Double
	get() = endInclusive.nextUp() - start

fun <T : Any> Map<T, Double>.weightedRandom(random: Random = Random): T {
	return weightedRandomOrNull(random) ?: error("Cannot take weighted random of effectively-empty collection!")
}

fun <T : Any> Map<T, Double>.weightedRandomOrNull(random: Random = Random): T? {
	if (values.none { it >= EPSILON }) return null
	
	val total = values.sum()
	if (total < EPSILON) return null
	
	var hasChoice = false
	var choice = random.nextDouble(total)
	for ((result, chance) in this) {
		if (chance < EPSILON) continue
		if (chance >= choice)
			return result
		choice -= chance
		hasChoice = true
	}
	
	return if (hasChoice)
		keys.last()
	else null
}

fun <T, U, V> Map<T, Map<U, V>>.flatten(): Map<Pair<T, U>, V> =
	toList().flatMap { (k, v) ->
		v.map { (l, w) ->
			(k to l) to w
		}
	}.toMap()

fun <T, U> Map<T, Set<U>>.transpose(): Map<U, Set<T>> =
	flatMap { (k, v) -> v.map { it to k } }
		.groupBy(Pair<U, T>::first, Pair<U, T>::second)
		.mapValues { (_, it) -> it.toSet() }

fun Iterable<Vec2>.mean(): Vec2 {
	if (none()) return Vec2(0.0, 0.0)
	
	val mx = sumOf { it.x }
	val my = sumOf { it.y }
	return Vec2(mx, my) / count().toDouble()
}

fun Double.signedPow(x: Double) = if (absoluteValue < EPSILON) 0.0 else sign * absoluteValue.pow(x)
