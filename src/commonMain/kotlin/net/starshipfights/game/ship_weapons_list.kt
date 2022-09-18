package net.starshipfights.game

val ShipType.armaments: ShipArmaments
	get() = when (this) {
		ShipType.MICRO -> mechyrdiaShipWeapons(1, false, 1, 0, 0, 0)
		ShipType.NANO -> mechyrdiaNanoClassWeapons()
		ShipType.PICO -> mechyrdiaPicoClassWeapons()
		ShipType.GLADIUS -> mechyrdiaShipWeapons(2, false, 1, 0, 0, 1)
		ShipType.PILUM -> mechyrdiaShipWeapons(2, false, 0, 1, 0, 1)
		ShipType.SICA -> mechyrdiaShipWeapons(2, false, 0, 0, 1, 1)
		ShipType.KAISERSWELT -> mechyrdiaShipWeapons(2, false, 2, 0, 0, 0)
		ShipType.KAROLINA -> mechyrdiaShipWeapons(2, false, 0, 1, 1, 0)
		ShipType.KOZACHNIA -> mechyrdiaShipWeapons(2, false, 1, 0, 1, 0)
		ShipType.MONT_IMPERIAL -> mechyrdiaShipWeapons(2, false, 0, 2, 0, 0)
		ShipType.MUNDUS_CAESARIS_DIVI -> mechyrdiaShipWeapons(0, true, 2, 0, 0, 0)
		ShipType.VENSCA -> mechyrdiaShipWeapons(2, false, 1, 1, 0, 0)
		ShipType.AUCTORITAS -> mechyrdiaShipWeapons(3, false, 1, 1, 0, 2)
		ShipType.CIVITAS -> mechyrdiaShipWeapons(3, false, 1, 0, 1, 2)
		ShipType.HONOS -> mechyrdiaShipWeapons(0, true, 1, 0, 1, 2)
		ShipType.IMPERIUM -> mechyrdiaShipWeapons(3, false, 0, 0, 2, 2)
		ShipType.PAX -> mechyrdiaShipWeapons(3, false, 2, 0, 0, 2)
		ShipType.PIETAS -> mechyrdiaShipWeapons(0, true, 2, 0, 0, 2)
		ShipType.EARTH -> mechyrdiaShipWeapons(3, false, 1, 1, 1, 1)
		ShipType.LANGUAVARTH -> mechyrdiaShipWeapons(3, false, 1, 2, 0, 1)
		ShipType.MECHYRDIA -> mechyrdiaShipWeapons(3, false, 3, 0, 0, 1)
		ShipType.NOVA_ROMA -> mechyrdiaShipWeapons(0, true, 0, 3, 0, 1)
		ShipType.TYLA -> mechyrdiaShipWeapons(3, false, 1, 0, 2, 1)
		
		ShipType.JAGER -> ndrcShipWeapons(2, true, 0, false, 0, 2)
		ShipType.NOVAATJE -> ndrcShipWeapons(0, true, 2, true, 3, 0)
		ShipType.ZWAARD -> ndrcShipWeapons(2, false, 2, true, 3, 0)
		ShipType.SLAGSCHIP -> ndrcShipWeapons(3, false, 2, true, 5, 0)
		ShipType.VOORHOEDE -> ndrcShipWeapons(3, true, 0, false, 3, 1)
		ShipType.KRIJGSCHUIT -> ndrcShipWeapons(4, true, 2, false, 6, 0)
		
		ShipType.ERIS -> diadochiShipWeapons(2, false, 1, 0, 0, 0)
		ShipType.TYPHON -> diadochiShipWeapons(0, false, 1, 0, 0, 1)
		ShipType.AHRIMAN -> diadochiShipWeapons(1, false, 0, 1, 0, 0)
		ShipType.APOPHIS -> diadochiShipWeapons(1, false, 0, 0, 1, 1)
		ShipType.AZATHOTH -> diadochiShipWeapons(1, false, 1, 0, 0, 0)
		ShipType.CHERNOBOG -> diadochiShipWeapons(2, false, 0, 2, 0, 0)
		ShipType.CIPACTLI -> diadochiShipWeapons(2, false, 2, 0, 0, 0)
		ShipType.LAMASHTU -> diadochiShipWeapons(2, false, 0, 1, 1, 0)
		ShipType.LOTAN -> diadochiShipWeapons(2, false, 0, 0, 2, 2)
		ShipType.MORGOTH -> diadochiShipWeapons(2, false, 1, 1, 0, 0)
		ShipType.TIAMAT -> diadochiShipWeapons(2, false, 1, 0, 1, 1)
		ShipType.CHARYBDIS -> diadochiShipWeapons(3, false, 3, 0, 0, 3)
		ShipType.KAKIA -> diadochiShipWeapons(3, false, 1, 2, 0, 3)
		ShipType.MOLOCH -> diadochiShipWeapons(3, false, 1, 1, 1, 3)
		ShipType.SCYLLA -> diadochiShipWeapons(3, false, 1, 0, 2, 3)
		ShipType.AEDON -> diadochiShipWeapons(4, false, 4, 0, 2, 4)
		ShipType.KHAGAN -> diadochiShipWeapons(0, true, 1, 2, 0, 0)
		
		ShipType.KODKOD -> felinaeShipWeapons(
			mapOf(
				FiringArc.BOW to 4,
				FiringArc.ABEAM_PORT to 3,
				FiringArc.ABEAM_STARBOARD to 3,
			),
			emptyMap()
		)
		
		ShipType.ONCILLA -> felinaeShipWeapons(
			mapOf(
				FiringArc.BOW to 2,
			),
			mapOf(
				setOf(FiringArc.BOW, FiringArc.ABEAM_PORT) to 1,
				setOf(FiringArc.BOW, FiringArc.ABEAM_STARBOARD) to 1,
			)
		)
		
		ShipType.MARGAY -> felinaeShipWeapons(
			mapOf(
				FiringArc.BOW to 2,
				FiringArc.ABEAM_PORT to 2,
				FiringArc.ABEAM_STARBOARD to 2,
			),
			mapOf(
				FiringArc.FIRE_FORE_270 to 1,
				setOf(FiringArc.BOW, FiringArc.ABEAM_PORT) to 1,
				setOf(FiringArc.ABEAM_PORT) to 1,
				setOf(FiringArc.BOW, FiringArc.ABEAM_STARBOARD) to 1,
				setOf(FiringArc.ABEAM_STARBOARD) to 1,
			)
		)
		
		ShipType.OCELOT -> felinaeShipWeapons(
			mapOf(
				FiringArc.BOW to 4,
				FiringArc.ABEAM_PORT to 4,
				FiringArc.ABEAM_STARBOARD to 4,
			),
			mapOf(
				FiringArc.FIRE_FORE_270 to 2,
			)
		)
		
		ShipType.BOBCAT -> felinaeShipWeapons(
			mapOf(
				FiringArc.BOW to 3,
				FiringArc.ABEAM_PORT to 3,
				FiringArc.ABEAM_STARBOARD to 3,
			),
			mapOf(
				setOf(FiringArc.BOW, FiringArc.ABEAM_PORT) to 3,
				setOf(FiringArc.BOW, FiringArc.ABEAM_STARBOARD) to 3,
				FiringArc.FIRE_BROADSIDE to 3,
			)
		)
		
		ShipType.LYNX -> felinaeShipWeapons(
			mapOf(
				FiringArc.BOW to 5,
				FiringArc.ABEAM_PORT to 5,
				FiringArc.ABEAM_STARBOARD to 5,
			),
			emptyMap()
		)
		
		ShipType.LEOPARD -> felinaeShipWeapons(
			mapOf(
				FiringArc.BOW to 5,
				FiringArc.ABEAM_PORT to 5,
				FiringArc.ABEAM_STARBOARD to 5,
			),
			emptyMap()
		)
		
		ShipType.TIGER -> felinaeShipWeapons(
			mapOf(
				FiringArc.BOW to 3,
				FiringArc.ABEAM_PORT to 3,
				FiringArc.ABEAM_STARBOARD to 3,
			),
			mapOf(
				setOf(FiringArc.BOW, FiringArc.ABEAM_PORT) to 3,
				setOf(FiringArc.BOW, FiringArc.ABEAM_STARBOARD) to 3,
				FiringArc.FIRE_BROADSIDE to 3,
			)
		)
		
		ShipType.CARACAL -> felinaeShipWeapons(
			mapOf(
				FiringArc.BOW to 10,
				FiringArc.ABEAM_PORT to 10,
				FiringArc.ABEAM_STARBOARD to 10,
			),
			mapOf(
				setOf(FiringArc.BOW) to 5,
				setOf(FiringArc.ABEAM_PORT) to 5,
				setOf(FiringArc.ABEAM_STARBOARD) to 5,
			)
		)
		
		ShipType.GANNAN -> fulkreykkShipWeapons(0, true, 0, 0)
		ShipType.LODOVIK -> fulkreykkShipWeapons(4, false, 0, 0)
		ShipType.KARNAS -> fulkreykkShipWeapons(2, false, 2, 0)
		ShipType.PERTONA -> fulkreykkShipWeapons(2, false, 1, 1)
		ShipType.VOSS -> fulkreykkShipWeapons(2, false, 0, 2)
		ShipType.BREKORYN -> fulkreykkShipWeapons(3, false, 2, 0)
		ShipType.FALK -> fulkreykkShipWeapons(0, true, 1, 1)
		ShipType.LORUS -> fulkreykkShipWeapons(0, true, 2, 0)
		ShipType.ORSH -> fulkreykkShipWeapons(3, false, 0, 2)
		ShipType.TEFRAN -> fulkreykkShipWeapons(3, false, 1, 1)
		ShipType.KASSCK -> fulkreykkShipWeapons(4, false, 3, 0)
		ShipType.KHORR -> fulkreykkShipWeapons(4, false, 1, 2)
		
		ShipType.COLEMAN -> vestigiumShipWeapons(4, 0, 1, 1, 0)
		ShipType.JEFFERSON -> vestigiumShipWeapons(4, 0, 0, 1, 1)
		ShipType.QUENNEY -> vestigiumShipWeapons(4, 0, 0, 0, 2)
		ShipType.ROOSEVELT -> vestigiumShipWeapons(4, 0, 0, 2, 0)
		ShipType.WASHINGTON -> vestigiumShipWeapons(4, 0, 3, 0, 0)
		ShipType.ARLINGTON -> vestigiumShipWeapons(7, 0, 1, 0, 0)
		ShipType.CONCORD -> vestigiumShipWeapons(7, 0, 0, 0, 1)
		ShipType.LEXINGTON -> vestigiumShipWeapons(7, 0, 0, 1, 0)
		ShipType.RAVEN_ROCK -> vestigiumShipWeapons(1, 4, 0, 1, 0)
		ShipType.IOWA -> vestigiumShipWeapons(9, 0, 0, 2, 0)
		ShipType.MARYLAND -> vestigiumShipWeapons(3, 4, 0, 2, 0)
		ShipType.NEW_YORK -> vestigiumShipWeapons(0, 6, 0, 0, 2)
		ShipType.OHIO -> vestigiumShipWeapons(9, 0, 3, 0, 0)
	}
