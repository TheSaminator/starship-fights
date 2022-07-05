package net.starshipfights.data.space

import net.starshipfights.data.admiralty.LatinAdjective
import net.starshipfights.data.admiralty.LatinNoun
import net.starshipfights.data.admiralty.LatinNounForm
import net.starshipfights.data.admiralty.describedBy

fun newStarName(existingNames: MutableSet<String>) = generateSequence {
	randomStarName()
}.take(20).dropWhile { it in existingNames }.firstOrNull()?.also { existingNames.add(it) }

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
	"Rogaal's",
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
	LatinNoun("Dominatus", LatinNounForm.MAS_SG),
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

private val constellationBayerNames = listOf(
	// Semitic
	"Alep",
	"Bet",
	"Giml",
	"Dalet",
	"Heh",
	"Waw",
	"Zayin",
	"H'et",
	"T'et",
	"Yod",
	"Kap",
	"Lamed",
	"Mem",
	"Nun",
	"Samek",
	"Ayin",
	"Peh",
	"S'adeh",
	"Qop",
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

private val letters = 'A'..'Z'

private fun Int.pow(x: Int) = (1..x).fold(1) { acc, _ -> acc * this }
private fun generateNDigitNumber(n: Int) = (10.pow(n - 1) until 10.pow(n)).random()

private fun generateConstellationStarName(): String {
	val prefix = letters.shuffled().take((1..3).random()).joinToString(separator = "")
	val infix = listOf(" ", "-", "").random()
	val suffix = generateNDigitNumber((2..4).random())
	return "$prefix$infix$suffix"
}

private val constellationNamesWithGenitives = listOf(
	"Antlia" to "Antliae",
	"Apus" to "Apodis",
	"Aquarius" to "Aquarii",
	"Aquila" to "Aquilae",
	"Ara" to "Arae",
	"Argo Navis" to "Argus Navis",
	"Aries" to "Arietis",
	"Auriga" to "Aurigae",
	"Boötes" to "Boötis",
	"Caelum" to "Caeli",
	"Caesar Divus" to "Caesaris Divi",
	"Cancer" to "Cancri",
	"Canis Maior" to "Canis Maioris",
	"Canis Minor" to "Canis Minoris",
	"Canes Venatici" to "Canum Venaticorum",
	"Capricornus" to "Capricorni",
	"Cassiopeia" to "Cassiopeiae",
	"Centaurus" to "Centauri",
	"Cepheus" to "Cephei",
	"Cetus" to "Ceti",
	"Circinus" to "Circini",
	"Columba" to "Columbae",
	"Coma Berenices" to "Comae Berenices",
	"Corona Australis" to "Coronae Australis",
	"Corona Borealis" to "Coronae Borealis",
	"Corvus" to "Corvi",
	"Crater" to "Crateris",
	"Crux" to "Crucis",
	"Cygnus" to "Cygni",
	"Delphinus" to "Delphini",
	"Draco" to "Draconis",
	"Eridanus" to "Eridani",
	"Fornax" to "Fornacis",
	"Gemini" to "Geminorum",
	"Hercules" to "Herculis",
	"Hydra" to "Hydrae",
	"Indus" to "Indi",
	"Lacerta" to "Lacertae",
	"Leo" to "Leonis",
	"Leo Minor" to "Leonis Minoris",
	"Lepus" to "Leporis",
	"Libra" to "Librae",
	"Lupus" to "Lupi",
	"Lyra" to "Lyrae",
	"Mensa" to "Mensae",
	"Mentula" to "Mentulae",
	"Musca" to "Muscae",
	"Norma" to "Normae",
	"Ophiuchus" to "Ophiuchi",
	"Orion" to "Orionis",
	"Pavo" to "Pavonis",
	"Perseus" to "Persei",
	"Phoenix" to "Phoenicis",
	"Pictor" to "Pictoris",
	"Pisces" to "Piscium",
	"Pyxis Nautica" to "Pyxidis Nauticae",
	"Reticulum" to "Reticuli",
	"Sagittarius" to "Sagittarii",
	"Scorpius" to "Scorpii",
	"Scutum" to "Scuti",
	"Serpens" to "Serpentis",
	"Taurus" to "Tauri",
	"Triangulum" to "Trianguli",
	"Ursa Maior" to "Ursae Maioris",
	"Ursa Minor" to "Ursae Minoris",
	"Verpa Magna" to "Verpae Magnae",
	"Virgo" to "Virginis",
	"Vulpecula" to "Vulpeculae"
)

private fun bayerName() = "${constellationBayerNames.random()} ${constellationNamesWithGenitives.random().second}"
private fun constellationCatalogueName() = "${constellationNamesWithGenitives.random().first} ${generateConstellationStarName()}"

fun randomStarName() = when ((1..10).random()) {
	in 1..4 -> bayerName()
	in 5..7 -> constellationCatalogueName()
	in 8..9 -> latinName()
	else -> possessionName()
}
