package starshipfights.game

import kotlinx.html.TagConsumer
import kotlinx.html.i
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
				+"Having spent much of its history coming under threat from oppressive theocracies, conquering hordes, rebelling sectors, and invading syndicalists, the Empire of Mechyrdia now enjoys a place in the stars as the foremost power of the galaxy."
			}
			p {
				+"Don't be confused by the name \"Empire\", Mechyrdia is a free and liberal democratic republic. While they once had an emperor, Nicólei the First and Only, he declared that the people of Mechyrdia would inherit the throne, thus abolishing the monarchy. Now the Empire runs on a semi-presidential democracy; the government does not have any office named \"President\", rather there is a Chancellor, the head of state who is elected by the people, and a Prime Minister, the head of government who is appointed by the Chancellor and confirmed by the tricameral Senate. "
			}
			p {
				+"But things are not so ideal for Mechyrdia. The western menace, the Diadochus Masra Draetsen, threatens to upend this peaceful order and conquer Mechyrdia, to succeed where their predecessors the Arkant Horde had failed. Their new leader, Ogus Khan, has made many connections with the disgraced nations of the galaxy, and will stop at nothing to see Mechyrdia fall. Isarnareykk is making waves in its neighboring states of Theudareykk and Stahlareykk, states that are now within Mechyrdia's sphere of influence. Vestigium forces are being spotted in deep space throughout the Empire, and the Corvus Cluster sect has ended its radio silence."
			}
			p {
				+"External problems aren't the only issues faced by the Empire. Mechyrdia is also having internal troubles - corruption, erosion of liberty, concentration of wealth and power into an oligarchic elite - all problems that the current Chancellor, Marc Adlerówič Basileiów, and his populist Freedom Party are trying to fix. But his solutions are not without opposition, as various sectors of the Empire: Calibor, Vescar, Texandria, among others, are waging a campaign of passive resistance against Basileiów and his populist Chancery."
			}
			p {
				+"It is the eleventh hour for the Empire of Mechyrdia; shall they enter a new golden age, or a new dark age? Only time will tell."
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
				+"The Arkant Horde was once the greatest power of its time. Having conquered half of the galaxy in less than a decade, blessed by their dark god Aedon, the end of the Horde came when the Mechyrdians' trickery resulted in the death of the Great Khagan, and the Arkant Horde broke into hundreds of petty, feuding Diadochi."
			}
			p {
				+"But now, one of these Diadochi has come to the forefront: the Diadochus Masra Draetsen. Known by their friends as freedom-fighters or liberators, and by their enemies as terrorists or barbarian khans from the galactic west, the Masra Draetsen rose to prominence under their current leader Ogus Khan."
			}
			p {
				+"Having conquered 87 other Diadochi star-tribes, Ogus is making alliances with the various oppressed nations and outcast civilizations of the galaxy; groups as diverse as Isarnareyksk tech barons, Vestigium sects, Ilkhan syndicalist intellectuals, Ferthlon rebel remnants, and Olympian pagan elites, have all flocked to the cause of the Masra Draetsen. Soon, the conquest of Mechyrdia will begin. May there be woe to the vanquished!"
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
				+"The Fulkreyksk Authoritariat was one of the oldest civilizations in galactic history, second (within the current cycle, at least) to only the Drakhassi Federation. Early on in Fulkreyksk history, their first Forarr, Vrankenn Kassck, developed the ideology that would characterize the Authoritariat for the rest of its existence: entire cadres of the population would be genetically modified to fit into a randomly-chosen caste: leaders, speakers, bureaucrats, enforcers, warriors, and laborers - families were assigned at random to one of these, and then would receive a retroviral injection to enhance the traits relevant to that caste's work."
			}
			p {
				+"Under their fourth Forarr, Praethoris Khorr, Fulkreykk defeated the daemon warlord Aedonau, who had previously been ravaging the northern half of Drakhassi space. Their next Forarr, Toval Brekoryn, conquered the alien races to the galactic south-east: the Ilkhans, Niska, and Tylans; Brekoryn also reversed some of the totalitarian centralizations that Khorr had instated. Serna Pertona reinstated those Khorrian reforms, which Kor Tefran continued. Eventually, the final Forarr of the First Authoritariat, Augast Voss, would lead Fulkreykk to its demise, and the humans of the galactic north would isolate their entire civilization for over a millennium."
			}
			p {
				+"Fulkreykk returned to galactic politics during the Great Galactic War between the Empire of Mechyrdia and the Ilkhan Commune. The Second Authoritariat invaded the Commune from the north, opening another front that allowed the Mechyrdians to counterattack into the eastern Tylan space. Fulkreyksk and Mechyrdian fleets met at the Ilkhai system, and the space of the Commune was partitioned into a northern, Fulkreykk-aligned Ilkhan Potentate, and a southern Mechyrdia-aligned Ilkhan Republic. A cold war ensued between Fulkreykk and Mechyrdia, resulting in the collapse of the Second Authoritariat. Now, Isarnareykk is left to either pick up the pieces, or forge its own legacy independent of the Fulkreyksk shadow."
			}
			p {
				+"Isarnareykk is at a crossroads now. Shall they embrace democracy and join forces with Mechyrdia? Shall they give the Faurasitand a perpetual dictatorship to end the crisis? Or shall one of the Iunta's factions win out: the military reclaiming the former glory of Fulkreykk, or the tech barons to gain fatter and fatter profits?"
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
				+"The American Empire has its origins in the Second American Civil War, which saw the fall of the American Republic to the first American Emperor, Jack G. Coleman. The Empire was became the new shining city on a hill, the brightest example of what the strong leadership of Caesarism can accomplish. Under Emperor Trevor Neer, the American Empire reached its greatest extent, both in size of territory, and in prosperity. From there, things could only get worse."
			}
			p {
				+"The Second Protestant Reformation started in the mid-21st century AD. At first, it was suppressed by the American government, peaking with Emperor Dio Audrey. However, it would become legal under the first Neoprotestant Emperor, Connor Vance, who also founded the new capital of the Empire in Connor City, on top of old Toronto, which has once been a part of Canada before the conquest of the North."
			}
			p {
				+"Experts within the imperial government realized that the American Empire would fall just like the Roman Empire did, and so they hatched a plan. Creating a secret organization called the Vestigium, these experts evacuated top government and intelligence officials off of Earth, to starbases and planetary colonies operated by the Imperial States Space Force. Eventually, the American Empire finally fell to warlord Odoacro Grande, founder of the "
				i { +"Reino de Columbia" }
				+"."
			}
			p {
				+"The American Empire has been fallen for a long time to barbarian warlords, and its homeworld Earth has been turned into a historical site by the Mechyrdian government. But the government lives on; hidden away in secret space stations, they desire nothing less than to conquer the stars and establish a ten-thousand-year empire."
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
