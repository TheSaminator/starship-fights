package net.starshipfights.campaign

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id
import net.starshipfights.game.FactionFlavor
import net.starshipfights.game.Position

enum class StarClusterBackground {
	BLUE, GOLD, GRAY, GREEN, ORANGE, PINK, PURPLE, RED;
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

@Serializable
data class StarClusterView(
	val background: StarClusterBackground,
	val systems: Map<Id<StarSystem>, StarSystem>,
	val lanes: Set<WarpLane>
)

@Serializable
data class StarSystem(
	val name: String,
	val holder: FactionFlavor?,
	val fleets: Map<Id<FleetPresence>, FleetPresence>,
	
	val position: Position,
	val radius: Double,
	val bodies: Map<Id<CelestialObject>, CelestialObject>
)

@Serializable
data class WarpLane(
	val systemA: Id<StarSystem>,
	val systemB: Id<StarSystem>,
) {
	val equivalent: WarpLane
		get() = let { (a, b) -> WarpLane(b, a) }
	
	operator fun contains(test: Id<StarSystem>) = test == systemA || test == systemB
	
	private val asPair: Pair<Id<StarSystem>, Id<StarSystem>>
		get() = systemA to systemB
	
	override fun equals(other: Any?): Boolean {
		return other is WarpLane && (asPair == other.asPair || equivalent.asPair == other.asPair)
	}
	
	override fun hashCode(): Int {
		val result = 31 * systemA.hashCode() + systemB.hashCode()
		val reverse = 31 * systemB.hashCode() + systemA.hashCode()
		return result xor reverse
	}
}
