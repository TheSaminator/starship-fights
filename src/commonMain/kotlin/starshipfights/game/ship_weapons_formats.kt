package starshipfights.game

import starshipfights.data.Id

private class ShipWeaponIdCounter {
	private var numCannons = 0
	private var numLances = 0
	private var numHangars = 0
	private var numTorpedoes = 0
	private var numParticleClaws = 0
	private var numLightningYarn = 0
	
	fun nextId(shipWeapon: ShipWeapon): Id<ShipWeapon> = Id(
		when (shipWeapon) {
			is ShipWeapon.Cannon -> "cannons-${++numCannons}"
			is ShipWeapon.Lance -> "lances-${++numLances}"
			is ShipWeapon.Hangar -> "hangar-${++numHangars}"
			is ShipWeapon.Torpedo -> "torpedo-${++numTorpedoes}"
			is ShipWeapon.ParticleClawLauncher -> "particle-claw-${++numParticleClaws}"
			is ShipWeapon.LightningYarn -> "lightning-yarn-${++numLightningYarn}"
			else -> "super-weapon"
		}
	)
	
	fun add(addTo: MutableMap<Id<ShipWeapon>, ShipWeapon>, shipWeapon: ShipWeapon) {
		if (shipWeapon.numShots <= 0) return
		addTo[nextId(shipWeapon)] = shipWeapon
	}
}

fun mechyrdiaShipWeapons(
	torpedoRows: Int,
	hasMegaCannon: Boolean,
	
	cannonSections: Int,
	lanceSections: Int,
	hangarSections: Int,
	dorsalLances: Int,
): ShipArmaments {
	val idCounter = ShipWeaponIdCounter()
	val weapons = mutableMapOf<Id<ShipWeapon>, ShipWeapon>()
	
	repeat(torpedoRows * 2) {
		idCounter.add(weapons, ShipWeapon.Torpedo(setOf(FiringArc.BOW), "Fore torpedo launchers"))
	}
	
	if (hasMegaCannon)
		idCounter.add(weapons, ShipWeapon.MegaCannon)
	
	repeat(cannonSections) {
		idCounter.add(weapons, ShipWeapon.Cannon(3, setOf(FiringArc.ABEAM_PORT), "Port cannon battery"))
		idCounter.add(weapons, ShipWeapon.Cannon(3, setOf(FiringArc.ABEAM_STARBOARD), "Starboard cannon battery"))
	}
	
	repeat(lanceSections) {
		idCounter.add(weapons, ShipWeapon.Lance(2, setOf(FiringArc.ABEAM_PORT), "Port lance battery"))
		idCounter.add(weapons, ShipWeapon.Lance(2, setOf(FiringArc.ABEAM_STARBOARD), "Starboard lance battery"))
	}
	
	repeat(hangarSections * 2) { w ->
		if (w % 2 == 0)
			idCounter.add(weapons, ShipWeapon.Hangar(StrikeCraftWing.FIGHTERS, "Fighter complement"))
		else
			idCounter.add(weapons, ShipWeapon.Hangar(StrikeCraftWing.BOMBERS, "Bomber complement"))
	}
	
	repeat(dorsalLances) {
		idCounter.add(weapons, ShipWeapon.Lance(1, FiringArc.FIRE_BROADSIDE, "Dorsal lance turrets"))
	}
	
	return ShipArmaments(weapons)
}

fun mechyrdiaNanoClassWeapons(): ShipArmaments {
	val idCounter = ShipWeaponIdCounter()
	val weapons = mutableMapOf<Id<ShipWeapon>, ShipWeapon>()
	
	idCounter.add(weapons, ShipWeapon.Lance(2, FiringArc.FIRE_FORE_270, "Dorsal lance turrets"))
	
	return ShipArmaments(weapons)
}

