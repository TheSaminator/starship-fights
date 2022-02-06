package starshipfights.data

import kotlinx.coroutines.*
import org.litote.kmongo.lte
import starshipfights.data.auth.UserSession
import starshipfights.sfLogger
import kotlin.coroutines.CoroutineContext

object DataRoutines : CoroutineScope {
	override val coroutineContext: CoroutineContext = SupervisorJob() + CoroutineExceptionHandler { ctx, ex ->
		val coroutine = ctx[CoroutineName]?.name?.let { "coroutine $it" } ?: "unnamed coroutine"
		sfLogger.error("Caught unhandled exception in $coroutine", ex)
	}
	
	fun initializeRoutines() = launch {
		launch {
			while (currentCoroutineContext().isActive) {
				launch {
					UserSession.remove(UserSession::expirationMillis lte System.currentTimeMillis())
				}
				delay(3600_000)
			}
		}
	}
}
