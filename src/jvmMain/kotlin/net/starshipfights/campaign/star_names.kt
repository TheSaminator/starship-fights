package net.starshipfights.campaign

import net.starshipfights.data.admiralty.LatinAdjective
import net.starshipfights.data.admiralty.LatinNoun
import net.starshipfights.data.admiralty.LatinNounForm
import net.starshipfights.data.admiralty.describedBy

fun Int.toRomanNumerals(): String {
	require(this in 1..3999) { "Roman numerals must be in the range [1, 4000)!" }
	
	val place3 = when (this / 1000) {
		1 -> "M"
		2 -> "MM"
		3 -> "MMM"
		else -> ""
	}
	
	val place2 = when ((this / 100) % 10) {
		1 -> "C"
		2 -> "CC"
		3 -> "CCC"
		4 -> "CD"
		5 -> "D"
		6 -> "DC"
		7 -> "DCC"
		8 -> "DCCC"
		9 -> "CM"
		else -> ""
	}
	
	val place1 = when ((this / 10) % 10) {
		1 -> "X"
		2 -> "XX"
		3 -> "XXX"
		4 -> "XL"
		5 -> "L"
		6 -> "LX"
		7 -> "LXX"
		8 -> "LXXX"
		9 -> "XC"
		else -> ""
	}
	
	val place0 = when (this % 10) {
		1 -> "I"
		2 -> "II"
		3 -> "III"
		4 -> "IV"
		5 -> "V"
		6 -> "VI"
		7 -> "VII"
		8 -> "VIII"
		9 -> "IX"
		else -> ""
	}
	
	return "$place3$place2$place1$place0"
}

private val letters = 'A'..'Z'
private val numbers = (1000..9999)

private fun catalogueName() = "${letters.random()}${letters.random()}-${numbers.random()}"

private val possessiveNames = listOf(
	"Mark's",
	"Antony's",
	"Karl's",
	"Frederick's",
	"Ivan's",
	"Julian's",
	"Erik's",
	"Nicolai's",
	"Theodore's",
	"Sigismund's",
	"Stephan's",
	"William's",
	"George's",
	
	"Octavia's",
	"Annika's",
	"Astrid's",
	"Catherine's",
	"Signy's",
	"Erika's",
	"Freiya's",
	"Hilda's",
	"Zhanna's",
	"Kaarina's",
	
	"Althanar's",
	"Bochra's",
	"Joachim's",
	"Koldimar's",
	"Kor's",
	"Koloth's",
	"Shayel's",
	"Shokar's",
	"Tolavayel's",
	"Voskar's",
	
	"Althē's",
	"Anaseil's",
	"Azetbyr's",
	"Atautha's",
	"Aurantia's",
	"Ilasheva's",
	"Kalora's",
	"Kotolva's",
	"Psekna's",
	"Shenera's",
	"Reoka's",
	"Velga's",
	
	"Aalderik's",
	"Bruno's",
	"Christiaan's",
	"Darnath's",
	"Dirk's",
	"Eren's",
	"Erwin's",
	"Gerlach's",
	"Helbrant's",
	"Hendrik's",
	"Jakob's",
	"Jochem's",
	"Koenraad's",
	"Koorland's",
	"Lodewijk's",
	"Maarten's",
	"Michel's",
	"Pieter's",
	"Renaat's",
	"Rogal's",
	"Ruben's",
	"Sebastiaan's",
	"Sjaak's",
	"Valentijn's",
	"Wiebrand's",
	
	"Eva's",
	"Gerda's",
	"Irene's",
	"Jacqueline's",
	"Josephine's",
	"Margaret's",
	"Maximilia's",
	"Nora's",
	"Rebeka's",
	"Sara's",
	"Wilhelmina's",
	
	"van Birka's",
	"van Heiðabýr's",
	"van Rostok's",
	"van Schverin's",
	"van Bruigge's",
	"van Elbing's",
	"van Stralsund's",
	"van Breslaw's",
	"van Antwerp's",
	"van Zwolle's",
	"van Bremen's",
	"van Kampen's",
	"van Deventer's",
	"van Luinenburg's",
	"van Jastobaal's",
	"van Kupferberg's",
	"van Umboldt's",
	"van d'Wain's",
	"van Horstein's",
	"van Castellan's",
	"van Gerrit's",
	"van d'Aquairre's",
	"van Terozzante's",
	"van d'Argovon's",
	"van Ijzerhoorn's",
	"van Dremel's",
	"van Hinckel's",
	"van Doorn's",
	"van d'Arquebus's",
	"van Vogen's",
)

private val possessedNames = listOf(
	"Reach",
	"Realm",
	"Nebula",
	"World",
	"Hope",
	"Heap",
	"Forge",
	"Conquest",
	"Glory",
	"Paradise",
	"Prize",
	"Tomb",
	"Wake",
	"Bane",
	"Fall",
	"Folly",
)

