package net.starshipfights.data.admiralty

import net.starshipfights.game.Faction
import net.starshipfights.game.ShipWeightClass
import kotlin.random.Random

fun newShipName(faction: Faction, shipWeightClass: ShipWeightClass, existingNames: MutableSet<String>) = generateSequence {
	nameShip(faction, shipWeightClass)
}.take(20).dropWhile { it in existingNames }.firstOrNull()?.also { existingNames.add(it) }

private val mechyrdianFrigateNames1 = listOf(
	"Unconquerable",
	"Indomitable",
	"Invincible",
	"Imperial",
	"Regal",
	"Royal",
	"Imperious",
	"Honorable",
	"Defiant",
	"Eternal",
	"Infinite",
	"Dominant",
	"Divine",
	"Righteous",
	"Resplendent",
	"Protective",
	"Innocent",
	"August",
	"Loyal"
)

private val mechyrdianFrigateNames2 = listOf(
	"Faith",
	"Empire",
	"Royalty",
	"Regality",
	"Honor",
	"Defiance",
	"Eternity",
	"Dominator",
	"Divinity",
	"Right",
	"Righteousness",
	"Resplendency",
	"Defender",
	"Protector",
	"Innocence",
	"Victory",
	"Duty",
	"Loyalty"
)

private val mechyrdianCruiserNames1 = listOf(
	"Defender of",
	"Protector of",
	"Shield of",
	"Sword of",
	"Champion of",
	"Hero of",
	"Salvation of",
	"Savior of",
	"Shining Light of",
	"Righteous Flame of",
	"Eternal Glory of",
)

private val mechyrdianCruiserNames2 = listOf(
	"Mechyrd",
	"Kaiserswelt",
	"Tenno no Wakusei",
	"Nova Roma",
	"Mont Imperial",
	"Tyla",
	"Vensca",
	"Kaltag",
	"Languavarth Prime",
	"Languavarth Secundum",
	"Elcialot",
	"Othon",
	"Starport",
	"Sacrilegum",
	"New Constantinople",
	"Fairhus",
	"Praxagora",
	"Karolina",
	"Kozachnia",
	"New New Amsterdam",
	"Mundus Caesaris Divi",
	"Saiwatta",
	"Earth"
)

private val mechyrdianBattleshipNames = listOf(
	"Kaiser Wilhelm I",
	"Kaiser Wilhelm II",
	"Empereur Napoléon I Bonaparte",
	"Tsar Nikolaj II Romanov",
	"Seliger Kaiser Karl I von Habsburg",
	"Emperor Joshua A. Norton I",
	"Emperor Meiji the Great",
	"Emperor Jack G. Coleman",
	"Emperor Trevor C. Neer",
	"Emperor Connor F. Vance",
	"Emperor Jean-Bédel Bokassa I",
	"King Charles XII",
	"King William I the Conqueror",
	"King Alfred the Great",
	"Gustavus Adolphus Magnus Rex",
	"Queen Victoria",
	"Kōnstantînos XI Dragásēs Palaiológos",
	"Ioustinianós I ho Mégas",
	"Kjarossa Liha Vilakauva",
	"Kjarossa Tarkona Sovasra",
	"Great King Kūruš",
	"Queen Elizabeth II",
	"Kjarossa Karelka Helasra",
	"Imperātor Cæsar Dīvī Fīlius Augustus",
	"Cæsar Nerva Trāiānus",
	"King Kaleb of Axum"
)

private fun nameMechyrdianShip(weightClass: ShipWeightClass) = when (weightClass) {
	ShipWeightClass.ESCORT -> "${mechyrdianFrigateNames1.random()} ${mechyrdianFrigateNames2.random()}"
	ShipWeightClass.DESTROYER -> "${mechyrdianFrigateNames1.random()} ${mechyrdianFrigateNames2.random()}"
	ShipWeightClass.CRUISER -> "${mechyrdianCruiserNames1.random()} ${mechyrdianCruiserNames2.random()}"
	ShipWeightClass.BATTLECRUISER -> "${mechyrdianCruiserNames1.random()} ${mechyrdianCruiserNames2.random()}"
	ShipWeightClass.BATTLESHIP -> mechyrdianBattleshipNames.random()
	ShipWeightClass.BATTLE_BARGE -> mechyrdianBattleshipNames.random()
	else -> error("Invalid Mechyrdian ship weight!")
}

