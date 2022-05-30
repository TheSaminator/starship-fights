package starshipfights.game

import kotlinx.serialization.Serializable
import kotlin.js.Date

@Serializable(with = MomentSerializer::class)
actual class Moment(val date: Date) : Comparable<Moment> {
	actual constructor(millis: Double) : this(Date(millis))
	
	actual fun toMillis(): Double {
		return date.getTime()
	}
	
	actual override fun compareTo(other: Moment) = toMillis().compareTo(other.toMillis())
	
	actual companion object {
		actual val now: Moment
			get() = Moment(Date())
	}
}
