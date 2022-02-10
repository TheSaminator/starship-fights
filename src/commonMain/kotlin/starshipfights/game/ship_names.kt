package starshipfights.game

import kotlin.random.Random

fun newShipName(faction: Faction, shipWeightClass: ShipWeightClass, existingNames: MutableSet<String>, random: Random = Random) = generateSequence {
	ShipNames.nameShip(faction, shipWeightClass, random)
}.take(20).dropWhile { it in existingNames }.firstOrNull()?.also { existingNames.add(it) }

object ShipNames {
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
	
	private fun nameMechyrdianShip(weightClass: ShipWeightClass, randomChooser: Random) = when (weightClass) {
		ShipWeightClass.ESCORT -> "${mechyrdianFrigateNames1.random(randomChooser)} ${mechyrdianFrigateNames2.random(randomChooser)}"
		ShipWeightClass.DESTROYER -> "${mechyrdianFrigateNames1.random(randomChooser)} ${mechyrdianFrigateNames2.random(randomChooser)}"
		ShipWeightClass.CRUISER -> "${mechyrdianCruiserNames1.random(randomChooser)} ${mechyrdianCruiserNames2.random(randomChooser)}"
		ShipWeightClass.BATTLECRUISER -> "${mechyrdianCruiserNames1.random(randomChooser)} ${mechyrdianCruiserNames2.random(randomChooser)}"
		ShipWeightClass.BATTLESHIP -> mechyrdianBattleshipNames.random(randomChooser)
		else -> error("Invalid Mechyrdian ship weight!")
	}
	
	private val masraDraetsenShipNames1 = listOf(
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
	
	private val masraDraetsenShipNames2 = listOf(
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
	
	private const val masraDraetsenColossusName = "Boukephalas"
	
	private fun nameMasraDraetsenShip(weightClass: ShipWeightClass, randomChooser: Random) = if (weightClass == ShipWeightClass.COLOSSUS)
		masraDraetsenColossusName
	else "${masraDraetsenShipNames1.random(randomChooser)} ${masraDraetsenShipNames2.random(randomChooser)}"
	
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
	
	private fun nameIsarnareykskShip(randomChooser: Random) = isarnareykkShipNames.random(randomChooser)
	
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
		"Richard Nixon",
		"Ronald Reagan",
		"Donald Trump",
		"Ron DeSantis",
		"Gary Martison",
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
	
	private fun nameAmericanShip(randomChooser: Random) = vestigiumShipNames.random(randomChooser)
	
	fun nameShip(faction: Faction, weightClass: ShipWeightClass, randomChooser: Random = Random): String = when (faction) {
		Faction.MECHYRDIA -> nameMechyrdianShip(weightClass, randomChooser)
		Faction.MASRA_DRAETSEN -> nameMasraDraetsenShip(weightClass, randomChooser)
		Faction.ISARNAREYKK -> nameIsarnareykskShip(randomChooser)
		Faction.VESTIGIUM -> nameAmericanShip(randomChooser)
	}
}
