package net.starshipfights.campaign

import kotlinx.serialization.Serializable
import net.starshipfights.game.Faction
import net.starshipfights.game.FactionFlavor
import kotlin.jvm.JvmInline
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

enum class ClusterSize(val maxStars: Int, val maxHyperlaneDistanceFactor: Double) {
	SMALL(20, 1.5), MEDIUM(35, 2.0), LARGE(50, 2.5);
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class ClusterLaneDensity(val chanceToRemove: Double) {
	SPARSE(0.72), MEDIUM(0.36), DENSE(0.12);
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class ClusterPlanetDensity(val chanceToAdd: Double) {
	FEWER(0.06), NORMAL(0.16), MORE(0.36);
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class ClusterCorruption(val corruptedStarsPortion: Double) {
	SACROSANCT(0.075), MATERIAL(0.15), INFERNAL(0.3);
	
	fun getNumCorruptedStars(size: ClusterSize): Int {
		val corruptedStars = corruptedStarsPortion * size.maxStars
		val min = floor(corruptedStars).roundToInt()
		val max = ceil(corruptedStars).roundToInt()
		
		val chance = corruptedStars - min
		
		return if (Random.nextDouble() < chance)
			max
		else
			min
	}
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class ClusterFactionMode {
	ALLOW, REQUIRE, EXCLUDE;
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

@JvmInline
@Serializable
value class ClusterFactions private constructor(private val factions: Map<FactionFlavor, ClusterFactionMode>) {
	init {
		require(FactionFlavor.values().any { factions[it] != ClusterFactionMode.EXCLUDE }) { "Must not exclude all factions when creating star cluster" }
	}
	
	fun getModesForFaction(faction: Faction) = faction.allegiences.map { this[it] }.toSet()
	
	fun canFitInto(size: ClusterSize) = factions.count { (_, mode) -> mode == ClusterFactionMode.REQUIRE } < size.maxStars * 2 / 5
	
	fun <T : Comparable<T>> forceFitInto(size: ClusterSize, precedence: (FactionFlavor) -> T): ClusterFactions {
		val canDemote = factions.filterValues { it == ClusterFactionMode.REQUIRE }.keys.sortedBy(precedence)
		val mustDemote = (canDemote.size - size.maxStars * 2 / 5).coerceAtLeast(0)
		return this + canDemote.take(mustDemote).associateWith { ClusterFactionMode.ALLOW }
	}
	
	operator fun get(factionFlavor: FactionFlavor) = factions[factionFlavor] ?: ClusterFactionMode.ALLOW
	
	operator fun plus(other: ClusterFactions) = ClusterFactions(factions + other.factions)
	private operator fun plus(otherFactions: Map<FactionFlavor, ClusterFactionMode>) = ClusterFactions(factions + otherFactions)
	
	fun asGenerationSequence() = sequence {
		val required = FactionFlavor.values().filter { this@ClusterFactions[it] == ClusterFactionMode.REQUIRE }.toSet()
		val included = FactionFlavor.values().filter { this@ClusterFactions[it] != ClusterFactionMode.EXCLUDE }.toSet()
		
		// first, start with the required flavors
		yieldAll(required.shuffled())
		while (true) {
			// continue with the included flavors
			yieldAll(included.shuffled())
		}
	}
	
	fun getRelatedFaction(faction: FactionFlavor) = factions
		.filterKeys { it.loyalties == faction.loyalties }
		.filterValues { it != ClusterFactionMode.EXCLUDE }
		.keys.random()
	
	companion object {
		val Default: ClusterFactions
			get() = ClusterFactions(FactionFlavor.values().associateWith { ClusterFactionMode.ALLOW })
		
		fun of(factions: Map<FactionFlavor, ClusterFactionMode>) = Default + factions
	}
}

enum class ClusterContention(val numLanesSpreadControl: Double, val maxFleets: Int, val fleetStrengthMult: Double) {
	BLOODBATH(3.0, 5, 1.0), CONTESTED(1.75, 3, 0.8), PEACEFUL(1.25, 2, 0.5);
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

@Serializable
data class ClusterGenerationSettings(
	val background: StarClusterBackground,
	val size: ClusterSize,
	val laneDensity: ClusterLaneDensity,
	val planetDensity: ClusterPlanetDensity,
	val corruption: ClusterCorruption,
	val factions: ClusterFactions,
	val contention: ClusterContention
)
