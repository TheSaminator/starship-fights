package net.starshipfights.game

import kotlinx.serialization.Serializable

@Serializable
data class IntColor(val red: Int, val green: Int, val blue: Int) {
	init {
		require(red in 0..255) { "Invalid RGB value: red = $red" }
		require(green in 0..255) { "Invalid RGB value: green = $green" }
		require(blue in 0..255) { "Invalid RGB value: blue = $blue" }
	}
	
	override fun toString(): String {
		val redHex = red.toString(16).padStart(2, '0')
		val greenHex = green.toString(16).padStart(2, '0')
		val blueHex = blue.toString(16).padStart(2, '0')
		
		return "#$redHex$greenHex$blueHex"
	}
}

val IntColor.highlight: IntColor
	get() = let { (r, g, b) ->
		IntColor(
			255 - (255 - r) * 2 / 3,
			255 - (255 - g) * 2 / 3,
			255 - (255 - b) * 2 / 3,
		)
	}

val Faction.trimColor: IntColor?
	get() = when (this) {
		Faction.MECHYRDIA -> IntColor(255, 204, 51)
		Faction.NDRC -> IntColor(255, 153, 51)
		Faction.MASRA_DRAETSEN -> IntColor(34, 85, 170)
		Faction.FELINAE_FELICES -> IntColor(255, 119, 187)
		Faction.ISARNAREYKK -> null
		Faction.VESTIGIUM -> IntColor(108, 96, 153)
	}

enum class FactionFlavor(val nativeName: String?, val translatedName: String, val colorReplacement: IntColor) {
	MECHYRDIA("Štelflót Ciarstuos Mehurdiasi", "Imperial Star Fleet of Mechyrdia", IntColor(255, 204, 51)),
	TYLA("Helasram Laevashtam Moashtas Tulasras", "Stellar Navy of the Tylan Republic", IntColor(51, 102, 204)),
	OLYMPIA("Classis Nautica Rei Publicae Olympicae", "Naval Fleet of the Olympia Commonwealth", IntColor(204, 51, 51)),
	TEXANDRIA("Texandrische Sternenmarine der Volkswehr", "Texandrian Star Navy of the Public Defense", IntColor(255, 221, 119)),
	
	NDRC("Sterrenvloot der NdRC", "NdRC Stellar Fleet", IntColor(255, 153, 51)),
	CCC("Collegium Comitatum Caeleste", "Celestial Caravan Company", IntColor(255, 204, 51)),
	MJOLNIR_ENERGY("Mjolniri Energia", "Mjölnir Energy", IntColor(34, 68, 136)),
	
	MASRA_DRAETSEN(null, "Diadochus Masra Draetsen", IntColor(34, 85, 170)),
	AEDON_CULTISTS(null, "Aedon Cultists", IntColor(136, 68, 204)),
	FERTHLON_EXILES(null, "Ferthlon Exiles", IntColor(51, 204, 68)),
	
	RES_NOSTRA(null, "Res Nostra", IntColor(153, 17, 85)),
	CORSAIRS(null, "Corsairs' Commune", IntColor(34, 34, 34)),
	FELINAE_FELICES(null, "Felinae Felices", IntColor(255, 119, 187)),
	
	ISARNAREYKK(null, "Isarnareyksk Federation", IntColor(255, 255, 255)),
	SWARTAREYKK(null, "Swartareyksk Totalitariat", IntColor(255, 170, 170)),
	THEUDAREYKK(null, "Theudareyksk Kingdom", IntColor(153, 204, 255)),
	STAHLAREYKK(null, "Stahlareyksk Binding", IntColor(204, 153, 102)),
	LYUDAREYKK(null, "Lyudareyksk Baurginassus", IntColor(153, 204, 153)),
	NEUIA_FULKREYKK(null, "Neuia Fulkreykk Rebellion", IntColor(153, 153, 153)),
	
	CORVUS_CLUSTER_VESTIGIUM(null, "Vestigium Sect in the Corvus Cluster", IntColor(108, 96, 153)),
	COLEMAN_SF_BASE_VESTIGIUM(null, "Vestigium Sect at Coleman Space Force Base", IntColor(153, 102, 102)),
	;
	
	companion object {
		fun defaultForFaction(playerFaction: Faction): FactionFlavor = when (playerFaction) {
			Faction.MECHYRDIA -> MECHYRDIA
			Faction.NDRC -> NDRC
			Faction.MASRA_DRAETSEN -> MASRA_DRAETSEN
			Faction.FELINAE_FELICES -> FELINAE_FELICES
			Faction.ISARNAREYKK -> ISARNAREYKK
			Faction.VESTIGIUM -> CORVUS_CLUSTER_VESTIGIUM
		}
		
		fun optionsForAiEnemy(computerFaction: Faction): Set<FactionFlavor> = when (computerFaction) {
			Faction.MECHYRDIA -> setOf(MECHYRDIA, TYLA, OLYMPIA, TEXANDRIA, NDRC, CORSAIRS, RES_NOSTRA)
			Faction.NDRC -> setOf(NDRC, CCC, MJOLNIR_ENERGY, RES_NOSTRA, CORSAIRS)
			Faction.MASRA_DRAETSEN -> setOf(MASRA_DRAETSEN, AEDON_CULTISTS, FERTHLON_EXILES)
			Faction.FELINAE_FELICES -> setOf(FELINAE_FELICES, RES_NOSTRA, CORSAIRS)
			Faction.ISARNAREYKK -> setOf(ISARNAREYKK, SWARTAREYKK, THEUDAREYKK, STAHLAREYKK, LYUDAREYKK, NEUIA_FULKREYKK)
			Faction.VESTIGIUM -> setOf(CORVUS_CLUSTER_VESTIGIUM, COLEMAN_SF_BASE_VESTIGIUM)
		}
	}
}
