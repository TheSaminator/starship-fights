package starshipfights.game

enum class ShipWeightClass(
	val meshIndex: Int,
	val rank: Int
) {
	// General
	ESCORT(1, 0),
	DESTROYER(2, 1),
	CRUISER(3, 2),
	BATTLECRUISER(4, 3),
	BATTLESHIP(5, 4),
	
	// Masra Draetsen-specific
	GRAND_CRUISER(4, 3),
	COLOSSUS(5, 5),
	
	// Isarnareykk-specific
	AUXILIARY_SHIP(1, 0),
	LIGHT_CRUISER(2, 1),
	MEDIUM_CRUISER(3, 2),
	HEAVY_CRUISER(4, 4),
	
	// Vestigium-specific
	FRIGATE(1, 0),
	LINE_SHIP(3, 2),
	DREADNOUGHT(5, 4),
	;
	
	val displayName: String
		get() = name.lowercase().split('_').joinToString(separator = " ") { word -> word.replaceFirstChar { c -> c.uppercase() } }
	
	val basePointCost: Int
		get() = when (this) {
			ESCORT -> 50
			DESTROYER -> 100
			CRUISER -> 200
			BATTLECRUISER -> 250
			BATTLESHIP -> 350
			
			GRAND_CRUISER -> 300
			COLOSSUS -> 500
			
			AUXILIARY_SHIP -> 50
			LIGHT_CRUISER -> 100
			MEDIUM_CRUISER -> 200
			HEAVY_CRUISER -> 400
			
			FRIGATE -> 150
			LINE_SHIP -> 275
			DREADNOUGHT -> 400
		}
	
	val isUnique: Boolean
		get() = this == COLOSSUS
}

