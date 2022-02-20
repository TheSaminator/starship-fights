package starshipfights.game

import starshipfights.data.Id

private class ShipWeaponIdCounter {
	private var numCannons = 1
	private var numLances = 1
	private var numHangars = 1
	private var numTorpedoes = 1
	
	fun nextId(shipWeapon: ShipWeapon): Id<ShipWeapon> = Id(
		when (shipWeapon) {
			is ShipWeapon.Cannon -> "cannons-${numCannons++}"
			is ShipWeapon.Lance -> "lances-${numLances++}"
			is ShipWeapon.Hangar -> "hangar-${numHangars++}"
			is ShipWeapon.Torpedo -> "torpedo-${numTorpedoes++}"
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

fun diadochiShipWeapons(
	torpedoes: Int,
	foreLances: Int,
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
	
	idCounter.add(weapons, ShipWeapon.Lance(foreLances, setOf(FiringArc.BOW), "Fore lance battery"))
	
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
		if (w % 3 == 0)
			idCounter.add(weapons, ShipWeapon.Hangar(StrikeCraftWing.FIGHTERS, "Fighter complement"))
		else
			idCounter.add(weapons, ShipWeapon.Hangar(StrikeCraftWing.BOMBERS, "Bomber complement"))
	}
	
	repeat(dorsalLances) {
		idCounter.add(weapons, ShipWeapon.Lance(2, FiringArc.FIRE_BROADSIDE, "Dorsal lance turrets"))
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
