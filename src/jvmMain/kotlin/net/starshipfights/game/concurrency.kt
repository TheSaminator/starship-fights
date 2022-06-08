package net.starshipfights.game

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ConcurrentCurator<T>(private val curated: T) {
	private val mutex = Mutex()
	
	suspend fun <U> use(block: suspend (T) -> U) = mutex.withLock { block(curated) }
}