private fun possessionName() = "${possessiveNames.random()} ${possessedNames.random()}"

private val latinNouns = listOf(
	LatinNoun("Prima", LatinNounForm.FEM_SG),
	LatinNoun("Secunda", LatinNounForm.FEM_SG),
	LatinNoun("Tertia", LatinNounForm.FEM_SG),
	LatinNoun("Ultima", LatinNounForm.FEM_SG),
	LatinNoun("Aedis", LatinNounForm.FEM_SG),
	LatinNoun("Stella", LatinNounForm.FEM_SG),
	LatinNoun("Aster", LatinNounForm.MAS_SG),
	LatinNoun("Mundus", LatinNounForm.MAS_SG),
	LatinNoun("Astrum", LatinNounForm.NEU_SG),
	LatinNoun("Sidus", LatinNounForm.NEU_SG),
	LatinNoun("Sacrarium", LatinNounForm.NEU_SG),
	LatinNoun("Conditorium", LatinNounForm.NEU_SG),
)

private val latinAdjectives = listOf(
	LatinAdjective("Aelius", "Aelia", "Aelium", "Aelii", "Aeliae", "Aelia"),
	LatinAdjective("Aternius", "Aternia", "Aternium", "Aternii", "Aterniae", "Aternia"),
	LatinAdjective("Caecilius", "Caecilia", "Caecilium", "Caecilii", "Caeciliae", "Caecilia"),
	LatinAdjective("Cassius", "Cassia", "Cassium", "Cassii", "Cassiae", "Cassia"),
	LatinAdjective("Claudius", "Claudia", "Claudium", "Claudii", "Claudiae", "Claudia"),
	LatinAdjective("Cornelius", "Cornelia", "Cornelium", "Cornelii", "Corneliae", "Cornelia"),
	LatinAdjective("Calpurnius", "Calpurnia", "Calpurnium", "Calpurnii", "Calpurniae", "Calpurnia"),
	LatinAdjective("Fabius", "Fabia", "Fabium", "Fabii", "Fabiae", "Fabia"),
	LatinAdjective("Flavius", "Flavia", "Flavium", "Flavii", "Flaviae", "Flavia"),
	LatinAdjective("Fulvius", "Fulvia", "Fulvium", "Fulvii", "Fulviae", "Fulvia"),
	LatinAdjective("Haterius", "Hateria", "Haterium", "Haterii", "Hateriae", "Hateria"),
	LatinAdjective("Hostilius", "Hostilia", "Hostilium", "Hostilii", "Hostiliae", "Hostilia"),
	LatinAdjective("Iulius", "Iulia", "Iulium", "Iulii", "Iuliae", "Iulia"),
	LatinAdjective("Iunius", "Iunia", "Iunium", "Iunii", "Iuniae", "Iunia"),
	LatinAdjective("Iuventius", "Iuventia", "Iuventium", "Iuventii", "Iuventiae", "Iuventia"),
	LatinAdjective("Lavinius", "Lavinia", "Lavinium", "Lavinii", "Laviniae", "Lavinia"),
	LatinAdjective("Licinius", "Licinia", "Licinium", "Licinii", "Liciniae", "Licinia"),
	LatinAdjective("Marius", "Maria", "Marium", "Marii", "Mariae", "Maria"),
	LatinAdjective("Octavius", "Octavia", "Octavium", "Octavii", "Octaviae", "Octavia"),
	LatinAdjective("Pompeius", "Pompeia", "Pompeium", "Pompeii", "Pompeiae", "Pompeia"),
	LatinAdjective("Porcius", "Porcia", "Porcium", "Porcii", "Porciae", "Porcia"),
	LatinAdjective("Salvius", "Salvia", "Salvium", "Salvii", "Salviae", "Salvia"),
	LatinAdjective("Sempronius", "Sempronia", "Sempronium", "Sempronii", "Semproniae", "Sempronia"),
	LatinAdjective("Spurius", "Spuria", "Spurium", "Spurii", "Spuriae", "Spuria"),
	LatinAdjective("Terentius", "Terentia", "Terentium", "Terentii", "Terentiae", "Terentia"),
	LatinAdjective("Tullius", "Tullia", "Tullium", "Tullii", "Tulliae", "Tullia"),
	LatinAdjective("Ulpius", "Ulpia", "Ulpium", "Ulpii", "Ulpiae", "Ulpia"),
	LatinAdjective("Valerius", "Valeria", "Valerium", "Valerii", "Valeriae", "Valeria"),
	LatinAdjective("Gaius", "Gaia", "Gaium", "Gaii", "Gaiae", "Gaia"),
	LatinAdjective("Lucius", "Lucia", "Lucium", "Lucii", "Luciae", "Lucia"),
	LatinAdjective("Marcus", "Marca", "Marcum", "Marci", "Marcae", "Marca"),
	LatinAdjective("Publius", "Publia", "Publium", "Publii", "Publiae", "Publia"),
	LatinAdjective("Quintus", "Quinta", "Quintum", "Quinti", "Quintae", "Quinta"),
	LatinAdjective("Titus", "Tita", "Titum", "Titi", "Titae", "Tita"),
	LatinAdjective("Gnaeus", "Gnaea", "Gnaeum", "Gnaei", "Gnaeae", "Gnaea"),
	LatinAdjective("Aulus", "Aula", "Aulum", "Auli", "Aulae", "Aula"),
	LatinAdjective("Spurius", "Spuria", "Spurium", "Spurii", "Spuriae", "Spuria"),
	LatinAdjective("Tiberius", "Tiberia", "Tiberium", "Tiberii", "Tiberiae", "Tiberia"),
	LatinAdjective("Servius", "Servia", "Servium", "Servii", "Serviae", "Servia"),
	LatinAdjective("Hostus", "Hosta", "Hostum", "Hosti", "Hostae", "Hosta"),
)

