package starshipfights.data

import kotlinx.coroutines.*
import org.litote.kmongo.div
import org.litote.kmongo.inc
import org.litote.kmongo.lt
import org.litote.kmongo.setValue
import starshipfights.data.admiralty.*
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession
import starshipfights.game.AdmiralRank
import starshipfights.sfLogger
import java.time.Instant
import java.time.ZoneId

object DataRoutines {
	private val scope: CoroutineScope = CoroutineScope(
		SupervisorJob() + CoroutineExceptionHandler { ctx, ex ->
			val coroutine = ctx[CoroutineName]?.name?.let { "coroutine $it" } ?: "unnamed coroutine"
			sfLogger.error("Caught unhandled exception in $coroutine", ex)
		}
	)
	
	fun initializeRoutines(): Job {
		// Initialize tables
		Admiral.initialize()
		BattleRecord.initialize()
		ShipInDrydock.initialize()
		User.initialize()
		UserSession.initialize()
		
		return scope.launch {
			// Repair ships
			launch {
				while (currentCoroutineContext().isActive) {
					launch {
						val now = Instant.now()
						ShipInDrydock.update(ShipInDrydock::status / DrydockStatus.InRepair::until lt now, setValue(ShipInDrydock::status, DrydockStatus.Ready))
					}
					delay(300_000)
				}
			}
			
			// Pay admirals
			launch {
				var prevTime = Instant.now().atZone(ZoneId.systemDefault())
				while (currentCoroutineContext().isActive) {
					val currTime = Instant.now().atZone(ZoneId.systemDefault())
					if (currTime.dayOfWeek != prevTime.dayOfWeek)
						launch {
							AdmiralRank.values().forEach { rank ->
								launch {
									Admiral.update(
										AdmiralRank eq rank,
										inc(Admiral::money, rank.dailyWage)
									)
								}
							}
						}
					prevTime = currTime
					delay(900_000)
				}
			}
		}
	}
}
