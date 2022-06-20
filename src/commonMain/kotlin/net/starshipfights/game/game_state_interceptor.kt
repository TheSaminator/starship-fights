package net.starshipfights.game

fun interface GameStateInterceptor {
	fun onStateChange(previous: GameState, current: GameState): GameState
}

object NoopInterceptor : GameStateInterceptor {
	override fun onStateChange(previous: GameState, current: GameState) = current
}
