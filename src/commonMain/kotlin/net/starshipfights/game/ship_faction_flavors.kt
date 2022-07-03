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

val IntColor.shadow: IntColor
	get() = let { (r, g, b) ->
		IntColor(
			r * 2 / 3,
			g * 2 / 3,
			b * 2 / 3,
		)
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

enum class FactionFlavor(val displayName: String, val colorReplacement: IntColor) {
	MECHYRDIA("Štelflót Ciarstuos Mehurdiasi", IntColor(255, 204, 51)),
	TYLA("Helasram Kasashtam Moashtas Tulasras", IntColor(51, 102, 204)),
	OLYMPIA("Classis Nautica Rei Publicae Olympicae", IntColor(204, 51, 51)),
	TEXANDRIA("Texandrische Sternenmarine der Volkswehr", IntColor(255, 221, 119)),
	
	NDRC("Sterrenvloot der NdRC", IntColor(255, 153, 51)),
	CCC("Collegium Comitatum Caeleste", IntColor(238, 187, 34)),
	MJOLNIR_ENERGY("Mjölnir Energy", IntColor(34, 68, 136)),
	
	MASRA_DRAETSEN("Diadochus Masra Draetsen", IntColor(34, 85, 170)),
	AEDON_CULTISTS("Aedonolatrous Cultists", IntColor(136, 68, 204)),
	FERTHLON_EXILES("Ferthlon Internation Exiles", IntColor(51, 204, 68)),
	
	RES_NOSTRA("Res Nostra", IntColor(153, 17, 85)),
	CORSAIRS("Corsairs' Commune", IntColor(34, 34, 34)),
	FELINAE_FELICES("Felinae Felices", IntColor(255, 119, 187)),
	
	ISARNAREYKK("Isarnareyksk Federation", IntColor(255, 255, 255)),
	SWARTAREYKK("Swartareyksk Totalitariat", IntColor(255, 170, 170)),
	THEUDAREYKK("Theudareyksk Kingdom", IntColor(153, 204, 255)),
	STAHLAREYKK("Stahlareyksk Binding", IntColor(204, 153, 102)),
	LYUDAREYKK("Lyudareyksk Baurginassus", IntColor(153, 204, 153)),
	NEUIA_FULKREYKK("Neuia Fulkreykk Rebellion", IntColor(153, 153, 153)),
	
	CORVUS_CLUSTER_VESTIGIUM("Vestigium Sect in the Corvus Cluster", IntColor(108, 96, 153)),
	COLEMAN_SF_BASE_VESTIGIUM("Vestigium Sect at Coleman Space Force Base", IntColor(153, 102, 102)),
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
			Faction.MECHYRDIA -> setOf(MECHYRDIA, TYLA, OLYMPIA, TEXANDRIA, NDRC, RES_NOSTRA, CORSAIRS, FERTHLON_EXILES)
			Faction.NDRC -> setOf(NDRC, CCC, MJOLNIR_ENERGY, RES_NOSTRA, CORSAIRS, FERTHLON_EXILES)
			Faction.MASRA_DRAETSEN -> setOf(MASRA_DRAETSEN, AEDON_CULTISTS, RES_NOSTRA, CORSAIRS, FERTHLON_EXILES)
			Faction.FELINAE_FELICES -> setOf(FELINAE_FELICES, RES_NOSTRA, CORSAIRS)
			Faction.ISARNAREYKK -> setOf(ISARNAREYKK, SWARTAREYKK, THEUDAREYKK, STAHLAREYKK, LYUDAREYKK, NEUIA_FULKREYKK)
			Faction.VESTIGIUM -> setOf(CORVUS_CLUSTER_VESTIGIUM, COLEMAN_SF_BASE_VESTIGIUM)
		}
	}
}

val FactionFlavor.flagUrl: String
	get() = if (name.endsWith("VESTIGIUM"))
		"/static/images/flag/vestigium.svg"
	else
		"/static/images/flag/${toUrlSlug()}.svg"
