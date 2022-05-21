package starshipfights.game

import kotlinx.html.TagConsumer
import kotlinx.html.i
import kotlinx.html.p

enum class Faction(
	val shortName: String,
	val shortNameIsDefinite: Boolean,
	val navyName: String,
	val polityName: String,
	val adjective: String,
	val currencyName: String,
	val shipPrefix: String,
	val blurbDesc: TagConsumer<*>.() -> Unit,
) {
	MECHYRDIA(
		shortName = "Mechyrdia",
		shortNameIsDefinite = false,
		navyName = "Mechyrdian Star Fleet",
		polityName = "Empire of Mechyrdia",
		adjective = "Mechyrdian",
		currencyName = "thrones",
		shipPrefix = "CMŠ ", // Ciarstuos Mehurdiasi Štelnau
		blurbDesc = {
			p {
				+"Having spent much of its history coming under threat from oppressive theocracies, conquering hordes, rebelling sectors, and invading syndicalists, the Empire of Mechyrdia now enjoys a place in the stars as the foremost power of the galaxy."
			}
			p {
				+"Do not be confused by the name \"Empire\", Mechyrdia is a free and liberal democratic republic. While they once had an emperor, Nicólei the First and Only, he declared that the people of Mechyrdia would inherit the throne, thus abolishing the monarchy upon his death. Now the Empire runs on a semi-presidential democracy; the government does not have any office named \"President\", rather there is a Chancellor, the head of state who is elected by the people, and a Prime Minister, the head of government who is appointed by the Chancellor and confirmed by the tricameral Senate. "
			}
			p {
				+"But things are not so ideal for Mechyrdia. The western menace, the Diadochus Masra Draetsen, threatens to upend this peaceful order and conquer Mechyrdia, to succeed where their predecessors the Arkant Horde had failed. Their new leader, Ogus Khan, has made many connections with the disgraced nations of the galaxy, and will stop at nothing to see Mechyrdia fall. Isarnareykk is making waves in its neighboring states of Theudareykk and Stahlareykk, states that are now within Mechyrdia's sphere of influence. Vestigium forces are being spotted in deep space throughout the Empire, and the Corvus Cluster sect has ended its radio silence."
			}
			p {
				+"External problems are not the only issues faced by the Empire. Mechyrdia is also having internal troubles - corruption, erosion of liberty, concentration of wealth and power into an oligarchic elite - all problems that the current Chancellor, Marc Adlerówič Basileiów, and his populist Freedom Party are trying to fix. But his solutions are not without opposition, as various sectors of the Empire: Calibor, Vescar, Texandria, among others, are waging a campaign of passive resistance against Basileiów and his populist Chancery."
			}
			p {
				+"It is the eleventh hour for the Empire of Mechyrdia; shall they enter a new golden age, or a new dark age? Only time will tell."
			}
		},
	),
	NDRC(
		shortName = "NdRC",
		shortNameIsDefinite = true,
		navyName = "Dutch Marines",
		polityName = "Dutch Outer Space Company",
		adjective = "Dutch",
		currencyName = "guldens",
		shipPrefix = "NKS ", // Nederlandse Koopvaardijschip
		blurbDesc = {
			p {
				+"The history of the Dutch Outer Space Company (Dutch: "
				foreign("nl") { +"Nederlandse der Ruimte Compagnie" }
				+") extends almost as far back as that of the American Vestigium. Founded in 2079 to provide space-colonization services to the European continent, the Dutch Outer Space Company has come into frequent conflict with the Imperial States of America."
			}
			p {
				+"They survived during, and fought back against, the Drakhassi and Tylan occupations, waging a guerilla war against the oppressive regimes, as well as supplying other local humans with weapons to rebel too. In doing so, they put aside their differences with the Americans and formed a united front."
			}
			p {
				+"Now, the Dutch Outer Space Company prospers, and so too do their business partners: the Empire of Mechyrdia. But with the imperilment of Mechyrdia to threats both within and without, the Company finds itself in the same danger. Shall it be liberty, or shall it be death?"
			}
			p {
				i { +"Gameside note: Dutch admirals may purchase ships from other factions at a marked-up price, in addition to ships from their own faction." }
			}
		},
	),
	MASRA_DRAETSEN(
		shortName = "Masra Draetsen",
		shortNameIsDefinite = true,
		navyName = "Masra Draetsen Khoy'qan",
		polityName = "Diadochus Masra Draetsen",
		adjective = "Diadochi",
		currencyName = "sylaphs",
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
	FELINAE_FELICES(
		shortName = "Felinae Felices",
		shortNameIsDefinite = true,
		navyName = "Felinae Felices",
		polityName = "Felinae Felices",
		adjective = "Felinae",
		currencyName = "PoliCreds",
		shipPrefix = "NFF ", // Navis Felinarum Felicium
		blurbDesc = {
			p {
				+"The "
				foreign("la") { +"Felinae Felices" }
				+" (fey-LEE-nye fey-LEE-case) are quite the unusual power among the stars. Not a proper nation or state, the "
				foreign("la") { +"Felinae" }
				+" are an organized crime syndicate originating in the Mechyrdian sector of Olympia. They are the second most powerful mafia-like organization in the Empire, second to only their allies of convenience, the "
				foreign("la") { +"Res Nostra" }
				+"."
			}
			p {
				+"Formerly a rival of the "
				foreign("la") { +"Res Nostra" }
				+", the "
				foreign("la") { +"Felinae Felices" }
				+" have turned their attitude 180-degrees under their new "
				foreign("la") { +"Maxima" }
				+", Tanaquil Cassia Pulchra. Now, the "
				foreign("la") { +"Felinae" }
				+" work as shipbuilders for the "
				foreign("la") { +"Res Nostra" }
				+" and other crime syndicates in need of starship fleets, though many are unhappy with the ships they receive, since the "
				foreign("la") { +"Felinae" }
				+" only build cat-themed starships with very little in the way of customizability."
			}
			p {
				+"While the "
				foreign("la") { +"Res Nostra" }
				+" maintain good publicity by being charitable to poor individuals, they do not share this same attitude with competing organizations. The primary reason why they accepted the offer to ally with the "
				foreign("la") { +"Felinae Felices" }
				+" is because the "
				foreign("la") { +"Felinae" }
				+" are one of the most technologically-advanced organizations in the galaxy. "
				foreign("la") { +"Felinae" }
				+" ships have inertialess drives like the Vestigium, but unlike the Vestigium, the syndicate's ships can activate it anywhere, even inside the gravity wells of star systems. Advanced relativistic armor that denies more damage the faster the ship is moving, and weapons such as Particle Claws that can deal multiple critical hits in a single attack, and Lightning Yarn that ignores shields entirely, represent the peak of "
				foreign("la") { +"Felinae" }
				+" high technology."
			}
			p {
				+"The "
				foreign("la") { +"Felinae Felices" }
				+" are a rather secretive organization. The people who observe them, whether they be high-ranking members of anti-mafia organizations or obsessive conspiracy theorists, speculate on how the syndicate gains new members: some believe that the "
				foreign("la") { +"Felinae" }
				+" kidnap, gene-mod, and brainwash people into serving them. Others think that the "
				foreign("la") { +"Felinae" }
				+" invite prominent political figures to join them, offering great power similar to what the Freesysadmins do. No one truly knows what the origin or grand goal of the "
				foreign("la") { +"Felinae Felices" }
				+" is. The only thing that is known for certain, is that their cat-themed starships are making more and more frequent appearances throughout deep space."
			}
		},
	),
	ISARNAREYKK(
		shortName = "Isarnareykk",
		shortNameIsDefinite = false,
		navyName = "Isarnareyksk Styurnamariyn",
		polityName = "Isarnareyksk Federation",
		adjective = "Isarnareyksk",
		currencyName = "marks",
		shipPrefix = "ISS ", // Isarnareyksk Styurnamariyn nu Skyf
		blurbDesc = {
			p {
				+"The Isarnareyksk Federation is the largest and most populous successor state to the Fulkreyksk Authoritariat. A shadow of its former glory, Isarnareykk is led by Faurasitand Demeter Ursalia and ruled by dissenting factions such as the tech barons and the revanchist military, that hate each other more than they hate Ursalia."
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
				+"Isarnareykk is at a crossroads now. Shall they embrace democracy and join forces with Mechyrdia? Shall they give the Faurasitand a perpetual dictatorship to end the crisis? Or shall one of the Federation's factions win out: the military reclaiming the former glory of Fulkreykk, or the tech barons to gain fatter and fatter profits?"
			}
		},
	),
	VESTIGIUM(
		shortName = "Vestigium",
		shortNameIsDefinite = true,
		navyName = "Imperial States Space Force",
		polityName = "Imperial States of America",
		adjective = "American",
		currencyName = "dollars",
		shipPrefix = "ASC ", // American Space Craft
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
		Faction.NDRC -> "ndrc"
		Faction.MASRA_DRAETSEN -> "diadochi"
		Faction.FELINAE_FELICES -> "felinae"
		Faction.ISARNAREYKK -> "fulkreykk"
		Faction.VESTIGIUM -> "usa"
	}
