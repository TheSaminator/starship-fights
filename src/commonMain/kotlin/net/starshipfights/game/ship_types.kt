package net.starshipfights.game

enum class ShipTier {
	ESCORT, LIGHT_CRUISER, CRUISER, BATTLECRUISER, BATTLESHIP, TITAN;
	
	val displayName: String
		get() = name.lowercase().split('_').joinToString(separator = " ") { word -> word.replaceFirstChar { c -> c.uppercase() } }
}

enum class ShipWeightClass(
	val meshIndex: Int,
	val tier: ShipTier
) {
	// General
	ESCORT(1, ShipTier.ESCORT),
	DESTROYER(2, ShipTier.LIGHT_CRUISER),
	CRUISER(3, ShipTier.CRUISER),
	BATTLECRUISER(4, ShipTier.BATTLECRUISER),
	BATTLESHIP(5, ShipTier.BATTLESHIP),
	
	// NdRC-specific
	BATTLE_BARGE(5, ShipTier.BATTLECRUISER),
	
	// Masra Draetsen-specific
	GRAND_CRUISER(4, ShipTier.BATTLECRUISER),
	COLOSSUS(5, ShipTier.TITAN),
	
	// Felinae Felices-specific
	FF_ESCORT(1, ShipTier.ESCORT),
	FF_DESTROYER(2, ShipTier.LIGHT_CRUISER),
	FF_CRUISER(3, ShipTier.CRUISER),
	FF_BATTLECRUISER(4, ShipTier.BATTLECRUISER),
	FF_BATTLESHIP(5, ShipTier.BATTLESHIP),
	
	// Isarnareykk-specific
	AUXILIARY_SHIP(1, ShipTier.ESCORT),
	LIGHT_CRUISER(2, ShipTier.LIGHT_CRUISER),
	MEDIUM_CRUISER(3, ShipTier.CRUISER),
	HEAVY_CRUISER(4, ShipTier.BATTLECRUISER),
	
	// Vestigium-specific
	FRIGATE(1, ShipTier.ESCORT),
	LINE_SHIP(3, ShipTier.CRUISER),
	DREADNOUGHT(5, ShipTier.BATTLESHIP),
	;
	
	val displayName: String
		get() = if (this in FF_ESCORT..FF_BATTLESHIP)
			name.lowercase().split('_').joinToString(separator = " ") { word -> word.replaceFirstChar { c -> c.uppercase() } }.removePrefix("Ff ")
		else
			name.lowercase().split('_').joinToString(separator = " ") { word -> word.replaceFirstChar { c -> c.uppercase() } }
	
	val basePointCost: Int
		get() = when (this) {
			ESCORT -> 50
			DESTROYER -> 100
			CRUISER -> 200
			BATTLECRUISER -> 250
			BATTLESHIP -> 350
			
			BATTLE_BARGE -> 300
			
			GRAND_CRUISER -> 300
			COLOSSUS -> 490
			
			FF_ESCORT -> 25
			FF_DESTROYER -> 125
			FF_CRUISER -> 175
			FF_BATTLECRUISER -> 275
			FF_BATTLESHIP -> 325
			
			AUXILIARY_SHIP -> 50
			LIGHT_CRUISER -> 100
			MEDIUM_CRUISER -> 200
			HEAVY_CRUISER -> 400
			
			FRIGATE -> 100
			LINE_SHIP -> 200
			DREADNOUGHT -> 300
		}
	
	val isUnique: Boolean
		get() = this == COLOSSUS
}

enum class ShipType(
	val faction: Faction,
	val weightClass: ShipWeightClass,
) {
	// Mechyrdia
	MICRO(Faction.MECHYRDIA, ShipWeightClass.ESCORT),
	NANO(Faction.MECHYRDIA, ShipWeightClass.ESCORT),
	PICO(Faction.MECHYRDIA, ShipWeightClass.ESCORT),
	
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
	
	// NdRC
	JAGER(Faction.NDRC, ShipWeightClass.DESTROYER),
	NOVAATJE(Faction.NDRC, ShipWeightClass.DESTROYER),
	ZWAARD(Faction.NDRC, ShipWeightClass.DESTROYER),
	
	SLAGSCHIP(Faction.NDRC, ShipWeightClass.CRUISER),
	VOORHOEDE(Faction.NDRC, ShipWeightClass.CRUISER),
	
	KRIJGSCHUIT(Faction.NDRC, ShipWeightClass.BATTLE_BARGE),
	
	// Masra Draetsen
	ERIS(Faction.MASRA_DRAETSEN, ShipWeightClass.ESCORT),
	PAZUZU(Faction.MASRA_DRAETSEN, ShipWeightClass.ESCORT),
	TYPHON(Faction.MASRA_DRAETSEN, ShipWeightClass.ESCORT),
	
	AHRIMAN(Faction.MASRA_DRAETSEN, ShipWeightClass.DESTROYER),
	AIPALOOVIK(Faction.MASRA_DRAETSEN, ShipWeightClass.DESTROYER),
	APOPHIS(Faction.MASRA_DRAETSEN, ShipWeightClass.DESTROYER),
	AZATHOTH(Faction.MASRA_DRAETSEN, ShipWeightClass.DESTROYER),
	
	CHARON(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	CHERNOBOG(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	CIPACTLI(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	ERESHKIGAL(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	LAMASHTU(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	LOTAN(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	MORGOTH(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	TAMAG(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	TIAMAT(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	WHIRO(Faction.MASRA_DRAETSEN, ShipWeightClass.CRUISER),
	
	CHARYBDIS(Faction.MASRA_DRAETSEN, ShipWeightClass.GRAND_CRUISER),
	KAKIA(Faction.MASRA_DRAETSEN, ShipWeightClass.GRAND_CRUISER),
	MOLOCH(Faction.MASRA_DRAETSEN, ShipWeightClass.GRAND_CRUISER),
	SCYLLA(Faction.MASRA_DRAETSEN, ShipWeightClass.GRAND_CRUISER),
	THANATOS(Faction.MASRA_DRAETSEN, ShipWeightClass.GRAND_CRUISER),
	
	AEDON(Faction.MASRA_DRAETSEN, ShipWeightClass.COLOSSUS),
	KHAGAN(Faction.MASRA_DRAETSEN, ShipWeightClass.COLOSSUS),
	
	// Felinae Felices
	KODKOD(Faction.FELINAE_FELICES, ShipWeightClass.FF_ESCORT),
	ONCILLA(Faction.FELINAE_FELICES, ShipWeightClass.FF_ESCORT),
	
	MARGAY(Faction.FELINAE_FELICES, ShipWeightClass.FF_DESTROYER),
	OCELOT(Faction.FELINAE_FELICES, ShipWeightClass.FF_DESTROYER),
	
	BOBCAT(Faction.FELINAE_FELICES, ShipWeightClass.FF_CRUISER),
	LYNX(Faction.FELINAE_FELICES, ShipWeightClass.FF_CRUISER),
	
	LEOPARD(Faction.FELINAE_FELICES, ShipWeightClass.FF_BATTLECRUISER),
	TIGER(Faction.FELINAE_FELICES, ShipWeightClass.FF_BATTLECRUISER),
	
	CARACAL(Faction.FELINAE_FELICES, ShipWeightClass.FF_BATTLESHIP),
	
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
		get() = "$displayName-class ${faction.adjective} ${weightClass.displayName}"
}

val ShipType.pointCost: Int
	get() = weightClass.basePointCost + armaments.values.sumOf { it.addsPointCost }

val ShipType.meshName: String
	get() = "${faction.meshTag}-${weightClass.meshIndex}-${toUrlSlug()}-class"