private val masraDraetsenFrigateNames1 = listOf(
	"Murderous",
	"Hateful",
	"Heinous",
	"Pestilent",
	"Corrupting",
	"Homicidal",
	"Deadly",
	"Primordial",
	"Painful",
	"Agonizing",
	"Spiteful",
	"Odious",
	"Miserating",
	"Damned",
	"Condemned",
	"Hellish",
	"Dark",
	"Impious",
	"Unfaithful",
	"Abyssal",
	"Furious",
	"Vengeful",
	"Spiritous"
)

private val masraDraetsenFrigateNames2 = listOf(
	"Murder",
	"Hate",
	"Hatred",
	"Pestilence",
	"Corruption",
	"Homicide",
	"Massacre",
	"Death",
	"Agony",
	"Pain",
	"Suffering",
	"Spite",
	"Misery",
	"Damnation",
	"Hell",
	"Darkness",
	"Impiety",
	"Faithlessness",
	"Abyss",
	"Fury",
	"Vengeance",
	"Spirit"
)

private val masraDraetsenCruiserNames1 = listOf(
	"Despoiler of",
	"Desecrator of",
	"Desolator of",
	"Destroyer of",
	"Executioner of",
	"Pillager of",
	"Villain of",
	"Great Devil of",
	"Infidelity of",
	"Incineration of",
	"Immolation of",
	"Crucifixion of",
	"Unending Darkness of",
)

private val masraDraetsenCruiserNames2 = listOf(
	// Diadochi space
	"Eskhaton",
	"Terminus",
	"Tychiphage",
	"Magaddu",
	"Ghattusha",
	"Three Suns",
	"RB-5354",
	"VT-3072",
	"Siegsstern",
	"Atzalstadt",
	"Apex",
	"Summit",
	// Lyudareykk and Isarnareykk
	"Vion Kann",
	"Kasr Karul",
	"Vladizapad",
	// Chaebodes Star Empire
	"Ultima Thule",
	"Prenovez",
	// Calibor and Vescar sectors
	"Letum Angelorum",
	"Pharsalus",
	"Eutopia",
	// Ferthlon and Olympia sectors
	"Ferthlon Primus",
	"Ferthlon Secundus",
	"Nova Roma",
	"Mont Imperial",
)

private const val masraDraetsenColossusName = "Boukephalas"

private fun nameMasraDraetsenShip(weightClass: ShipWeightClass) = when (weightClass) {
	ShipWeightClass.ESCORT -> "${masraDraetsenFrigateNames1.random()} ${masraDraetsenFrigateNames2.random()}"
	ShipWeightClass.DESTROYER -> "${masraDraetsenFrigateNames1.random()} ${masraDraetsenFrigateNames2.random()}"
	ShipWeightClass.CRUISER -> "${masraDraetsenCruiserNames1.random()} ${masraDraetsenCruiserNames2.random()}"
	ShipWeightClass.GRAND_CRUISER -> "${masraDraetsenCruiserNames1.random()} ${masraDraetsenCruiserNames2.random()}"
	ShipWeightClass.COLOSSUS -> masraDraetsenColossusName
	else -> error("Invalid Masra Draetsen ship weight!")
}

enum class LatinNounForm {
	MAS_SG,
	FEM_SG,
	NEU_SG,
	MAS_PL,
	FEM_PL,
	NEU_PL,
}

data class LatinNoun(
	val noun: String,
	val form: LatinNounForm
) {
	override fun toString(): String {
		return noun
	}
}

data class LatinAdjective(
	val masculineSingular: String,
	val feminineSingular: String,
	val neuterSingular: String,
	val masculinePlural: String,
	val femininePlural: String,
	val neuterPlural: String,
) {
	fun get(form: LatinNounForm) = when (form) {
		LatinNounForm.MAS_SG -> masculineSingular
		LatinNounForm.FEM_SG -> feminineSingular
		LatinNounForm.NEU_SG -> neuterSingular
		LatinNounForm.MAS_PL -> masculinePlural
		LatinNounForm.FEM_PL -> femininePlural
		LatinNounForm.NEU_PL -> neuterPlural
	}
}

