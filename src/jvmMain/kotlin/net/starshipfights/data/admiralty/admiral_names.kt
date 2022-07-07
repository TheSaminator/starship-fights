package net.starshipfights.data.admiralty

import net.starshipfights.game.Faction
import net.starshipfights.game.FactionFlavor
import kotlin.random.Random

enum class AdmiralNameFlavor {
	MECHYRDIA, TYLA, CALIBOR, OLYMPIA, // Mechyrdia-aligned
	DUTCH, // NdRC-aliged
	NORTHERN_DIADOCHI, SOUTHERN_DIADOCHI, // Masra Draetsen-aligned
	FULKREYKK, // Isarnareykk-aligned
	AMERICAN, HISPANIC_AMERICAN; // Vestigium-aligned
	
	val displayName: String
		get() = when (this) {
			MECHYRDIA -> "Mechyrdian"
			TYLA -> "Tylan"
			CALIBOR -> "Caliborese"
			OLYMPIA -> "Olympian"
			DUTCH -> "Dutch"
			NORTHERN_DIADOCHI -> "Northern Diadochi"
			SOUTHERN_DIADOCHI -> "Southern Diadochi"
			FULKREYKK -> "Thedish"
			AMERICAN -> "American"
			HISPANIC_AMERICAN -> "Hispanic-American"
		}
	
	companion object {
		fun forFaction(faction: Faction) = when (faction) {
			Faction.MECHYRDIA -> setOf(MECHYRDIA, TYLA, CALIBOR, OLYMPIA, DUTCH)
			Faction.NDRC -> setOf(DUTCH)
			Faction.MASRA_DRAETSEN -> setOf(CALIBOR, NORTHERN_DIADOCHI, SOUTHERN_DIADOCHI)
			Faction.FELINAE_FELICES -> setOf(OLYMPIA)
			Faction.ISARNAREYKK -> setOf(FULKREYKK)
			Faction.VESTIGIUM -> setOf(AMERICAN, HISPANIC_AMERICAN)
		}
		
		fun forFactionFlavor(flavor: FactionFlavor) = when (flavor) {
			FactionFlavor.MECHYRDIA -> setOf(MECHYRDIA, TYLA, DUTCH)
			FactionFlavor.TYLA -> setOf(TYLA)
			FactionFlavor.OLYMPIA -> setOf(OLYMPIA)
			FactionFlavor.TEXANDRIA -> setOf(MECHYRDIA, TYLA, DUTCH)
			
			FactionFlavor.NDRC -> setOf(DUTCH)
			FactionFlavor.CCC -> setOf(MECHYRDIA, TYLA, DUTCH)
			FactionFlavor.MJOLNIR_ENERGY -> setOf(MECHYRDIA, TYLA, DUTCH)
			
			FactionFlavor.MASRA_DRAETSEN -> setOf(CALIBOR, NORTHERN_DIADOCHI, SOUTHERN_DIADOCHI)
			FactionFlavor.AEDON_CULTISTS -> setOf(NORTHERN_DIADOCHI, SOUTHERN_DIADOCHI)
			FactionFlavor.FERTHLON_EXILES -> setOf(MECHYRDIA, CALIBOR)
			
			FactionFlavor.RES_NOSTRA -> setOf(OLYMPIA)
			FactionFlavor.CORSAIRS -> setOf(OLYMPIA, CALIBOR)
			FactionFlavor.FELINAE_FELICES -> setOf(OLYMPIA)
			
			FactionFlavor.ISARNAREYKK -> setOf(FULKREYKK)
			FactionFlavor.SWARTAREYKK -> setOf(FULKREYKK)
			FactionFlavor.THEUDAREYKK -> setOf(FULKREYKK)
			FactionFlavor.STAHLAREYKK -> setOf(FULKREYKK)
			FactionFlavor.LYUDAREYKK -> setOf(FULKREYKK)
			FactionFlavor.NEUIA_FULKREYKK -> setOf(FULKREYKK)
			
			FactionFlavor.CORVUS_CLUSTER_VESTIGIUM -> setOf(AMERICAN, HISPANIC_AMERICAN)
			FactionFlavor.COLEMAN_SF_BASE_VESTIGIUM -> setOf(AMERICAN, HISPANIC_AMERICAN)
			FactionFlavor.NEW_AUSTIN_VESTIGIUM -> setOf(AMERICAN, HISPANIC_AMERICAN)
		}
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
		"Friðurix" to "Friðurigówič",
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
		"Vinsennas" to "Vinsenatta",
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
		it.length < 8 && (isFemale == (it.last() in caliboreseVowels))
	}.random() + " " + caliboreseNames.filter { it.length > 7 }.random()
	
	private val latinMaleCommonPraenomina = listOf(
		"Gaius",
		"Lucius",
		"Marcus",
	)
	
	private val latinMaleUncommonPraenomina = listOf(
		"Publius",
		"Quintus",
		"Titus",
		"Gnaeus",
	)
	
