package starshipfights.data.admiralty

enum class AdmiralNameFlavor {
	MECHYRDIA, TYLA, CALIBOR, // Mechyrdia-aligned
	DIADOCHI, // Masra Draetsen-aligned
	FULKREYKK, // Isarnareykk-aligned
	AMERICAN, HISPANIC_AMERICAN; // Vestigium-aligned
	
	val displayName: String
		get() = when (this) {
			MECHYRDIA -> "Mechyrdian"
			TYLA -> "Tylan"
			CALIBOR -> "Caliborese"
			DIADOCHI -> "Diadochi"
			FULKREYKK -> "Thedish"
			AMERICAN -> "American"
			HISPANIC_AMERICAN -> "Hispanic-American"
		}
}

object AdmiralNames {
	// PERSONAL NAME to PATRONYMIC
	private val mechyrdianMaleNames: List<Pair<String, String>> = listOf(
		"Marc" to "Marcówič",
		"Anton" to "Antonówič",
		"Bjarnarð" to "Bjarnarðówič",
		"Carl" to "Carlówič",
		"Þjutarix" to "Þjutarigówič",
		"Friðurix" to "Friþurigówič",
		"Iwan" to "Iwanówič",
		"Wladimer" to "Wladimerówič",
		"Giulius" to "Giuliówič",
		"Nicólei" to "Nicóleiówič",
		"Þjódor" to "Þjóderówič",
		"Sigismund" to "Sigismundówič",
		"Stefan" to "Stefanówič",
		"Wilhelm" to "Wilhelmówič",
		"Giórgj" to "Giórgiówič"
	)
	
	// PERSONAL NAME to MATRONYMIC
	private val mechyrdianFemaleNames: List<Pair<String, String>> = listOf(
		"Octavia" to "Octaviówca",
		"Annica" to "Annicówca",
		"Astrið" to "Astriðówca",
		"Caþarin" to "Caþarinówca",
		"Signi" to "Signówca",
		"Erica" to "Ericówca",
		"Fréja" to "Fréjówca",
		"Hilda" to "Hildówca",
		"Žanna" to "Žannówca",
		"Xenia" to "Xeniówca",
		"Carina" to "Carinówca",
		"Giadwiga" to "Giadwigówca",
		"Ženia" to "Ženiówca"
	)
	
	private val mechyrdianFamilyNames: List<Pair<String, String>> = listOf(
		"Alexandrów",
		"Antonów",
		"Pogdanów",
		"Hrusčjów",
		"Caísarów",
		"Carolów",
		"Sócolów",
		"Romanów",
		"Nemeciów",
		"Pjótrów",
		"Brutów",
		"Augustów",
		"Calašniców",
		"Anželów",
		"Sigmarów",
		"Dróganów",
		"Coroljów",
		"Wlasów"
	).map { it to "${it}a" }
	
	private fun randomMechyrdianName(isFemale: Boolean) = if (isFemale)
		mechyrdianFemaleNames.random().first + " " + mechyrdianFemaleNames.random().second + " " + mechyrdianFamilyNames.random().second
	else
		mechyrdianMaleNames.random().first + " " + mechyrdianMaleNames.random().second + " " + mechyrdianFamilyNames.random().first
	
	private val tylanMaleNames = listOf(
		"Althanar" to "Althanas",
		"Aurans" to "Aurantes",
		"Bochra" to "Bochranes",
		"Chshaejar" to "Chshaejas",
		"Hjofvachi" to "Hjovachines",
		"Koldimar" to "Koldimas",
		"Kor" to "Kores",
		"Ljomas" to "Ljomates",
		"Shajel" to "Shajel",
		"Shokar" to "Shokas",
		"Tolavajel" to "Tolavajel",
		"Voskar" to "Voskas",
	)
	
	private val tylanFemaleNames = listOf(
		"Althe" to "Althenes",
		"Anaseil" to "Anaseil",
		"Asetbur" to "Asetbus",
		"Atautha" to "Atauthas",
		"Aurantia" to "Aurantias",
		"Ilasheva" to "Ilashevas",
		"Kalora" to "Kaloras",
		"Kotolva" to "Kotolvas",
		"Psekna" to "Pseknas",
		"Shenera" to "Sheneras",
		"Reoka" to "Reokas",
		"Velga" to "Velgas",
	)
	
	private fun randomTylanName(isFemale: Boolean) = if (isFemale)
		tylanFemaleNames.random().first + " " + tylanFemaleNames.random().second + "-Nahra"
	else
		tylanMaleNames.random().first + " " + tylanMaleNames.random().second + "-Nensar"
	
