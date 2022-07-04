package net.starshipfights.data.space

import net.starshipfights.game.FactionFlavor

fun Int.toOrdinal(): String {
	return if ((this / 10) % 10 == 1)
		"${this}th"
	else when (this % 10) {
		1 -> "${this}st"
		2 -> "${this}nd"
		3 -> "${this}rd"
		else -> "${this}th"
	}
}

private fun rangedOrdinal(max: Int, min: Int = 1) = (min..max).random().toOrdinal()
private fun rangedRomanNumeral(max: Int, min: Int = 1) = (min..max).random().toRomanNumerals()

fun FactionFlavor.genFleetName(): String = when (this) {
	FactionFlavor.MECHYRDIA -> "${(100..999).random()}il Expediciós Flót"
	FactionFlavor.TYLA -> "${(100..399).random()}a Kasaklas Safasra"
	FactionFlavor.OLYMPIA -> "${rangedRomanNumeral(399, 50)} Classis Belligerens Astronautica"
	FactionFlavor.TEXANDRIA -> "${(50..199).random()}te Wehrsflotte"
	FactionFlavor.NDRC -> "${(100..699).random()}e Sterrenvloot"
	FactionFlavor.CCC -> "${rangedRomanNumeral(299, 50)} Classis Comitans"
	FactionFlavor.MJOLNIR_ENERGY -> "${(100..499).random()}a Stjarnnafloti"
	FactionFlavor.MASRA_DRAETSEN -> "${rangedOrdinal(4999, 500)} ${listOf("Conquest", "War", "Punishment").random()} Armada"
	FactionFlavor.AEDON_CULTISTS -> "${rangedOrdinal(1999, 200)} ${listOf("Defilement", "Despoilation", "Desolation").random()} Armada"
	FactionFlavor.FERTHLON_EXILES -> "${rangedOrdinal(399, 50)} Revolutionary Remnant Fleet"
	FactionFlavor.RES_NOSTRA -> "${rangedRomanNumeral(299, 10)} Classis Nostra"
	FactionFlavor.CORSAIRS -> "${rangedRomanNumeral(299, 10)} Nauticum Piratarum"
	FactionFlavor.FELINAE_FELICES -> "${rangedRomanNumeral(299, 10)} Grex Felinarum"
	FactionFlavor.ISARNAREYKK -> "${rangedOrdinal(2999, 300)} Reykksflott"
	FactionFlavor.SWARTAREYKK -> "${rangedOrdinal(1999, 200)} Reykksflott"
	FactionFlavor.THEUDAREYKK -> "${rangedOrdinal(1999, 200)} Reyalis Flott"
	FactionFlavor.STAHLAREYKK -> "${rangedOrdinal(1999, 200)} Fulksflott"
	FactionFlavor.LYUDAREYKK -> "${rangedOrdinal(1999, 200)} Vaerflott"
	FactionFlavor.NEUIA_FULKREYKK -> "${rangedOrdinal(499, 20)} Fulkreyksk Kriygsflott"
	FactionFlavor.CORVUS_CLUSTER_VESTIGIUM -> "${rangedOrdinal(499, 20)} Expeditionary Fleet"
	FactionFlavor.COLEMAN_SF_BASE_VESTIGIUM -> "${rangedOrdinal(499, 20)} Exploratory Fleet"
}