fun mechyrdiaPicoClassWeapons(): ShipArmaments {
	val idCounter = ShipWeaponIdCounter()
	val weapons = mutableMapOf<Id<ShipWeapon>, ShipWeapon>()
	
	idCounter.add(weapons, ShipWeapon.Cannon(2, FiringArc.FIRE_FORE_270, "Double-barrel cannon turret"))
	idCounter.add(weapons, ShipWeapon.Torpedo(setOf(FiringArc.BOW), "Fore torpedo launcher"))
	
	return ShipArmaments(weapons)
}

fun ndrcShipWeapons(
	torpedoes: Int,
	hasMegaCannon: Boolean,
	
	numDorsalLances: Int,
	foreFiringDorsalLances: Boolean,
	
	numBroadsideCannons: Int,
	numBroadsideLances: Int
): ShipArmaments {
	val idCounter = ShipWeaponIdCounter()
	val weapons = mutableMapOf<Id<ShipWeapon>, ShipWeapon>()
	
	repeat(torpedoes) {
		idCounter.add(weapons, ShipWeapon.Torpedo(setOf(FiringArc.BOW), "Fore torpedo launchers"))
	}
	
	if (hasMegaCannon)
		idCounter.add(weapons, ShipWeapon.MegaCannon)
	
	if (numDorsalLances > 0)
		idCounter.add(weapons, ShipWeapon.Lance(numDorsalLances, if (foreFiringDorsalLances) FiringArc.FIRE_FORE_270 else FiringArc.FIRE_BROADSIDE, "Dorsal lance batteries"))
	
	if (numBroadsideCannons > 0) {
		idCounter.add(weapons, ShipWeapon.Cannon(numBroadsideCannons, setOf(FiringArc.ABEAM_PORT), "Port cannon battery"))
		idCounter.add(weapons, ShipWeapon.Cannon(numBroadsideCannons, setOf(FiringArc.ABEAM_STARBOARD), "Starboard cannon battery"))
	}
	
	if (numBroadsideLances > 0) {
		idCounter.add(weapons, ShipWeapon.Lance(numBroadsideLances, setOf(FiringArc.ABEAM_PORT), "Port lance battery"))
		idCounter.add(weapons, ShipWeapon.Lance(numBroadsideLances, setOf(FiringArc.ABEAM_STARBOARD), "Starboard lance battery"))
	}
	
	return ShipArmaments(weapons)
}

fun diadochiShipWeapons(
	torpedoes: Int,
	hasRevelationGun: Boolean,
	
	cannonSections: Int,
	lanceSections: Int,
	hangarSections: Int,
	dorsalLances: Int,
): ShipArmaments {
	val idCounter = ShipWeaponIdCounter()
	val weapons = mutableMapOf<Id<ShipWeapon>, ShipWeapon>()
	
	repeat(torpedoes) {
		idCounter.add(weapons, ShipWeapon.Torpedo(setOf(FiringArc.BOW), "Fore torpedo launchers"))
	}
	
	if (hasRevelationGun)
		idCounter.add(weapons, ShipWeapon.RevelationGun)
	
	repeat(cannonSections) {
		idCounter.add(weapons, ShipWeapon.Cannon(3, setOf(FiringArc.ABEAM_PORT), "Port cannon battery"))
		idCounter.add(weapons, ShipWeapon.Cannon(3, setOf(FiringArc.ABEAM_STARBOARD), "Starboard cannon battery"))
	}
	
	repeat(lanceSections) {
		idCounter.add(weapons, ShipWeapon.Lance(2, setOf(FiringArc.ABEAM_PORT), "Port lance battery"))
		idCounter.add(weapons, ShipWeapon.Lance(2, setOf(FiringArc.ABEAM_STARBOARD), "Starboard lance battery"))
	}
	
	repeat(hangarSections * 2) { w ->
		if (w % 2 == 0)
			idCounter.add(weapons, ShipWeapon.Hangar(StrikeCraftWing.FIGHTERS, "Fighter complement"))
		else
			idCounter.add(weapons, ShipWeapon.Hangar(StrikeCraftWing.BOMBERS, "Bomber complement"))
	}
	
	repeat(dorsalLances) {
		idCounter.add(weapons, ShipWeapon.Lance(2, FiringArc.FIRE_FORE_270, "Dorsal lance batteries"))
	}
	
	return ShipArmaments(weapons)
}

