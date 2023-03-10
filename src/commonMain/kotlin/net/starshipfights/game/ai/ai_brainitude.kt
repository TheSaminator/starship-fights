package net.starshipfights.game.ai

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import net.starshipfights.data.Id
import net.starshipfights.game.ShipInstance
import net.starshipfights.game.jsonSerializer
import kotlin.jvm.JvmInline
import kotlin.properties.ReadOnlyProperty

@JvmInline
@Serializable
value class Instincts private constructor(private val numbers: MutableMap<String, Double>) {
	constructor() : this(mutableMapOf())
	
	operator fun get(instinct: Instinct) = numbers.getOrPut(instinct.key) { instinct.randRange.random() }
	
	companion object {
		fun fromValues(values: Map<String, Double>) = Instincts(values.toMutableMap())
	}
}

data class Instinct(val key: String, val randRange: ClosedFloatingPointRange<Double>)

fun instinct(randRange: ClosedFloatingPointRange<Double>) = ReadOnlyProperty<Any?, Instinct> { _, property ->
	Instinct(property.name, randRange)
}

@JvmInline
@Serializable
value class Brain private constructor(private val data: MutableMap<String, JsonElement>) {
	constructor() : this(mutableMapOf())
	
	operator fun <T> get(neuron: Neuron<T>) = jsonSerializer.decodeFromJsonElement(
		neuron.codec,
		data.getOrPut(neuron.key) {
			jsonSerializer.encodeToJsonElement(
				neuron.codec,
				neuron.default()
			)
		}
	)
	
	operator fun <T> set(neuron: Neuron<T>, value: T) = data.set(
		neuron.key,
		jsonSerializer.encodeToJsonElement(
			neuron.codec,
			value
		)
	)
}

data class Neuron<T>(val key: String, val codec: KSerializer<T>, val default: () -> T)

fun <T> neuron(codec: KSerializer<T>, default: () -> T) = ReadOnlyProperty<Any?, Neuron<T>> { _, property ->
	Neuron(property.name, codec, default)
}

infix fun <T> Neuron<T>.forShip(ship: Id<ShipInstance>) = copy(key = "$key[$ship]")
