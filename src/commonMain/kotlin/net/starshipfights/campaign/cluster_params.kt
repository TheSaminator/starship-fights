package net.starshipfights.campaign

import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

enum class ClusterSize(val maxStars: Int, val maxHyperlaneDistanceFactor: Double) {
	SMALL(15, 1.5), MEDIUM(25, 2.0), LARGE(35, 2.5);
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class ClusterLaneDensity(val chanceToRemove: Double, val numToAdd: Int) {
	SPARSE(0.72, 1), MEDIUM(0.36, 2), DENSE(0.12, 3);
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class ClusterPlanetDensity(val chanceToAdd: Double) {
	FEWER(0.06), NORMAL(0.16), MORE(0.36);
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class ClusterCorruption(val corruptedStarsPortion: Double) {
	SACROSANCT(0.05), MATERIAL(0.15), INFERNAL(0.25);
	
	fun getNumCorruptedStars(size: ClusterSize): Int {
		val min = floor(corruptedStarsPortion * size.maxStars).roundToInt()
		val max = ceil(corruptedStarsPortion * size.maxStars).roundToInt()
		
		return (min..max).random()
	}
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

@Serializable
data class ClusterGenerationSettings(
	val background: StarClusterBackground,
	val size: ClusterSize,
	val laneDensity: ClusterLaneDensity,
	val planetDensity: ClusterPlanetDensity,
	val corruption: ClusterCorruption
)
