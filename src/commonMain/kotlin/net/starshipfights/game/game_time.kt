package net.starshipfights.game

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MomentSerializer::class)
expect class Moment(millis: Double) : Comparable<Moment> {
	fun toMillis(): Double
	
	override fun compareTo(other: Moment): Int
	
	companion object {
		val now: Moment
	}
}

object MomentSerializer : KSerializer<Moment> {
	private val inner = Double.serializer()
	
	override val descriptor: SerialDescriptor
		get() = inner.descriptor
	
	override fun serialize(encoder: Encoder, value: Moment) {
		inner.serialize(encoder, value.toMillis())
	}
	
	override fun deserialize(decoder: Decoder): Moment {
		return Moment(inner.deserialize(decoder))
	}
}
