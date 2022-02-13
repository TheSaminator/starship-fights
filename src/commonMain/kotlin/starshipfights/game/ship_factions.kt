package starshipfights.game

import kotlinx.html.TagConsumer
import kotlinx.html.p

enum class Faction(
	val shortName: String,
	val shortNameIsDefinite: Boolean,
	val navyName: String,
	val polityName: String,
	val demonymSingular: String,
	val currencyName: String,
	val shipPrefix: String,
	val blurbDesc: TagConsumer<*>.() -> Unit,
) {
	MECHYRDIA(
		shortName = "Mechyrdia",
		shortNameIsDefinite = false,
		navyName = "Mechyrdian Star Fleet",
		polityName = "Empire of Mechyrdia",
		demonymSingular = "Mechyrdian",
		currencyName = "throne",
		shipPrefix = "CMS ", // Ciarstuos Mehurdiasi Štelnau
		blurbDesc = {
			p {
				+"Having spent much of its history coming under threat from oppressive theocracies, invading hordes, and revolutionary insurrections, the Empire of Mechyrdia now enjoys a place in the stars as the foremost power of the galaxy."
			}
			p {
				+"But things are not so ideal for Mechyrdia. The western menace, the Diadochus Masra Draetsen, threatens to upend this peaceful order and conquer Mechyrdia, to succeed where their predecessors the Arkant Horde had failed. Their new leader, Ogus Khan, has made many connections with the disgraced nations of the galaxy, and will stop at nothing to see Mechyrdia fall."
			}
		},
	),
	MASRA_DRAETSEN(
		shortName = "Masra Draetsen",
		shortNameIsDefinite = true,
		navyName = "Masra Draetsen Khoy'qan",
		polityName = "Diadochus Masra Draetsen",
		demonymSingular = "Diadochi",
		currencyName = "sylaph",
		shipPrefix = "", // The Diadochi don't use ship prefixes
		blurbDesc = {
			p {
				+"The Arkant Horde was once the greatest power of its time. Having conquered half of the galaxy in less than a decade, the end of the Horde came when the Mechyrdians' trickery resulted in the death of the Great Khagan, and the Arkant Horde broke into hundreds of petty, feuding Diadochi."
			}
			p {
				+"But now, one of these Diadochi has come to the forefront: the Diadochus Masra Draetsen. Their new leader, Ogus Khan, has forged alliances with many other oppressed nations and remnant states of the galaxy, and stands ready to begin the true conquest of Mechyrdia! May there be woe to the vanquished!"
			}
		},
	),
	ISARNAREYKK(
		shortName = "Isarnareykk",
		shortNameIsDefinite = false,
		navyName = "Isarnareyksk Styurnamariyn",
		polityName = "Isarnareyksk Iunta",
		demonymSingular = "Isarnareyksk",
		currencyName = "mark",
		shipPrefix = "ISMS ", // Isarnareyksk StyurnaMariyn nu Skyf
		blurbDesc = {
			p {
				+"The Isarnareyksk Iunta is the largest and most populous successor state to the Fulkreyksk Authoritariat. A shadow of its former glory, Isarnareykk is led by Faurasitand Demeter Ursalia and ruled by dissenting factions such as the tech barons and the revanchist military, that hate each other more than they hate Ursalia."
			}
			p {
				+"Isarnareykk is at a crossroads now. Shall they embrace democracy and join forces with Mechyrdia? Shall they give the Faurasitand a perpetual dictatorship to end the crisis? Or shall one of the Iunta's factions win out: the military reclaiming the former glory of Fulkreykk, or the tech barons to gain fatter profits?"
			}
		},
	),
	VESTIGIUM(
		shortName = "Vestigium",
		shortNameIsDefinite = true,
		navyName = "Imperial States Space Force",
		polityName = "Imperial States of America",
		demonymSingular = "American",
		currencyName = "dollar",
		shipPrefix = "ISFC ", // Imperial Space Force Craft
		blurbDesc = {
			p {
				+"The Imperial States of America was once the political hyperpower of Earth and beyond, and the ideological bulwark of the Caesarism of its time. They were strong, they were proud... they were hated. Hated to the point that entire nations fled from Earth and colonized the stars just to escape American hegemony."
			}
			p {
				+"The American Empire has been fallen for a long time to barbarian warlords, and its homeworld Earth has been turned into a museum planet by the Mechyrdian government. But the government lives on; hidden away in secret space stations, they desire nothing less than to conquer the stars and establish a ten-thousand-year empire."
			}
		},
	);
	
	val flagUrl: String
		get() = "/static/images/flag/${toUrlSlug()}.svg"
}

fun Faction.getDefiniteShortName(capitalized: Boolean = false) = if (shortNameIsDefinite) {
	(if (capitalized) "The " else "the ") + shortName
} else shortName

val Faction.meshTag: String
	get() = when (this) {
		Faction.MECHYRDIA -> "mechyrdia"
		Faction.MASRA_DRAETSEN -> "diadochi"
		Faction.ISARNAREYKK -> "fulkreykk"
		Faction.VESTIGIUM -> "usa"
	}
