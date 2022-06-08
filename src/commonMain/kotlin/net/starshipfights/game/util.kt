package net.starshipfights.game

import kotlinx.html.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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

class MapAsListSerializer<K, V>(keySerializer: KSerializer<K>, valueSerializer: KSerializer<V>) : KSerializer<Map<K, V>> {
	private val inner = ListSerializer(PairSerializer(keySerializer, valueSerializer))
	
	override val descriptor: SerialDescriptor
		get() = inner.descriptor
	
	override fun serialize(encoder: Encoder, value: Map<K, V>) {
		inner.serialize(encoder, value.toList())
	}
	
	override fun deserialize(decoder: Decoder): Map<K, V> {
		return inner.deserialize(decoder).toMap()
	}
}

const val EPSILON = 0.00_001

fun <T : Enum<T>> T.toUrlSlug() = name.replace('_', '-').lowercase()

fun Double.toPercent() = "${(this * 100).roundToInt()}%"

fun smoothMinus1To1(x: Double, exponent: Double = 1.0) = x / (1 + abs(x).pow(exponent)).pow(1 / exponent)
fun smoothNegative(x: Double) = if (x < 0) exp(x) else x + 1

fun <T> Iterable<T>.joinToDisplayString(oxfordComma: Boolean = true, transform: (T) -> String = { it.toString() }): String = when (val size = count()) {
	0 -> ""
	1 -> transform(single())
	2 -> "${transform(first())} and ${transform(last())}"
	else -> "${take(size - 1).joinToString { transform(it) }}${if (oxfordComma) "," else ""} and ${transform(last())}"
}

inline fun <T : FlowOrPhrasingContent> T.foreign(language: String, crossinline block: SPAN.() -> Unit) = span {
	lang = language
	style = "font-style: italic"
	block()
}
