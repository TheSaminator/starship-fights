package net.starshipfights.game

import net.starshipfights.data.Id
import kotlin.math.roundToInt

fun factionBoardingModifier(faction: Faction): Int = when (faction) {
	Faction.MECHYRDIA -> 7
	Faction.NDRC -> 10
	Faction.MASRA_DRAETSEN -> 8
	Faction.FELINAE_FELICES -> 0
	Faction.ISARNAREYKK -> 3
	Faction.VESTIGIUM -> 2
}

fun weightClassBoardingModifier(weightClass: ShipWeightClass): Int = when (weightClass) {
	ShipWeightClass.ESCORT -> 2
	ShipWeightClass.DESTROYER -> 2
	ShipWeightClass.CRUISER -> 4
	ShipWeightClass.BATTLECRUISER -> 4
	ShipWeightClass.BATTLESHIP -> 6
	
	ShipWeightClass.BATTLE_BARGE -> 8
	
	ShipWeightClass.GRAND_CRUISER -> 6
	ShipWeightClass.COLOSSUS -> 10
	
	ShipWeightClass.FF_ESCORT -> 0
	ShipWeightClass.FF_DESTROYER -> 2
	ShipWeightClass.FF_CRUISER -> 2
	ShipWeightClass.FF_BATTLECRUISER -> 4
	ShipWeightClass.FF_BATTLESHIP -> 6
	
	ShipWeightClass.AUXILIARY_SHIP -> 0
	ShipWeightClass.LIGHT_CRUISER -> 2
	ShipWeightClass.MEDIUM_CRUISER -> 4
	ShipWeightClass.HEAVY_CRUISER -> 6
	
	ShipWeightClass.FRIGATE -> 0
	ShipWeightClass.LINE_SHIP -> 2
	ShipWeightClass.DREADNOUGHT -> 4
}

fun troopsBoardingModifier(troopsAmount: Int, totalTroops: Int): Int = when {
	troopsAmount < totalTroops / 3 -> 0
	troopsAmount < (totalTroops * 2) / 3 -> 2
	troopsAmount < totalTroops -> 3
	troopsAmount == totalTroops -> 4
	else -> 4
}

fun hullBoardingModifier(hullAmount: Int, totalHull: Int): Int = when {
	hullAmount < totalHull / 2 -> 1
	hullAmount < totalHull -> 3
	hullAmount == totalHull -> 5
	else -> 5
}

fun turretsBoardingModifier(turretsDefense: Double, turretsStatus: ShipModuleStatus): Int = when (turretsStatus) {
	ShipModuleStatus.INTACT -> turretsDefense.roundToInt()
	ShipModuleStatus.DAMAGED -> (turretsDefense * 0.5).roundToInt()
	else -> 0
}

fun shieldsBoardingAssaultModifier(shieldsAmount: Int, totalShields: Int): Int = when {
	shieldsAmount == 0 -> 2
	shieldsAmount < totalShields -> 1
	else -> 0
}

fun shieldsBoardingDefenseModifier(shieldsAmount: Int, totalShields: Int): Int = when {
	shieldsAmount == 0 -> 0
	shieldsAmount <= totalShields / 2 -> 1
	shieldsAmount < totalShields -> 2
	else -> 3
}

fun assaultBoardingModifier(assaultModuleStatus: ShipModuleStatus): Int = when (assaultModuleStatus) {
	ShipModuleStatus.INTACT -> 5
	ShipModuleStatus.DAMAGED -> 3
	ShipModuleStatus.DESTROYED -> 0
	else -> 0
}

fun defenseBoardingModifier(defenseModuleStatus: ShipModuleStatus): Int = when (defenseModuleStatus) {
	ShipModuleStatus.INTACT -> 3
	ShipModuleStatus.DAMAGED -> 2
	ShipModuleStatus.DESTROYED -> 0
	else -> 0
}

val ShipInstance.assaultModifier: Int
	get() = listOf(
		factionBoardingModifier(ship.shipType.faction),
		weightClassBoardingModifier(ship.shipType.weightClass),
		troopsBoardingModifier(troopsAmount, durability.troopsDefense),
		hullBoardingModifier(hullAmount, durability.maxHullPoints),
		turretsBoardingModifier(durability.turretDefense, modulesStatus[ShipModule.Turrets]),
		if (canUseShields)
			shieldsBoardingAssaultModifier(shieldAmount, powerMode.shields)
		else shieldsBoardingAssaultModifier(0, powerMode.shields),
		assaultBoardingModifier(modulesStatus[ShipModule.Assault]),
	).sum()

val ShipInstance.defenseModifier: Int
	get() = listOf(
		factionBoardingModifier(ship.shipType.faction),
		weightClassBoardingModifier(ship.shipType.weightClass),
		troopsBoardingModifier(troopsAmount, durability.troopsDefense),
		hullBoardingModifier(hullAmount, durability.maxHullPoints),
		turretsBoardingModifier(durability.turretDefense, modulesStatus[ShipModule.Turrets]),
		if (canUseShields)
			shieldsBoardingDefenseModifier(shieldAmount, powerMode.shields)
		else shieldsBoardingDefenseModifier(0, powerMode.shields),
		defenseBoardingModifier(modulesStatus[ShipModule.Defense]),
	).sum()

fun boardingRoll(): Int = (0..4).random() + (0..4).random()

fun ShipInstance.board(defender: ShipInstance): ImpactResult {
	val myValue = assaultModifier + boardingRoll()
	val otherValue = defender.defenseModifier + boardingRoll()
	
	return when {
		otherValue * 2 < myValue -> {
			when (val firstImpact = ImpactResult.Intact(defender).withCritResult(defender.doCriticalDamage())) {
				is ImpactResult.Damaged -> firstImpact.withCritResult(firstImpact.ship.doCriticalDamage())
				else -> firstImpact
			}
		}
		otherValue <= myValue -> {
			ImpactResult.Intact(defender).withCritResult(defender.doCriticalDamage())
		}
		else -> {
			val troopsKilled = (1..(myValue / 2)).randomOrNull() ?: 0
			ImpactResult.Intact(defender).withCritResult(defender.killTroops(troopsKilled))
		}
	}
}

fun ShipInstance.afterBoarding() = if (troopsAmount <= 1) null else copy(
	troopsAmount = troopsAmount - 1,
	hasSentBoardingParty = true,
)

fun reportBoardingResult(impactResult: ImpactResult, attacker: Id<ShipInstance>) = when (impactResult) {
	is ImpactResult.Destroyed -> ChatEntry.ShipDestroyed(
		ship = impactResult.ship.id,
		sentAt = Moment.now,
		destroyedBy = ShipAttacker.EnemyShip(attacker)
	)
	is ImpactResult.Damaged -> ChatEntry.ShipBoarded(
		ship = impactResult.ship.id,
		boarder = attacker,
		sentAt = Moment.now,
		critical = impactResult.critical.report(),
		damageAmount = impactResult.damage.amount
	)
}

fun ShipInstance.getBoardingPickRequest() = PickRequest(
	PickType.Ship(allowSides = setOf(owner.other)),
	PickBoundary.WeaponsFire(
		center = position.location,
		facing = position.facing,
		minDistance = SHIP_BASE_SIZE,
		maxDistance = firepower.rangeMultiplier * SHIP_TRANSPORTARIUM_RANGE,
		firingArcs = FiringArc.FIRE_FORE_270,
	)
)
