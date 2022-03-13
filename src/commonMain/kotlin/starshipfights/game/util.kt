package starshipfights.game

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

val jsonSerializer = Json {
	classDiscriminator = "\$ktClass"
	coerceInputValues = true
	encodeDefaults = false
	ignoreUnknownKeys = true
	useAlternativeNames = false
}

const val EPSILON = 0.00_001

fun <T : Enum<T>> T.toUrlSlug() = name.replace('_', '-').lowercase()

inline fun <T> pollFlow(intervalMs: Long = 50, crossinline poll: () -> T) = flow {
	var prev = poll()
	emit(prev)
	
	while (currentCoroutineContext().isActive) {
		delay(intervalMs)
		val curr = poll()
		if (curr != prev) {
			prev = curr
			emit(prev)
		}
	}
}

operator fun <T> List<T>.times(multiplier: Int): List<T> = if (multiplier <= 0) emptyList()
else if (multiplier == 1) this
else this + (this * (multiplier - 1))

fun Double.toPercent() = "${(this * 100).roundToInt()}%"

fun smoothMinus1To1(x: Double, exponent: Double = 1.0) = x / (1 + abs(x).pow(exponent)).pow(1 / exponent)
fun smoothNegative(x: Double) = if (x < 0) exp(x) else x + 1

fun <T> Iterable<T>.joinToDisplayString(oxfordComma: Boolean = true, transform: (T) -> String = { it.toString() }): String = when (val size = count()) {
	0 -> ""
	1 -> transform(single())
	2 -> "${transform(first())} and ${transform(last())}"
	else -> "${take(size - 1).joinToString { transform(it) }}${if (oxfordComma) "," else ""} and ${transform(last())}"
}
