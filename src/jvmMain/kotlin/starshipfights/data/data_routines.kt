package starshipfights.data

import kotlinx.coroutines.*
import org.litote.kmongo.div
import org.litote.kmongo.lt
import org.litote.kmongo.setValue
import starshipfights.data.admiralty.Admiral
import starshipfights.data.admiralty.BattleRecord
import starshipfights.data.admiralty.DrydockStatus
import starshipfights.data.admiralty.ShipInDrydock
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession
import starshipfights.sfLogger
import java.time.Instant
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
			// Repair ships
			launch {
				while (currentCoroutineContext().isActive) {
					val now = Instant.now()
					launch {
						ShipInDrydock.update(ShipInDrydock::status / DrydockStatus.InRepair::until lt now, setValue(ShipInDrydock::status, DrydockStatus.Ready))
					}
					delay(300_000)
				}
			}
		}
	}
}
