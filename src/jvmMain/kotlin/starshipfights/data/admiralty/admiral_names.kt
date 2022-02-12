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
	
	private val tylanFamilyNames = listOf(
		"Kalevkar" to "Kalevka",
		"Merku" to "Merkussa",
		"Telet" to "Telet",
		"Eutokar" to "Eutoka",
		"Vsocha" to "Vsochessa",
		"Vilar" to "Vilakauva",
		"Nikasrar" to "Nika",
		"Vlegamakar" to "Vlegamaka",
		"Vtokassar" to "Vtoka",
		"Theiar" to "Theia",
		"Aretar" to "Areta",
		"Derkas" to "Derkata",
		"Vinsennas" to "Vinsennata",
		"Kleio" to "Kleona"
	)
	
	// Tylans use matronymics for both sons and daughters
	private fun randomTylanName(isFemale: Boolean) = if (isFemale)
		tylanFemaleNames.random().first + " " + tylanFemaleNames.random().second + "-Nahra " + tylanFamilyNames.random().second
	else
		tylanMaleNames.random().first + " " + tylanFemaleNames.random().second + "-Nensar " + tylanFamilyNames.random().first
	
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
		"Qitinga",
		"Jimpaq",
		"Bivat",
		"Durash",
		"Elifas"
	)
	
	private val diadochiFemaleNames = listOf(
		"Lursha",
		"Jamoqena",
		"Lokoria",
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
		"Claw",
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
		"Daemon",
		"Talon"
	)
	
	private fun randomDiadochiName(isFemale: Boolean) = (if (isFemale) diadochiFemaleNames else diadochiMaleNames).random() + " " + diadochiEpithetParts.random() + diadochiEpithetParts.random().lowercase()
	
	private val thedishMaleNames = listOf(
		"Prethoris",
		"Severus",
		"Augast",
		"Dagobar",
		"Vrankenn",
		"Kandar",
		"Kleon",
		"Glaius",
		"Karul",
		"Ylai",
		"Toval",
		"Ivon",
		"Belis",
		"Jorh"
	)
	
	private val thedishFemaleNames = listOf(
		"Serna",
		"Veleska",
		"Ielga",
		"Glae",
		"Rova",
		"Ylia",
		"Galera",
		"Nerys",
		"Veleer",
		"Karuleyn",
		"Amberli",
		"Alysia",
		"Lenera",
	)
	
	private val thedishSurnames = listOf(
		"Kassck",
		"Orsh",
		"Falk",
		"Khorr",
		"Vaskoman",
		"Vholkazk",
		"Brekoryn",
		"Lorus",
		"Karnas",
		"Hathar",
		"Takan",
		"Pertona",
		"Tefran",
		"Arvi",
		"Galvus",
		"Voss",
		"Mandanof"
	)
	
	private fun randomThedishName(isFemale: Boolean) = (if (isFemale) thedishFemaleNames else thedishMaleNames).random() + " " + thedishSurnames.random()
	
	private val americanMaleNames = listOf(
		"George",
		"John",
		"Thomas",
		"James",
		"Quincy",
		"Andrew",
		"Martin",
		"William",
		"Henry",
		"James",
		"Zachary",
		"Millard",
		"Franklin",
		"Abraham",
		"Ulysses",
		"Rutherford",
		"Chester",
		"Grover",
		"Benjamin",
		"Theodore",
		"Warren",
		"Calvin",
		"Herbert",
		"Harry",
		"Dwight",
		"Lyndon",
		"Richard",
		"Dick",
		"Gerald",
		"Jimmy",
		"Ronald",
		"Donald"
	)
	
	private val americanFemaleNames = listOf(
		"Martha",
		"Abigail",
		"Elizabeth",
		"Louisa",
		"Emily",
		"Sarah",
		"Anna",
		"Jane",
		"Julia",
		"Margaret",
		"Harriet",
		"Mary",
		"Lucy",
		"Rose",
		"Caroline",
		"Ida",
		"Helen",
		"Grace",
		"Jacqueline",
		"Thelma",
		"Eleanor",
		"Nancy",
		"Barbara",
		"Laura",
		"Melania"
	)
	
	private val americanFamilyNames = listOf(
		"Knox",
		"Pickering",
		"McHenry",
		"Dexter",
		"Drawborn",
		"Eustis",
		"Armstrong",
		"Monroe",
		"Crawford",
		"Calhoun",
		"Barbour",
		"Porter",
		"Eaton",
		"Cass",
		"Poinsett",
		"Bell",
		"Forrestal",
		"Johnson",
		"Marshall",
		"Lovett",
		"Wilson",
		"McElroy",
		"McNamara",
		"Clifford",
		"Richardson",
		"Burndt",
	)
	
	private fun randomAmericanName(isFemale: Boolean) = (if (isFemale) americanFemaleNames else americanMaleNames).random() + " " + americanFamilyNames.random()
	
	private val hispanicMaleNames = listOf(
		"Aaron",
		"Antonio",
		"Augusto",
		"Eliseo",
		"Manuel",
		"Jose",
		"Juan",
		"Miguel",
		"Rafael",
		"Raul",
		"Adriano",
		"Emilio",
		"Francisco",
		"Ignacio",
		"Marco",
		"Pablo",
		"Octavio",
		"Victor",
		"Vito",
		"Valentin"
	)
	
	private val hispanicFemaleNames = listOf(
		"Maria",
		"Ana",
		"Camila",
		"Eva",
		"Flora",
		"Gloria",
		"Julia",
		"Marcelina",
		"Rosalia",
		"Victoria",
		"Valentina",
		"Cecilia",
		"Francisca",
		"Aurelia",
		"Cristina",
		"Magdalena",
		"Margarita",
		"Martina",
		"Teresa"
	)
	
	private val hispanicFamilyNames = listOf(
		"Acorda",
		"Aguirre",
		"Alzaga",
		"Arriaga",
		"Arrieta",
		"Berroya",
		"Barahona",
		"Carranza",
		"Carriaga",
		"Elcano",
		"Elizaga",
		"Endaya",
		"Franco",
		"Garalde",
		"Ibarra",
		"Juarez",
		"Lazarte",
		"Legarda",
		"Madariaga",
		"Medrano",
		"Narvaez",
		"Olano",
		"Ricarte",
		"Salazar",
		"Uriarte",
		"Varona",
		"Vergar",
	)
	
	private fun randomHispanicName(isFemale: Boolean) = (if (isFemale) hispanicFemaleNames else hispanicMaleNames).random() + " " + hispanicFamilyNames.random()
	
	fun randomName(flavor: AdmiralNameFlavor, isFemale: Boolean) = when (flavor) {
		AdmiralNameFlavor.MECHYRDIA -> randomMechyrdianName(isFemale)
		AdmiralNameFlavor.TYLA -> randomTylanName(isFemale)
		AdmiralNameFlavor.CALIBOR -> randomCaliboreseName(isFemale)
		AdmiralNameFlavor.DIADOCHI -> randomDiadochiName(isFemale)
		AdmiralNameFlavor.FULKREYKK -> randomThedishName(isFemale)
		AdmiralNameFlavor.AMERICAN -> randomAmericanName(isFemale)
		AdmiralNameFlavor.HISPANIC_AMERICAN -> randomHispanicName(isFemale)
	}
}