	private val latinMaleRarePraenomina = listOf(
		"Aulus",
		"Spurius",
		"Tiberius",
		"Servius",
		"Hostus",
	)
	
	private val latinFemaleCommonPraenomina = listOf(
		"Gaia",
		"Lucia",
		"Marcia",
	)
	
	private val latinFemaleUncommonPraenomina = listOf(
		"Prima",
		"Secunda",
		"Tertia",
		"Quarta",
		"Quinta",
		"Sexta",
		"Septima",
		"Octavia",
		"Nona",
		"Decima",
	)
	
	private val latinFemaleRarePraenomina = listOf(
		"Caesula",
		"Titia",
		"Tiberia",
		"Tanaquil",
	)
	
	private val latinNominaGentilica = listOf(
		"Aelius" to "Aelia",
		"Aternius" to "Aternia",
		"Caecilius" to "Caecilia",
		"Cassius" to "Cassia",
		"Claudius" to "Claudia",
		"Cornelius" to "Cornelia",
		"Calpurnius" to "Calpurnia",
		"Fabius" to "Fabia",
		"Flavius" to "Flavia",
		"Fulvius" to "Fulvia",
		"Haterius" to "Hateria",
		"Hostilius" to "Hostilia",
		"Iulius" to "Iulia",
		"Iunius" to "Iunia",
		"Iuventius" to "Iuventia",
		"Lavinius" to "Lavinia",
		"Licinius" to "Licinia",
		"Marius" to "Maria",
		"Octavius" to "Octavia",
		"Pompeius" to "Pompeia",
		"Porcius" to "Porcia",
		"Salvius" to "Salvia",
		"Sempronius" to "Sempronia",
		"Spurius" to "Spuria",
		"Terentius" to "Terentia",
		"Tullius" to "Tullia",
		"Ulpius" to "Ulpia",
		"Valerius" to "Valeria"
	)
	
	private val latinCognomina = listOf(
		"Agricola" to "Agricola",
		"Agrippa" to "Agrippina",
		"Aquilinus" to "Aquilina",
		"Balbus" to "Balba",
		"Bibulus" to "Bibula",
		"Bucco" to "Bucco",
		"Caecus" to "Caeca",
		"Calidus" to "Calida",
		"Catilina" to "Catilina",
		"Catulus" to "Catula",
		"Crassus" to "Crassa",
		"Crispus" to "Crispa",
		"Drusus" to "Drusilla",
		"Flaccus" to "Flacca",
		"Gracchus" to "Graccha",
		"Laevinus" to "Laevina",
		"Lanius" to "Lania",
		"Lepidus" to "Lepida",
		"Lucullus" to "Luculla",
		"Marcellus" to "Marcella",
		"Metellus" to "Metella",
		"Nasica" to "Nasica",
		"Nerva" to "Nerva",
		"Paullus" to "Paulla",
		"Piso" to "Piso",
		"Priscus" to "Prisca",
		"Publicola" to "Publicola",
		"Pulcher" to "Pulchra",
		"Regulus" to "Regula",
		"Rufus" to "Rufa",
		"Scaevola" to "Scaevola",
		"Severus" to "Severa",
		"Structus" to "Structa",
		"Taurus" to "Taura",
		"Varro" to "Varro",
		"Vitulus" to "Vitula"
	)
	
	private fun randomLatinPraenomen(isFemale: Boolean) = when {
		Random.nextBoolean() -> if (isFemale) latinFemaleCommonPraenomina else latinMaleCommonPraenomina
		Random.nextInt(3) > 0 -> if (isFemale) latinFemaleUncommonPraenomina else latinMaleUncommonPraenomina
		else -> if (isFemale) latinFemaleRarePraenomina else latinMaleRarePraenomina
	}.random()
	
	private fun randomLatinName(isFemale: Boolean) = randomLatinPraenomen(isFemale) + " " + latinNominaGentilica.random().let { (m, f) -> if (isFemale) f else m } + " " + latinCognomina.random().let { (m, f) -> if (isFemale) f else m }
	
	private val dutchMaleNames = listOf(
		"Aalderik",
		"Andreas",
		"Boudewijn",
		"Bruno",
		"Christiaan",
		"Cornelius",
		"Darnath",
		"Dirk",
		"Eren",
		"Erwin",
		"Frederik",
		"Gerlach",
		"Helbrant",
		"Helbrecht",
		"Hendrik",
		"Jakob",
		"Jochem",
		"Joris",
		"Koenraad",
		"Koorland",
		"Leopold",
		"Lodewijk",
		"Maarten",
		"Michel",
		"Niels",
		"Pieter",
		"Renaat",
		"Rogaal",
		"Ruben",
		"Sebastiaan",
		"Sigismund",
		"Sjaak",
		"Tobias",
		"Valentijn",
		"Wiebrand",
	)
	
