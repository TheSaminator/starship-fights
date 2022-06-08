package net.starshipfights.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(with = IdSerializer::class)
value class Id<@Suppress("unused") T>(val id: String) {
	override fun toString() = id
	
	fun <U> reinterpret() = Id<U>(id)
	
	companion object {
		fun serializer(): KSerializer<Id<*>> = IdSerializer
	}
}

object IdSerializer : KSerializer<Id<*>> {
	private val inner = String.serializer()
	
	override val descriptor: SerialDescriptor
		get() = inner.descriptor
	
	override fun serialize(encoder: Encoder, value: Id<*>) {
		inner.serialize(encoder, value.id)
	}
	
	override fun deserialize(decoder: Decoder): Id<*> {
		return Id<Any>(inner.deserialize(decoder))
	}
}