infix fun LatinNoun.describedBy(adjective: LatinAdjective) = LatinNoun("$this ${adjective.get(form)}", form)

private fun felinaeFelicesEscortShipName() = "ES-" + (1000..9999).random().toString()

private val felinaeFelicesLineShipNames1 = listOf(
	LatinNoun("Aevum", LatinNounForm.NEU_SG),
	LatinNoun("Aquila", LatinNounForm.FEM_SG),
	LatinNoun("Argonauta", LatinNounForm.MAS_SG),
	LatinNoun("Cattus", LatinNounForm.MAS_SG),
	LatinNoun("Daemon", LatinNounForm.MAS_SG),
	LatinNoun("Divitia", LatinNounForm.FEM_SG),
	LatinNoun("Feles", LatinNounForm.FEM_SG),
	LatinNoun("Imperium", LatinNounForm.NEU_SG),
	LatinNoun("Ius", LatinNounForm.NEU_SG),
	LatinNoun("Iustitia", LatinNounForm.FEM_SG),
	LatinNoun("Leo", LatinNounForm.MAS_SG),
	LatinNoun("Leopardus", LatinNounForm.MAS_SG),
	LatinNoun("Lynx", LatinNounForm.FEM_SG),
	LatinNoun("Panthera", LatinNounForm.FEM_SG),
	LatinNoun("Salvator", LatinNounForm.MAS_SG),
	LatinNoun("Scelus", LatinNounForm.NEU_SG),
	LatinNoun("Tigris", LatinNounForm.MAS_SG),
)

private val felinaeFelicesLineShipNames2 = listOf(
	LatinAdjective("Animosus", "Animosa", "Animosum", "Animosi", "Animosae", "Animosa"),
	LatinAdjective("Ardens", "Ardens", "Ardens", "Ardentes", "Ardentes", "Ardentia"),
	LatinAdjective("Audax", "Audax", "Audax", "Audaces", "Audaces", "Audacia"),
	LatinAdjective("Astutus", "Astuta", "Astutum", "Astuti", "Astutae", "Astuta"),
	LatinAdjective("Calidus", "Calida", "Calidum", "Calidi", "Calidae", "Calida"),
	LatinAdjective("Ferox", "Ferox", "Ferox", "Feroces", "Feroces", "Ferocia"),
	LatinAdjective("Fortis", "Fortis", "Forte", "Fortes", "Fortes", "Fortia"),
	LatinAdjective("Fugax", "Fugax", "Fugax", "Fugaces", "Fugaces", "Fugacia"),
	LatinAdjective("Indomitus", "Indomita", "Indomitum", "Indomiti", "Indomitae", "Indomita"),
	LatinAdjective("Intrepidus", "Intrepida", "Intrepidum", "Intrepidi", "Intrepidae", "Intrepida"),
	LatinAdjective("Pervicax", "Pervicax", "Pervicax", "Pervicaces", "Pervicaces", "Pervicacia"),
	LatinAdjective("Sagax", "Sagax", "Sagax", "Sagaces", "Sagaces", "Sagacia"),
	LatinAdjective("Superbus", "Superba", "Superbum", "Superbi", "Superbae", "Superba"),
	LatinAdjective("Trux", "Trux", "Trux", "Truces", "Truces", "Trucia"),
)

private fun nameFelinaeFelicesShip(weightClass: ShipWeightClass) = when (weightClass) {
	ShipWeightClass.FF_ESCORT -> felinaeFelicesEscortShipName()
	ShipWeightClass.FF_DESTROYER -> "${felinaeFelicesLineShipNames1.random() describedBy felinaeFelicesLineShipNames2.random()}"
	ShipWeightClass.FF_CRUISER -> "${felinaeFelicesLineShipNames1.random() describedBy felinaeFelicesLineShipNames2.random()}"
	ShipWeightClass.FF_BATTLECRUISER -> "${felinaeFelicesLineShipNames1.random() describedBy felinaeFelicesLineShipNames2.random()}"
	ShipWeightClass.FF_BATTLESHIP -> if (Random.nextDouble() < 0.01) "Big Floppa" else "${felinaeFelicesLineShipNames1.random() describedBy felinaeFelicesLineShipNames2.random()}"
	else -> error("Invalid Felinae Felices ship weight!")
}