	private val dutchFemaleNames = listOf(
		"Adelwijn",
		"Amberlij",
		"Annika",
		"Arete",
		"Eva",
		"Gerda",
		"Helga",
		"Ida",
		"Irene",
		"Jacqueline",
		"Josefien",
		"Juliana",
		"Katharijne",
		"Lore",
		"Margriet",
		"Maximilia",
		"Meike",
		"Nora",
		"Rebeka",
		"Sara",
		"Vera",
		"Wilhelmina",
	)
	
	private val dutchMerchantHouses = listOf(
		"Venetho",
		"Luibeck",
		"Birka",
		"Heiðabýr",
		"Rostok",
		"Guistrov",
		"Schverin",
		"Koeln",
		"Bruigge",
		"Reval",
		"Elbing",
		"Dorpat",
		"Stralsund",
		"Mijdeborg",
		"Breslaw",
		"Dortmund",
		"Antwerp",
		"Falsterbo",
		"Zwolle",
		"Buchtehud",
		"Bremen",
		"Zutphen",
		"Kampen",
		"Grunn",
		"Deventer",
		"Wismer",
		"Luinenburg",
		
		"Jager",
		"Jastobaal",
		"Varonius",
		"Kupferberg",
		"Dijn",
		"Umboldt",
		"Phalomor",
		"Drijk",
		"d'Wain",
		"du Languille",
		"Horstein",
		"Jerulas",
		"Kendar",
		"Castellan",
		"d'Aniasie",
		"Gerrit",
		"Hoed",
		"lo Pan",
		"Marchandrij",
		"d'Aquairre",
		"Terozzante",
		"d'Argovon",
		"de Monde",
		"Paillender",
		"Holstijn",
		"d'Imperia",
		"Borodin",
		"Agranozza",
		"d'Ortise",
		"Ijzerhoorn",
		"Dremel",
		"Hinckel",
		"Vuigens",
		"Drazen",
		"Marburg",
		"Xardt",
		"Lijze",
		"Gerlach",
		"Doorn",
		"d'Arquebus",
		"Alderic",
		"Vogen"
	)
	
	private fun randomDutchName(isFemale: Boolean) = (if (isFemale) dutchFemaleNames else dutchMaleNames).random() + " van " + dutchMerchantHouses.random()
	
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
		"Elifas",
		"Ogus",
		"Yuli",
		"Saret",
		"Mher",
		"Tyver",
		"Ghraq",
		"Niran",
		"Galik"
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
		"Kloya",
	)
	
	private val northernDiadochiEpithetParts = listOf(
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
		"Talon",
	)
	
	private fun randomNorthernDiadochiName(isFemale: Boolean) = (if (isFemale) diadochiFemaleNames else diadochiMaleNames).random() + " " + northernDiadochiEpithetParts.random() + northernDiadochiEpithetParts.random().lowercase()
	
	private val southernDiadochiClans = listOf(
		"Arkai",
		"Avado",
		"Djahhim",
		"Khankhen",
		"Porok",
		"Miras",
		"Terok",
		"Empok",
		"Noragh",
		"Nuunian",
		"Soung",
		"Akhero",
		"Qozaq",
		"Kherus",
		"Axina",
		"Ghaizas",
		"Saxha",
		"Meshu",
		"Khopesh",
		"Qitemar",
		"Vang",
		"Lugal",
		"Galla",
		"Hheka",
		"Nesut",
		"Koquon",
		"Molekh"
	)
	
	private fun randomSouthernDiadochiClan() = when {
		Random.nextInt(5) == 0 -> southernDiadochiClans.random() + "-" + southernDiadochiClans.random()
		else -> southernDiadochiClans.random()
	}
	
	private fun randomSouthernDiadochiName(isFemale: Boolean) = (if (isFemale) diadochiFemaleNames else diadochiMaleNames).random() + (if (isFemale && Random.nextBoolean()) " ka-" else " am-") + diadochiMaleNames.random() + " " + randomSouthernDiadochiClan()
	
	private val thedishMaleNames = listOf(
		"Praethoris",
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
		"Jorh",
		"Svar",
		"Alaric",
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
		"Demeter",
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
		"Mandanof",
		"Ursali",
		"Vytunn",
		"Quesrinn",
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
		AdmiralNameFlavor.OLYMPIA -> randomLatinName(isFemale)
		AdmiralNameFlavor.DUTCH -> randomDutchName(isFemale)
		AdmiralNameFlavor.NORTHERN_DIADOCHI -> randomNorthernDiadochiName(isFemale)
		AdmiralNameFlavor.SOUTHERN_DIADOCHI -> randomSouthernDiadochiName(isFemale)
		AdmiralNameFlavor.FULKREYKK -> randomThedishName(isFemale)
		AdmiralNameFlavor.AMERICAN -> randomAmericanName(isFemale)
		AdmiralNameFlavor.HISPANIC_AMERICAN -> randomHispanicName(isFemale)
	}
}