enum class ShipType(
	val faction: Faction,
	val weightClass: ShipWeightClass,
) {
	// Mechyrdia
	BLITZ(Faction.MECHYRDIA, ShipWeightClass.ESCORT),
	
	GLADIUS(Faction.MECHYRDIA, ShipWeightClass.DESTROYER),
	PILUM(Faction.MECHYRDIA, ShipWeightClass.DESTROYER),
	SICA(Faction.MECHYRDIA, ShipWeightClass.DESTROYER),
	
	KAISERSWELT(Faction.MECHYRDIA, ShipWeightClass.CRUISER),
	KAROLINA(Faction.MECHYRDIA, ShipWeightClass.CRUISER),
	KOZACHNIA(Faction.MECHYRDIA, ShipWeightClass.CRUISER),
	MONT_IMPERIAL(Faction.MECHYRDIA, ShipWeightClass.CRUISER),
	MUNDUS_CAESARIS_DIVI(Faction.MECHYRDIA, ShipWeightClass.CRUISER),
	VENSCA(Faction.MECHYRDIA, ShipWeightClass.CRUISER),
	
	AUCTORITAS(Faction.MECHYRDIA, ShipWeightClass.BATTLECRUISER),
	CIVITAS(Faction.MECHYRDIA, ShipWeightClass.BATTLECRUISER),
	HONOS(Faction.MECHYRDIA, ShipWeightClass.BATTLECRUISER),
	IMPERIUM(Faction.MECHYRDIA, ShipWeightClass.BATTLECRUISER),
	PAX(Faction.MECHYRDIA, ShipWeightClass.BATTLECRUISER),
	PIETAS(Faction.MECHYRDIA, ShipWeightClass.BATTLECRUISER),
	
	EARTH(Faction.MECHYRDIA, ShipWeightClass.BATTLESHIP),
	LANGUAVARTH(Faction.MECHYRDIA, ShipWeightClass.BATTLESHIP),
	MECHYRDIA(Faction.MECHYRDIA, ShipWeightClass.BATTLESHIP),
	NOVA_ROMA(Faction.MECHYRDIA, ShipWeightClass.BATTLESHIP),
	TYLA(Faction.MECHYRDIA, ShipWeightClass.BATTLESHIP),
	
	// Masra Draetsen
	ERIS(Faction.MASRA_DRAETSEN, ShipWeightClass.ESCORT),
	TYPHON(Faction.MASRA_DRAETSEN, ShipWeightClass.ESCORT),
	
	AHRIMAN(Faction.MASRA_DRAETSEN, ShipWeightClass.DESTROYER),
	APOPHIS(Faction.MASRA_DRAETSEN, ShipWeightClass.DESTROYER),
	AZATHOTH(Faction.MASRA_DRAETSEN, ShipWeightClass.DESTROYER),
	
	CHERNOBOG(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	CIPACTLI(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	LOTAN(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	MORGOTH(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	TIAMAT(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	
	CHARYBDIS(Faction.MASRA_DRAETSEN, ShipWeightClass.GRAND_CRUISER),
	SCYLLA(Faction.MASRA_DRAETSEN, ShipWeightClass.GRAND_CRUISER),
	
	AEDON(Faction.MASRA_DRAETSEN, ShipWeightClass.COLOSSUS),
	
	// Isarnareykk
	GANNAN(Faction.ISARNAREYKK, ShipWeightClass.AUXILIARY_SHIP),
	LODOVIK(Faction.ISARNAREYKK, ShipWeightClass.AUXILIARY_SHIP),
	
	KARNAS(Faction.ISARNAREYKK, ShipWeightClass.LIGHT_CRUISER),
	PERTONA(Faction.ISARNAREYKK, ShipWeightClass.LIGHT_CRUISER),
	VOSS(Faction.ISARNAREYKK, ShipWeightClass.LIGHT_CRUISER),
	
	BREKORYN(Faction.ISARNAREYKK, ShipWeightClass.MEDIUM_CRUISER),
	FALK(Faction.ISARNAREYKK, ShipWeightClass.MEDIUM_CRUISER),
	LORUS(Faction.ISARNAREYKK, ShipWeightClass.MEDIUM_CRUISER),
	ORSH(Faction.ISARNAREYKK, ShipWeightClass.MEDIUM_CRUISER),
	TEFRAN(Faction.ISARNAREYKK, ShipWeightClass.MEDIUM_CRUISER),
	
	KASSCK(Faction.ISARNAREYKK, ShipWeightClass.HEAVY_CRUISER),
	KHORR(Faction.ISARNAREYKK, ShipWeightClass.HEAVY_CRUISER),
	
	// Vestigium
	COLEMAN(Faction.VESTIGIUM, ShipWeightClass.FRIGATE),
	JEFFERSON(Faction.VESTIGIUM, ShipWeightClass.FRIGATE),
	QUENNEY(Faction.VESTIGIUM, ShipWeightClass.FRIGATE),
	ROOSEVELT(Faction.VESTIGIUM, ShipWeightClass.FRIGATE),
	WASHINGTON(Faction.VESTIGIUM, ShipWeightClass.FRIGATE),
	
	ARLINGTON(Faction.VESTIGIUM, ShipWeightClass.LINE_SHIP),
	CONCORD(Faction.VESTIGIUM, ShipWeightClass.LINE_SHIP),
	LEXINGTON(Faction.VESTIGIUM, ShipWeightClass.LINE_SHIP),
	RAVEN_ROCK(Faction.VESTIGIUM, ShipWeightClass.LINE_SHIP),
	
	IOWA(Faction.VESTIGIUM, ShipWeightClass.DREADNOUGHT),
	MARYLAND(Faction.VESTIGIUM, ShipWeightClass.DREADNOUGHT),
	NEW_YORK(Faction.VESTIGIUM, ShipWeightClass.DREADNOUGHT),
	OHIO(Faction.VESTIGIUM, ShipWeightClass.DREADNOUGHT),
	;
	
	val displayName: String
		get() = name.lowercase().split('_').joinToString(separator = " ") { word -> word.replaceFirstChar { c -> c.uppercase() } }
	
	val fullDisplayName: String
		get() = "$displayName-class ${weightClass.displayName}"
	
	val fullerDisplayName: String
		get() = "$displayName-class ${faction.demonymSingular} ${weightClass.displayName}"
}

val ShipType.meshName: String
	get() = "${faction.meshTag}-${weightClass.meshIndex}-${toUrlSlug()}-class"