	private val caliboreseNames = listOf(
		"Jathee",
		"Muly",
		"Simoh",
		"Laka",
		"Foryn",
		"Duxio",
		"Xirio",
		"Surmy",
		"Datarme",
		"Cloren",
		"Tared",
		"Quiliot",
		"Attiol",
		"Quarree",
		"Guil",
		"Miro",
		"Yryys",
		"Zarx",
		"Karm",
		"Mreek",
		"Dulyy",
		"Quorqui",
		"Dreminor",
		"Samitu",
		"Lurmak",
		"Quashi",
		"Barsyn",
		"Rymyo",
		"Soli",
		"Ickart",
		"Woom",
		"Qurquy",
		"Ymiro",
		"Rosiliq",
		"Xant",
		"Xateen",
		"Mssly",
		"Vixie",
		"Quelynn",
		"Plly",
		"Tessy",
		"Veekah",
		"Quett",
		"Xezeez",
		"Xyph",
		"Jixi",
		"Jeekie",
		"Meelen",
		"Rasah",
		"Reteeshy",
		"Xinchie",
		"Zae",
		"Ziggy",
		"Wurikah",
		"Loppie",
		"Tymma",
		"Reely",
		"Yjutee",
		"Len",
		"Vixirat",
		"Xumie",
		"Xilly",
		"Liwwy",
		"Gancee",
		"Pamah",
		"Zeryll",
		"Luteet",
		"Qusseet",
		"Alixika",
		"Sepirah",
		"Luttrah",
		"Aramynn",
		"Laxerynn",
		"Murylyt",
		"Quarapyt",
		"Tormiray",
		"Daromynn",
		"Zuleerynn",
		"Quarimat",
		"Dormaquazi",
		"Tullequazi",
		"Aleeray",
		"Eppiquit",
		"Wittirynn",
		"Semiokolipan",
		"Sosopurr",
		"Quamixit",
		"Croffet",
		"Xaalit",
		"Xemiolyt"
	)
	
	private val caliboreseVowels = "aeiouy".toSet()
	private fun randomCaliboreseName(isFemale: Boolean) = caliboreseNames.filter {
		it.length < 8 && (if (isFemale) it.last() in caliboreseVowels else it.last() !in caliboreseVowels)
	}.random() + " " + caliboreseNames.filter { it.length > 7 }.random()
	
	private val diadochiMaleNames = listOf(
		"Oqatai",
		"Amogus",
		"Nerokhan",
		"Choghor",
		"Aghonei",
		"Martaq",
		"Qaran",
		"Khargh",
		"Qolkhu",
		"Ghauran",
		"Woriv",
		"Vorcha",
		"Chagatai",
		"Neghvar",
		"Kitinga",
		"Jimpaq",
		"Bivat",
		"Durash",
		"Elifas"
	)
	
	private val diadochiFemaleNames = listOf(
		"Lursha",
		"Jamoqena",
		"Hikari",
		"Iekuna",
		"Shara",
		"Etugen",
		"Maral",
		"Temuln",
		"Akhensari",
		"Khadagan",
		"Gherelma",
		"Shechen",
		"Althani",
		"Tzyrina",
		"Daghasi",
	)
	
	private val diadochiEpithetParts = listOf(
		"Skull",
		"Blood",
		"Death",
		"Doom",
		"Dread",
		"Soul",
		"Spirit",
		"Hell",
		"Dread",
		"Bale",
		"Fire",
		"Fist",
		"Bear",
		"Pyre",
		"Dark",
		"Vile",
		"Heart",
		"Murder",
		"Gore",
		"Daemon"
	)
	
	private fun randomDiadochiName(isFemale: Boolean) = (if (isFemale) diadochiFemaleNames else diadochiMaleNames).random() + " " + diadochiEpithetParts.random() + diadochiEpithetParts.random().lowercase()
	
	fun randomName(flavor: AdmiralNameFlavor, isFemale: Boolean) = when (flavor) {
		AdmiralNameFlavor.MECHYRDIA -> randomMechyrdianName(isFemale)
		AdmiralNameFlavor.TYLA -> randomTylanName(isFemale)
		AdmiralNameFlavor.CALIBOR -> randomCaliboreseName(isFemale)
		AdmiralNameFlavor.DIADOCHI -> randomDiadochiName(isFemale)
		AdmiralNameFlavor.FULKREYKK -> "TODO NOT IMPLEMENTED" // TODO implement
		AdmiralNameFlavor.AMERICAN -> "TODO NOT IMPLEMENTED" // TODO implement
		AdmiralNameFlavor.HISPANIC_AMERICAN -> "TODO NOT IMPLEMENTED" // TODO implement
	}
}
