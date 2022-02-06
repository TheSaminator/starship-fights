package starshipfights.game

import kotlinx.serialization.Serializable
import kotlin.js.Date

@Serializable(with = MomentSerializer::class)
actual class Moment(val date: Date) {
	actual constructor(millis: Double) : this(Date(millis))
	
	actual fun toMillis(): Double {
		return date.getTime()
	}
	
	actual companion object {
		actual val now: Moment
			get() = Moment(Date())
	}
}
