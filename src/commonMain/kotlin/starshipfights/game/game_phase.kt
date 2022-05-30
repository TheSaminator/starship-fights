package starshipfights.game

import kotlinx.serialization.Serializable

@Serializable
sealed class GamePhase {
	abstract val turn: Int
	abstract fun next(): GamePhase
	
	@Serializable
	object Deploy : GamePhase() {
		override val turn: Int
			get() = 0
		
		override fun next() = Power(turn + 1)
	}
	
	@Serializable
	data class Power(override val turn: Int) : GamePhase() {
		override fun next() = Move(turn)
	}
	
	@Serializable
	data class Move(override val turn: Int) : GamePhase() {
		override fun next() = Attack(turn)
	}
	
	@Serializable
	data class Attack(override val turn: Int) : GamePhase() {
		override fun next() = Repair(turn)
	}
	
	@Serializable
	data class Repair(override val turn: Int) : GamePhase() {
		override fun next() = Power(turn + 1)
	}
}

val GamePhase.usesInitiative: Boolean
	get() = this is GamePhase.Move || this is GamePhase.Attack