fun felinaeShipWeapons(
	particleClaws: Map<FiringArc, Int>,
	lightningYarn: Map<Set<FiringArc>, Int>
): ShipArmaments {
	val idCounter = ShipWeaponIdCounter()
	val weapons = mutableMapOf<Id<ShipWeapon>, ShipWeapon>()
	
	for ((arc, num) in particleClaws) {
		idCounter.add(weapons, ShipWeapon.ParticleClawLauncher(num, setOf(arc), "${arc.displayName} particle claws"))
	}
	
	for ((arcs, num) in lightningYarn) {
		val displayName = arcs.joinToString(separator = "/") { it.displayName }
		idCounter.add(weapons, ShipWeapon.LightningYarn(num, arcs, "$displayName lightning yarn"))
	}
	
	return ShipArmaments(weapons)
}

fun fulkreykkShipWeapons(
	torpedoRows: Int,
	hasPulseBeam: Boolean,
	
	cannonSections: Int,
	lanceSections: Int,
): ShipArmaments {
	val idCounter = ShipWeaponIdCounter()
	val weapons = mutableMapOf<Id<ShipWeapon>, ShipWeapon>()
	
	repeat(torpedoRows * 2) {
		idCounter.add(weapons, ShipWeapon.Torpedo(setOf(FiringArc.BOW), "Fore torpedo launchers"))
	}
	
	if (hasPulseBeam)
		idCounter.add(weapons, ShipWeapon.EmpAntenna)
	
	repeat(cannonSections) {
		idCounter.add(weapons, ShipWeapon.Cannon(3, setOf(FiringArc.ABEAM_PORT), "Port cannon battery"))
		idCounter.add(weapons, ShipWeapon.Cannon(3, setOf(FiringArc.ABEAM_STARBOARD), "Starboard cannon battery"))
	}
	
	repeat(lanceSections) {
		idCounter.add(weapons, ShipWeapon.Lance(2, FiringArc.FIRE_BROADSIDE, "Broadside lance battery"))
	}
	
	return ShipArmaments(weapons)
}

fun vestigiumShipWeapons(
	foreCannons: Int,
	foreHangars: Int,
	
	dorsalCannons: Int,
	dorsalLances: Int,
	dorsalHangars: Int,
): ShipArmaments {
	val idCounter = ShipWeaponIdCounter()
	val weapons = mutableMapOf<Id<ShipWeapon>, ShipWeapon>()
	
	idCounter.add(weapons, ShipWeapon.Cannon(foreCannons, setOf(FiringArc.BOW), "Fore cannon battery"))
	
	idCounter.add(weapons, ShipWeapon.Cannon(dorsalCannons, setOf(FiringArc.ABEAM_PORT), "Port cannon battery"))
	idCounter.add(weapons, ShipWeapon.Cannon(dorsalCannons, setOf(FiringArc.ABEAM_STARBOARD), "Starboard cannon battery"))
	
	idCounter.add(weapons, ShipWeapon.Lance(dorsalLances, FiringArc.FIRE_BROADSIDE, "Broadside lance battery"))
	
	repeat(foreHangars + dorsalHangars) { w ->
		if (w % 2 == 0)
			idCounter.add(weapons, ShipWeapon.Hangar(StrikeCraftWing.FIGHTERS, "Fighter complement"))
		else
			idCounter.add(weapons, ShipWeapon.Hangar(StrikeCraftWing.BOMBERS, "Bomber complement"))
	}
	
	return ShipArmaments(weapons)
}