private fun latinName() = "${latinNouns.random() describedBy latinAdjectives.random()}"

private val constellationStarNames = listOf(
	// Greek
	"Alpha",
	"Beta",
	"Gamma",
	"Delta",
	"Epsilon",
	"Zeta",
	"Eta",
	"Theta",
	"Iota",
	"Kappa",
	"Iota",
	"Mu",
	"Nu",
	"Xi",
	"Omicron",
	"Pi",
	"Rho",
	"Sigma",
	"Tau",
	"Upsilon",
	"Phi",
	"Khi",
	"Psi",
	"Omega",
	// Semitic
	"Aleph",
	"Beth",
	"Gimel",
	"Daleth",
	"Heh",
	"Waw",
	"Zayin",
	"Hheth",
	"Theth",
	"Yodh",
	"Kaph",
	"Lamedh",
	"Mem",
	"Nun",
	"Samekh",
	"Ayin",
	"Peh",
	"Shadeh",
	"Qoph",
	"Resh",
	"Shin",
	"Taw",
	// Germanic
	"Fehu",
	"Urus",
	"Thuris",
	"Ansus",
	"Raida",
	"Kaun",
	"Geba",
	"Wunya",
	"Hagals",
	"Nauths",
	"Eiss",
	"Yer",
	"Eihus",
	"Pairtha",
	"Algs",
	"Sowila",
	"Teiws",
	"Bairkan",
	"Aihus",
	"Manna",
	"Lagus",
	"Ingus",
	"Othal",
	"Dags",
	// Russian
	"Az",
	"Buki",
	"Vedi",
	"Glagol",
	"Dobro",
	"Yest",
	"Zhivete",
	"Zemlya",
	"Izhe",
	"Kako",
	"Lyudi",
	"Myslete",
	"Nash",
	"On",
	"Pokoy",
	"Rtsy",
	"Slovo",
	"Tverdo",
	"Uk",
	"Fert",
	"Kher",
	"Tsy",
	"Cherf",
	"Yery"
)

private val constellationGenitiveNames = listOf(
	"Antliae",
	"Apodis",
	"Aquarii",
	"Aquilae",
	"Arae",
	"Argus Navis",
	"Arietis",
	"Aurigae",
	"Boötis",
	"Caeli",
	"Caesaris Divi",
	"Cancri",
	"Canis Maioris",
	"Canis Minoris",
	"Canum Venaticorum",
	"Capricorni",
	"Cassiopeiae",
	"Centauri",
	"Cephei",
	"Ceti",
	"Circini",
	"Columbae",
	"Comae Berenices",
	"Coronae Australis",
	"Coronae Borealis",
	"Corvi",
	"Crateris",
	"Crucis",
	"Cygni",
	"Delphini",
	"Draconis",
	"Eridani",
	"Fornacis",
	"Geminorum",
	"Herculis",
	"Hydrae",
	"Indi",
	"Lacertae",
	"Leonis",
	"Leonis Minoris",
	"Leporis",
	"Librae",
	"Lupi",
	"Lyrae",
	"Mensae",
	"Mentulae",
	"Muscae",
	"Normae",
	"Ophiuchi",
	"Orionis",
	"Pavonis",
	"Persei",
	"Phoenicis",
	"Pictoris",
	"Piscium",
	"Pyxidis Nauticae",
	"Reticuli",
	"Sagittarii",
	"Scorpii",
	"Scuti",
	"Serpentis",
	"Tauri",
	"Trianguli",
	"Ursae Maioris",
	"Ursae Minoris",
	"Verpae Magnae",
	"Virginis",
	"Vulpeculae"
)

private fun constellationName() = "${constellationStarNames.random()} ${constellationGenitiveNames.random()}"

fun randomStarName() = when {
	(1..8).random() <= 3 -> constellationName()
	(1..5).random() <= 2 -> catalogueName()
	(1..3).random() <= 2 -> possessionName()
	else -> latinName()
}
