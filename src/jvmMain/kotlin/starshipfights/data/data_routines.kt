package starshipfights.data

import kotlinx.coroutines.*
import org.litote.kmongo.inc
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import starshipfights.data.admiralty.Admiral
import starshipfights.data.admiralty.BattleRecord
import starshipfights.data.admiralty.ShipInDrydock
import starshipfights.data.admiralty.eq
import starshipfights.data.auth.User
import starshipfights.data.auth.UserSession
import starshipfights.game.AdmiralRank
import java.time.Instant
import java.time.ZoneId

object DataRoutines {
	private val logger: Logger = LoggerFactory.getLogger(javaClass)
	
	private val scope: CoroutineScope = CoroutineScope(
		SupervisorJob() + CoroutineExceptionHandler { ctx, ex ->
			val coroutine = ctx[CoroutineName]?.name?.let { "coroutine $it" } ?: "unnamed coroutine"
			logger.error("Caught unhandled exception in $coroutine", ex)
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
			// Pay admirals
			launch {
				var prevTime = Instant.now().atZone(ZoneId.systemDefault())
				while (currentCoroutineContext().isActive) {
					val currTime = Instant.now().atZone(ZoneId.systemDefault())
					if (currTime.dayOfWeek != prevTime.dayOfWeek)
						launch {
							logger.info("Paying admirals now")
							for (rank in AdmiralRank.values())
								launch {
									Admiral.update(
										AdmiralRank eq rank,
										inc(Admiral::money, rank.dailyWage)
									)
								}
						}
					prevTime = currTime
					delay(900_000)
				}
			}
		}
	}
}
