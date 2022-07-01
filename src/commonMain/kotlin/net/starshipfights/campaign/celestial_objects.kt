package net.starshipfights.campaign

import kotlinx.serialization.Serializable
import net.starshipfights.game.IntColor
import net.starshipfights.game.Position

@Serializable
sealed class CelestialObject {
	abstract val name: String
	abstract val position: Position
	abstract val size: Int
	abstract val rotationSpeed: Double
	
	@Serializable
	data class Star(
		override val name: String,
		override val position: Position,
		override val size: Int,
		override val rotationSpeed: Double,
		val type: StarType
	) : CelestialObject()
	
	@Serializable
	data class Planet(
		override val name: String,
		override val position: Position,
		override val size: Int,
		override val rotationSpeed: Double,
		val type: PlanetType
	) : CelestialObject()
}

enum class StarType(val displayName: String) {
	Y("Y-class Brown Dwarf"),
	T("T-class Brown Dwarf"),
	L("L-class Brown Dwarf"),
	M("M-class Star"),
	K("K-class Star"),
	G("G-class Star"),
	F("F-class Star"),
	A("A-class Star"),
	B("B-class Star"),
	O("O-class Star"),
	WHITE_DWARF("White Dwarf"),
	NEUTRON_STAR("Neutron Star"),
	BLACK_HOLE("Black Hole"),
	X("Veiled Star"),
	;
	
	val lightColor: IntColor
		get() = when (this) {
			Y -> IntColor(153, 85, 221)
			T -> IntColor(255, 51, 153)
			L -> IntColor(255, 102, 102)
			M -> IntColor(255, 218, 181)
			K -> IntColor(255, 102, 102)
			G -> IntColor(255, 237, 227)
			F -> IntColor(249, 245, 255)
			A -> IntColor(213, 224, 255)
			B -> IntColor(162, 192, 255)
			O -> IntColor(146, 191, 255)
			WHITE_DWARF -> IntColor(102, 102, 102)
			NEUTRON_STAR -> IntColor(34, 68, 136)
			BLACK_HOLE -> IntColor(153, 153, 153)
			X -> IntColor(181, 127, 200)
		}
	
	val sizeRange: IntRange
		get() = when (this) {
			Y -> 5..10
			T -> 6..11
			L -> 7..12
			M -> 8..30
			K -> 10..29
			G -> 12..28
			F -> 14..27
			A -> 16..26
			B -> 18..25
			O -> 20..24
			WHITE_DWARF -> 2..7
			NEUTRON_STAR -> 2..7
			BLACK_HOLE -> 2..7
			X -> 2..32
		}
	
	companion object {
		const val MAX_STAR_SIZE = 32
	}
}

val StarType.isEldritch: Boolean
	get() = this == StarType.X

enum class PlanetType(val displayName: String) {
	TERRESTRIAL("Habitable World"),
	BARREN("Barren World"),
	TOXIC("Toxic World"),
	DUSTY("Dusty World"),
	ICE_SHELL("Ice Shell World"),
	THOLIN("Tholin World"),
	GAS_GIANT("Gas Giant"),
	ICE_GIANT("Ice Giant"),
	VEILED_GIANT("Veiled Giant"),
	VEILED("Veiled World"),
	;
	
	val isGiant: Boolean
		get() = this in GAS_GIANT..VEILED_GIANT
	
	val sizeRange: IntRange
		get() = if (isGiant)
			5..8
		else
			1..4
}

val PlanetType.isEldritch: Boolean
	get() = this == PlanetType.VEILED_GIANT || this == PlanetType.VEILED
