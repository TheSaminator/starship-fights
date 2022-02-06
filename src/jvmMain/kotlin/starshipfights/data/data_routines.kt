package starshipfights.data

import kotlinx.coroutines.*
import org.litote.kmongo.lte
import starshipfights.data.admiralty.Admiral
import starshipfights.data.admiralty.BattleRecord
import starshipfights.data.admiralty.ShipInDrydock
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession
import starshipfights.sfLogger
import kotlin.coroutines.CoroutineContext

object DataRoutines : CoroutineScope {
	override val coroutineContext: CoroutineContext = SupervisorJob() + CoroutineExceptionHandler { ctx, ex ->
		val coroutine = ctx[CoroutineName]?.name?.let { "coroutine $it" } ?: "unnamed coroutine"
		sfLogger.error("Caught unhandled exception in $coroutine", ex)
	}
	
	fun initializeRoutines(): Job {
		// Initialize tables
		Admiral.initialize()
		BattleRecord.initialize()
		ShipInDrydock.initialize()
		User.initialize()
		UserSession.initialize()
		
		return launch {
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
}