private val isarnareykkShipNames = listOf(
	"Professional with Standards",
	"Online Game Cheater",
	"Actually Made of Antimatter",
	"Chucklehead",
	"Guns Strapped to an Engine",
	"Unidentified Comet",
	"Deep Space Encounter",
	"The Goggles Do Nothing",
	"Sensor Error",
	"ERROR SHIP NAME NOT FOUND",
	"0x426F6174",
	"Börgenkub",
	"Instant Death",
	"Assume The Position",
	"Negative Space Wedgie",
	"Tea, Earl Grey, Hot",
	"There's Coffee In That Nebula",
	"SPEHSS MEHREENS",
	"Inconspicuous Asteroid",
	"Inflatable Toy Ship",
	"HELP TRAPPED IN SHIP FACTORY",
	"Illegal Meme Dealer",
	"Reverse the Polarity!",
	"Send Your Bank Info To Win 10,000 Marks",
	"STOP CALLING ABOUT MY STARSHIP WARRANTY",
	"Somebody Once Told Me...",
	"Praethoris Khorr Gaming",
)

private fun nameIsarnareykskShip() = isarnareykkShipNames.random()

private val vestigiumShipNames = listOf(
	// NAMED AFTER SPACE SHUTTLES
	"Enterprise", // OV-101
	"Columbia", // OV-102
	"Discovery", // OV-103
	"Atlantis", // OV-104
	"Endeavor", // OV-105
	"Conqueror", // OV-106
	"Homeland", // OV-107
	"Augustus", // OV-108
	"Avenger", // OV-109
	"Protector", // OV-110
	
	// NAMED AFTER HISTORICAL SHIPS
	"Yorktown",
	"Lexington",
	"Ranger",
	"Hornet",
	"Wasp",
	"Antares",
	"Belfast",
	// NAMED AFTER PLACES
	"Akron",
	"Hudson",
	"Cleveland",
	"Baltimore",
	"Bel Air",
	"Cedar Rapids",
	"McHenry",
	"Rochester",
	"Cuyahoga Valley",
	"Catonsville",
	"Ocean City",
	"Philadelphia",
	"Somerset",
	"Pittsburgh",
	
	"Las Vegas",
	"Reno",
	"Boulder City",
	"Goodsprings",
	"Nipton",
	"Primm",
	"Nellis",
	"Fortification Hill",
	"McCarran",
	"Fremont",
	
	// NAMED AFTER SPACE PROBES
	"Voyager",
	"Juno",
	"Cassini",
	"Hubble",
	"Huygens",
	"Pioneer",
	
	// NAMED AFTER PEOPLE
	// Founding Fathers
	"George Washington",
	"Thomas Jefferson",
	"John Adams",
	"Alexander Hamilton",
	"James Madison",
	// US Presidents
	"Andrew Jackson",
	"Abraham Lincoln",
	"Theodore Roosevelt",
	"Calvin Coolidge",
	"Dwight Eisenhower",
	// IS Emperors
	"Jack Coleman",
	"Trevor Neer",
	"Hadrey Trevison",
	"Dio Audrey",
	"Connor Vance",
	// Vestigium Leaders
	"Thomas Blackrock",
	"Philip Mack",
	"Ilya Korochenko"
)

private fun nameAmericanShip() = vestigiumShipNames.random()

fun nameShip(faction: Faction, weightClass: ShipWeightClass): String = when (faction) {
	Faction.MECHYRDIA -> nameMechyrdianShip(weightClass)
	Faction.NDRC -> nameMechyrdianShip(weightClass)
	Faction.MASRA_DRAETSEN -> nameMasraDraetsenShip(weightClass)
	Faction.FELINAE_FELICES -> nameFelinaeFelicesShip(weightClass)
	Faction.ISARNAREYKK -> nameIsarnareykskShip()
	Faction.VESTIGIUM -> nameAmericanShip()
}
