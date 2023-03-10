package net.starshipfights.game

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable(with = MomentSerializer::class)
actual class Moment(val instant: Instant) : Comparable<Moment> {
	actual constructor(millis: Double) : this(
		Instant.ofEpochSecond(
			(millis / 1000.0).toLong(),
			((millis % 1000.0) * 1_000_000.0).toLong()
		)
	)
	
	actual fun toMillis(): Double {
		return (instant.epochSecond * 1000.0) + (instant.nano / 1_000_000.0)
	}
	
	actual override fun compareTo(other: Moment) = toMillis().compareTo(other.toMillis())
	
	actual companion object {
		actual val now: Moment
			get() = Moment(Instant.now())
	}
}
