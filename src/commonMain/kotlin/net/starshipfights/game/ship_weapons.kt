package net.starshipfights.game

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id
import kotlin.math.*
import kotlin.random.Random

enum class FiringArc {
	BOW, ABEAM_PORT, ABEAM_STARBOARD, STERN;
	
	val displayName: String
		get() = when (this) {
			BOW -> "Fore"
			ABEAM_PORT -> "Port"
			ABEAM_STARBOARD -> "Starboard"
			STERN -> "Aft"
		}
	
	companion object {
		val FIRE_360: Set<FiringArc> = setOf(BOW, ABEAM_PORT, ABEAM_STARBOARD, STERN)
		val FIRE_BROADSIDE: Set<FiringArc> = setOf(ABEAM_PORT, ABEAM_STARBOARD)
		val FIRE_FORE_270: Set<FiringArc> = setOf(BOW, ABEAM_PORT, ABEAM_STARBOARD)
	}
}

sealed interface AreaWeapon {
	val areaRadius: Double
	val isLine: Boolean
		get() = false
}

@Serializable
sealed class ShipWeapon {
	abstract val numShots: Int
	
	open val minRange: Double
		get() = SHIP_BASE_SIZE
	abstract val maxRange: Double
	abstract val firingArcs: Set<FiringArc>
	
	abstract val groupLabel: String
	
	abstract val addsPointCost: Int
	
	abstract fun instantiate(): ShipWeaponInstance
	
	@Serializable
	data class Cannon(
		override val numShots: Int,
		override val firingArcs: Set<FiringArc>,
		override val groupLabel: String,
	) : ShipWeapon() {
		override val maxRange: Double
			get() = SHIP_CANNON_RANGE
		
		override val addsPointCost: Int
			get() = numShots * 5
		
		override fun instantiate() = ShipWeaponInstance.Cannon(this)
	}
	
	@Serializable
	data class Lance(
		override val numShots: Int,
		override val firingArcs: Set<FiringArc>,
		override val groupLabel: String,
	) : ShipWeapon() {
		override val maxRange: Double
			get() = SHIP_LANCE_RANGE
		
		override val addsPointCost: Int
			get() = numShots * 10
		
		override fun instantiate() = ShipWeaponInstance.Lance(this, 10.0)
	}
	
	@Serializable
	data class Torpedo(
		override val firingArcs: Set<FiringArc>,
		override val groupLabel: String,
	) : ShipWeapon() {
		override val numShots: Int
			get() = 1
		
		override val maxRange: Double
			get() = SHIP_TORPEDO_RANGE
		
		override val addsPointCost: Int
			get() = 5
		
		override fun instantiate() = ShipWeaponInstance.Torpedo(this)
	}
	
	@Serializable
	data class Hangar(
		val wing: StrikeCraftWing,
		override val groupLabel: String,
	) : ShipWeapon() {
		override val numShots: Int
			get() = 1
		
		override val maxRange: Double
			get() = SHIP_HANGAR_RANGE
		
		override val firingArcs: Set<FiringArc>
			get() = FiringArc.FIRE_360
		
		override val addsPointCost: Int
			get() = when (wing) {
				StrikeCraftWing.FIGHTERS -> 5
				StrikeCraftWing.BOMBERS -> 10
			}
		
		override fun instantiate() = ShipWeaponInstance.Hangar(this, 1.0)
	}
	
	// FELINAE FELICES ADVANCED WEAPONS
	
	@Serializable
	data class ParticleClawLauncher(
		override val numShots: Int,
		override val firingArcs: Set<FiringArc>,
		override val groupLabel: String,
	) : ShipWeapon() {
		override val maxRange: Double
			get() = 1750.0
		
		override val addsPointCost: Int
			get() = numShots * 5
		
		override fun instantiate() = ShipWeaponInstance.ParticleClawLauncher(this)
	}
	
	@Serializable
	data class LightningYarn(
		override val numShots: Int,
		override val firingArcs: Set<FiringArc>,
		override val groupLabel: String,
	) : ShipWeapon() {
		override val maxRange: Double
			get() = 1250.0
		
		override val addsPointCost: Int
			get() = numShots * 5
		
		override fun instantiate() = ShipWeaponInstance.LightningYarn(this)
	}
	
	// HEAVY WEAPONS
	
	@Serializable
	object MegaCannon : ShipWeapon(), AreaWeapon {
		override val numShots: Int
			get() = 3
		
		override val minRange: Double
			get() = 3_000.0
		
		override val maxRange: Double
			get() = 7_000.0
		
		override val areaRadius: Double
			get() = 450.0
		
		override val firingArcs: Set<FiringArc>
			get() = setOf(FiringArc.BOW)
		
		override val groupLabel: String
			get() = "Mega Giga Cannon"
		
		override val addsPointCost: Int
			get() = 50
		
		override fun instantiate() = ShipWeaponInstance.MegaCannon(numShots)
	}
	
	@Serializable
	object RevelationGun : ShipWeapon(), AreaWeapon {
		override val numShots: Int
			get() = 1
		
		override val maxRange: Double
			get() = 2_000.0
		
		override val areaRadius: Double
			get() = SHIP_BASE_SIZE
		
		override val isLine: Boolean
			get() = true
		
		override val firingArcs: Set<FiringArc>
			get() = setOf(FiringArc.BOW)
		
		override val groupLabel: String
			get() = "Revelation Gun"
		
		override val addsPointCost: Int
			get() = 66
		
		override fun instantiate() = ShipWeaponInstance.RevelationGun(numShots)
	}
	
	@Serializable
	object EmpAntenna : ShipWeapon(), AreaWeapon {
		override val numShots: Int
			get() = 5
		
		override val maxRange: Double
			get() = 3_000.0
		
		override val areaRadius: Double
			get() = 650.0
		
		override val firingArcs: Set<FiringArc>
			get() = setOf(FiringArc.BOW)
		
		override val groupLabel: String
			get() = "EMP Emitter"
		
		override val addsPointCost: Int
			get() = 40
		
		override fun instantiate() = ShipWeaponInstance.EmpAntenna(numShots)
	}
}

enum class StrikeCraftWing {
	FIGHTERS, BOMBERS;
	
	val displayName: String
		get() = name.lowercase().replaceFirstChar { it.uppercase() }
	
	val iconUrl: String
		get() = "/static/game/images/strike-craft-${toUrlSlug()}.svg"
}

@Serializable
sealed class ShipWeaponInstance {
	abstract val weapon: ShipWeapon
	
	@Serializable
	data class Cannon(override val weapon: ShipWeapon.Cannon) : ShipWeaponInstance()
	
	@Serializable
	data class Lance(override val weapon: ShipWeapon.Lance, val numCharges: Double) : ShipWeaponInstance() {
		val charge: Double
			get() = -expm1(-numCharges)
	}
	
	@Serializable
	data class Torpedo(override val weapon: ShipWeapon.Torpedo) : ShipWeaponInstance()
	
	@Serializable
	data class Hangar(override val weapon: ShipWeapon.Hangar, val wingHealth: Double) : ShipWeaponInstance()
	
	// FELINAE FELICES ADVANCED WEAPONS
	@Serializable
	data class ParticleClawLauncher(override val weapon: ShipWeapon.ParticleClawLauncher) : ShipWeaponInstance()
	
	@Serializable
	data class LightningYarn(override val weapon: ShipWeapon.LightningYarn) : ShipWeaponInstance()
	
	// HEAVY WEAPONS
	
	@Serializable
	data class MegaCannon(val remainingShots: Int) : ShipWeaponInstance() {
		override val weapon: ShipWeapon
			get() = ShipWeapon.MegaCannon
	}
	
	@Serializable
	data class RevelationGun(val remainingShots: Int) : ShipWeaponInstance() {
		override val weapon: ShipWeapon
			get() = ShipWeapon.RevelationGun
	}
	
	@Serializable
	data class EmpAntenna(val remainingShots: Int) : ShipWeaponInstance() {
		override val weapon: ShipWeapon
			get() = ShipWeapon.EmpAntenna
	}
}

typealias ShipArmaments = Map<Id<ShipWeapon>, ShipWeapon>

fun ShipArmaments.instantiate() = mapValues { (_, weapon) -> weapon.instantiate() }

typealias ShipInstanceArmaments = Map<Id<ShipWeapon>, ShipWeaponInstance>

fun cannonChanceToHit(attacker: ShipInstance, targeted: ShipInstance): Double {
	val relativeDistance = attacker.position.location - targeted.position.location
	return sqrt(SHIP_BASE_SIZE / relativeDistance.length) * attacker.firepower.cannonAccuracy
}

enum class DamageIgnoreType {
	FELINAE_ARMOR
}

sealed class ImpactDamage {
	abstract val amount: Int
	abstract val ignore: DamageIgnoreType?
	
	data class Success(override val amount: Int) : ImpactDamage() {
		override val ignore: DamageIgnoreType?
			get() = null
	}
	
	data class Failed(override val ignore: DamageIgnoreType) : ImpactDamage() {
		override val amount: Int
			get() = 0
	}
	
	object OtherEffect : ImpactDamage() {
		override val amount: Int
			get() = 0
		override val ignore: DamageIgnoreType?
			get() = null
	}
}

operator fun ImpactDamage.plus(amount: Int) = if (amount > 0) ImpactDamage.Success(this.amount + amount) else this

sealed class ImpactResult {
	data class Damaged(val ship: ShipInstance, val damage: ImpactDamage, val critical: CritResult = CritResult.NoEffect) : ImpactResult()
	data class Destroyed(val ship: ShipWreck) : ImpactResult()
	
	companion object {
		@Suppress("FunctionName")
		fun Intact(ship: ShipInstance, ignoreType: DamageIgnoreType? = null) = Damaged(ship, if (ignoreType == null) ImpactDamage.OtherEffect else ImpactDamage.Failed(ignoreType), CritResult.NoEffect)
	}
}

fun ShipInstance.felinaeArmorIgnoreDamageChance(): Double {
	if (ship.shipType.faction != Faction.FELINAE_FELICES) return 0.0
	
	val maxVelocity = movement.moveSpeed
	val curVelocity = currentVelocity
	val ratio = curVelocity / maxVelocity
	val exponent = ratio / sqrt(1 + abs(4 * ratio))
	return -expm1(-exponent)
}

fun ShipInstance.killTroops(damage: Int) = if (damage >= troopsAmount)
	CritResult.Destroyed(ShipWreck(ship, owner))
else CritResult.TroopsKilled(copy(troopsAmount = troopsAmount - damage), damage)

fun ShipInstance.impact(damage: Int, ignoreShields: Boolean = false) = if (durability is FelinaeShipDurability && Random.nextDouble() < felinaeArmorIgnoreDamageChance())
	ImpactResult.Intact(this, DamageIgnoreType.FELINAE_ARMOR)
else if (ignoreShields) {
	if (damage >= hullAmount)
		ImpactResult.Destroyed(ShipWreck(ship, owner))
	else ImpactResult.Damaged(copy(hullAmount = hullAmount - damage), damage = ImpactDamage.Success(damage))
} else if (damage > shieldAmount) {
	if (damage - shieldAmount >= hullAmount)
		ImpactResult.Destroyed(ShipWreck(ship, owner))
	else ImpactResult.Damaged(copy(shieldAmount = 0, hullAmount = hullAmount - (damage - shieldAmount)), damage = ImpactDamage.Success(damage))
} else ImpactResult.Damaged(copy(shieldAmount = shieldAmount - damage), damage = ImpactDamage.Success(damage))

@Serializable
data class ShipHangarWing(
	val ship: Id<ShipInstance>,
	val hangar: Id<ShipWeapon>
)

fun ShipInstance.afterUsing(weaponId: Id<ShipWeapon>) = when (val weapon = armaments.getValue(weaponId)) {
	is ShipWeaponInstance.Cannon -> {
		copy(weaponAmount = weaponAmount - 1, usedArmaments = usedArmaments + setOf(weaponId))
	}
	is ShipWeaponInstance.Lance -> {
		val newWeapons = armaments + mapOf(
			weaponId to weapon.copy(numCharges = 0.0)
		)
		
		copy(armaments = newWeapons, usedArmaments = usedArmaments + setOf(weaponId))
	}
	is ShipWeaponInstance.MegaCannon -> {
		val newWeapons = armaments + mapOf(
			weaponId to weapon.copy(remainingShots = weapon.remainingShots - 1)
		)
		
		copy(armaments = newWeapons, usedArmaments = usedArmaments + setOf(weaponId))
	}
	is ShipWeaponInstance.RevelationGun -> {
		val newWeapons = armaments + mapOf(
			weaponId to weapon.copy(remainingShots = weapon.remainingShots - 1)
		)
		
		copy(armaments = newWeapons, usedArmaments = usedArmaments + setOf(weaponId))
	}
	is ShipWeaponInstance.EmpAntenna -> {
		val newWeapons = armaments + mapOf(
			weaponId to weapon.copy(remainingShots = weapon.remainingShots - 1)
		)
		
		copy(armaments = newWeapons, usedArmaments = usedArmaments + setOf(weaponId))
	}
	else -> copy(usedArmaments = usedArmaments + setOf(weaponId))
}

fun ShipInstance.afterTargeted(by: ShipInstance, weaponId: Id<ShipWeapon>) = when (val weapon = by.armaments.getValue(weaponId)) {
	is ShipWeaponInstance.Cannon -> {
		var hits = 0
		
		repeat(weapon.weapon.numShots) {
			if (Random.nextDouble() < cannonChanceToHit(by, this))
				hits++
		}
		
		impact(hits).applyCriticals(by, weaponId)
	}
	is ShipWeaponInstance.Lance -> {
		var hits = 0
		
		repeat(weapon.weapon.numShots) {
			if (Random.nextDouble() < weapon.charge)
				hits++
		}
		
		impact(hits).applyCriticals(by, weaponId)
	}
	is ShipWeaponInstance.Torpedo -> {
		if (shieldAmount > 0) {
			if (Random.nextBoolean())
				impact(1).applyCriticals(by, weaponId)
			else
				ImpactResult.Damaged(this, ImpactDamage.Success(0))
		} else
			impact(2).applyCriticals(by, weaponId)
	}
	is ShipWeaponInstance.Hangar -> {
		ImpactResult.Damaged(
			if (weapon.weapon.wing == StrikeCraftWing.FIGHTERS)
				copy(fighterWings = fighterWings + setOf(ShipHangarWing(by.id, weaponId)))
			else
				copy(bomberWings = bomberWings + setOf(ShipHangarWing(by.id, weaponId))),
			ImpactDamage.OtherEffect
		)
	}
	is ShipWeaponInstance.ParticleClawLauncher -> {
		var impactResult = impact(weapon.weapon.numShots)
		
		repeat(weapon.weapon.numShots) {
			if (Random.nextDouble() < cannonChanceToHit(by, this))
				impactResult = when (val ir = impactResult) {
					is ImpactResult.Damaged -> ir.withCritResult(ir.ship.doCriticalDamage())
					else -> ir
				}
		}
		
		impactResult
	}
	is ShipWeaponInstance.LightningYarn -> {
		impact(weapon.weapon.numShots, true).applyCriticals(by, weaponId)
	}
	is ShipWeaponInstance.MegaCannon -> {
		impact((3..7).random()).applyCriticals(by, weaponId)
	}
	is ShipWeaponInstance.RevelationGun -> {
		ImpactResult.Destroyed(ShipWreck(ship, owner))
	}
	is ShipWeaponInstance.EmpAntenna -> {
		ImpactResult.Damaged(
			copy(
				weaponAmount = (0..weaponAmount).random(),
				shieldAmount = (0..shieldAmount).random(),
			),
			ImpactDamage.Success(0)
		)
	}
}

fun ShipInstance.calculateBombing(otherShips: Map<Id<ShipInstance>, ShipInstance>, extraBombers: Double = 0.0, extraFighters: Double = 0.0): Double {
	if (bomberWings.isEmpty() && extraBombers < EPSILON)
		return 0.0
	
	val totalFighterHealth = fighterWings.sumOf { (carrierId, wingId) ->
		(otherShips[carrierId]?.armaments?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
	} + durability.turretDefense + extraFighters
	
	val totalBomberHealth = bomberWings.sumOf { (carrierId, wingId) ->
		(otherShips[carrierId]?.armaments?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
	} + extraBombers
	
	if (totalBomberHealth < EPSILON)
		return 0.0
	
	return totalBomberHealth - totalFighterHealth
}

fun ShipInstance.afterBombed(otherShips: Map<Id<ShipInstance>, ShipInstance>, strikeWingDamage: MutableMap<ShipHangarWing, Double>): ImpactResult {
	if (bomberWings.isEmpty())
		return ImpactResult.Intact(this)
	
	val calculatedBombing = calculateBombing(otherShips)
	
	val maxBomberWingOutput = exp(calculatedBombing)
	val maxFighterWingOutput = exp(-calculatedBombing)
	
	for (it in fighterWings)
		strikeWingDamage[it] = Random.nextDouble() * maxBomberWingOutput
	
	for (it in bomberWings)
		strikeWingDamage[it] = Random.nextDouble() * maxFighterWingOutput
	
	val chanceOfShipDamage = smoothNegative(calculatedBombing)
	val hits = floor(chanceOfShipDamage).let { floored ->
		floored.roundToInt() + (if (Random.nextDouble() < chanceOfShipDamage - floored) 1 else 0)
	}
	
	val criticalChance = smoothMinus1To1(chanceOfShipDamage, exponent = 0.5)
	return impact(hits).applyStrikeCraftCriticals(criticalChance)
}

fun ShipInstance.afterBombing(strikeWingDamage: Map<ShipHangarWing, Double>): ShipInstance {
	val newArmaments = armaments.mapValues { (weaponId, weapon) ->
		if (weapon is ShipWeaponInstance.Hangar)
			weapon.copy(wingHealth = weapon.wingHealth - (strikeWingDamage[ShipHangarWing(id, weaponId)] ?: 0.0))
		else weapon
	}.filterValues { it !is ShipWeaponInstance.Hangar || it.wingHealth > 0.0 }
	
	return copy(armaments = newArmaments)
}

fun ImpactResult.Damaged.withCritResult(critical: CritResult): ImpactResult = when (critical) {
	is CritResult.NoEffect -> this
	is CritResult.FireStarted -> copy(
		ship = critical.ship,
		damage = damage,
		critical = critical
	)
	is CritResult.ModulesDisabled -> copy(
		ship = critical.ship,
		damage = damage,
		critical = critical
	)
	is CritResult.TroopsKilled -> copy(
		ship = critical.ship,
		damage = damage,
		critical = critical
	)
	is CritResult.HullDamaged -> copy(
		ship = critical.ship,
		damage = damage + critical.amount,
		critical = critical
	)
	is CritResult.Destroyed -> ImpactResult.Destroyed(critical.ship)
}

fun ImpactResult.applyCriticals(attacker: ShipInstance, weaponId: Id<ShipWeapon>): ImpactResult {
	return when (this) {
		is ImpactResult.Destroyed -> this
		is ImpactResult.Damaged -> {
			if (damage is ImpactDamage.Failed)
				return this
			
			val critChance = criticalChance(attacker, weaponId, ship)
			if (Random.nextDouble() > critChance)
				this
			else
				withCritResult(ship.doCriticalDamage())
		}
	}
}

fun ImpactResult.applyStrikeCraftCriticals(criticalChance: Double): ImpactResult {
	return when (this) {
		is ImpactResult.Destroyed -> this
		is ImpactResult.Damaged -> {
			if (Random.nextDouble() > criticalChance)
				this
			else
				withCritResult(ship.doCriticalDamage())
		}
	}
}

fun criticalChance(attacker: ShipInstance, weaponId: Id<ShipWeapon>, targeted: ShipInstance): Double {
	val targetHasShields = targeted.canUseShields && targeted.shieldAmount > 0
	val weapon = attacker.armaments[weaponId] ?: return 0.0
	
	return when (weapon) {
		is ShipWeaponInstance.Torpedo -> if (targetHasShields) 0.0 else 0.375
		is ShipWeaponInstance.Hangar -> 0.0 // implemented elsewhere
		is ShipWeaponInstance.MegaCannon -> 0.5
		else -> if (targetHasShields) 0.125 else 0.25
	} * attacker.firepower.criticalChance
}

fun ShipInstance.getWeaponPickRequest(weapon: ShipWeapon): PickRequest = when (weapon) {
	is AreaWeapon -> PickRequest(
		type = PickType.Location(
			excludesNearShips = emptySet(),
			helper = PickHelper.Circle(radius = weapon.areaRadius),
			drawLineFrom = if (weapon.isLine) null else position.location
		),
		boundary = if (weapon.isLine)
			PickBoundary.AlongLine(
				pointA = position.location + polarDistance(weapon.minRange, position.facing),
				pointB = position.location + polarDistance(weapon.maxRange, position.facing)
			)
		else
			PickBoundary.WeaponsFire(
				center = position.location,
				facing = position.facing,
				minDistance = weapon.minRange,
				maxDistance = weapon.maxRange,
				firingArcs = weapon.firingArcs,
			),
	)
	else -> {
		val targetSet = if ((weapon as? ShipWeapon.Hangar)?.wing == StrikeCraftWing.FIGHTERS)
			setOf(owner.side)
		else
			setOf(owner.side.other)
		
		val weaponRangeMult = when (weapon) {
			is ShipWeapon.Cannon -> firepower.rangeMultiplier
			is ShipWeapon.Lance -> firepower.rangeMultiplier
			is ShipWeapon.ParticleClawLauncher -> firepower.rangeMultiplier
			is ShipWeapon.LightningYarn -> firepower.rangeMultiplier
			else -> 1.0
		}
		
		PickRequest(
			PickType.Ship(targetSet),
			PickBoundary.WeaponsFire(
				center = position.location,
				facing = position.facing,
				minDistance = weapon.minRange,
				maxDistance = weapon.maxRange * weaponRangeMult,
				firingArcs = weapon.firingArcs,
				canSelfSelect = owner.side in targetSet
			)
		)
	}
}

fun ImpactResult.toChatEntry(attacker: ShipAttacker, weapon: ShipWeaponInstance?) = when (this) {
	is ImpactResult.Damaged -> when (damage) {
		is ImpactDamage.Success -> ChatEntry.ShipAttacked(
			ship = ship.id,
			attacker = attacker,
			sentAt = Moment.now,
			damageInflicted = damage.amount,
			weapon = weapon?.weapon,
			critical = critical.report(),
		)
		is ImpactDamage.Failed -> ChatEntry.ShipAttackFailed(
			ship = ship.id,
			attacker = attacker,
			sentAt = Moment.now,
			weapon = weapon?.weapon,
			damageIgnoreType = damage.ignore
		)
		else -> null
	}
	is ImpactResult.Destroyed -> {
		ChatEntry.ShipDestroyed(
			ship = ship.id,
			sentAt = Moment.now,
			destroyedBy = attacker,
		)
	}
}

fun GameState.useWeaponPickResponse(attacker: ShipInstance, weaponId: Id<ShipWeapon>, target: PickResponse): GameEvent {
	val weapon = attacker.armaments[weaponId] ?: return GameEvent.InvalidAction("That weapon does not exist")
	
	return when (val weaponType = weapon.weapon) {
		is AreaWeapon -> {
			val targetedLocation = (target as? PickResponse.Location)?.position ?: return GameEvent.InvalidAction("Invalid pick response type")
			val targetedShips = ships.filterValues { (it.position.location - targetedLocation).length < weaponType.areaRadius }
			
			if (targetedShips.isEmpty()) return GameEvent.InvalidAction("No ships targeted - aborting fire")
			
			val newAttacker = attacker.afterUsing(weaponId)
			
			val impacts = targetedShips.mapValues { (_, ship) ->
				ship.afterTargeted(attacker, weaponId)
			}
			
			val newShips = ships.filterKeys { id ->
				id !in impacts
			} + impacts.mapNotNull { (id, impact) ->
				(impact as? ImpactResult.Damaged)?.ship?.let { id to it }
			}.toMap() + mapOf(attacker.id to newAttacker)
			
			val newWrecks = destroyedShips + impacts.mapNotNull { (id, impact) ->
				(impact as? ImpactResult.Destroyed)?.ship?.let { id to it }
			}.toMap()
			
			val newChatMessages = chatBox + impacts.mapNotNull { (_, impact) ->
				impact.toChatEntry(ShipAttacker.EnemyShip(newAttacker.id), weapon)
			}
			
			GameEvent.StateChange(
				copy(ships = newShips, destroyedShips = newWrecks, chatBox = newChatMessages)
					.withRecalculatedInitiative { calculateAttackPhaseInitiative() }
			)
		}
		else -> {
			val targetedShipId = (target as? PickResponse.Ship)?.id ?: return GameEvent.InvalidAction("Invalid pick response type")
			val targetedShip = ships[targetedShipId] ?: return GameEvent.InvalidAction("That ship does not exist")
			
			val impact = targetedShip.afterTargeted(attacker, weaponId)
			
			val newAttacker = if (targetedShipId == attacker.id) {
				if (impact is ImpactResult.Damaged)
					impact.ship.afterUsing(weaponId)
				else
					null
			} else
				attacker.afterUsing(weaponId)
			
			val newShips = (if (impact is ImpactResult.Damaged)
				ships + mapOf(targetedShipId to impact.ship)
			else ships - targetedShipId) + (if (newAttacker != null)
				mapOf(attacker.id to newAttacker)
			else emptyMap())
			
			val newWrecks = destroyedShips + if (impact is ImpactResult.Destroyed)
				mapOf(targetedShipId to impact.ship)
			else emptyMap()
			
			val newChatMessages = chatBox + listOfNotNull(
				impact.toChatEntry(ShipAttacker.EnemyShip(attacker.id), weapon)
			)
			
			GameEvent.StateChange(
				copy(ships = newShips, destroyedShips = newWrecks, chatBox = newChatMessages)
					.withRecalculatedInitiative { calculateAttackPhaseInitiative() }
			)
		}
	}
}

val ShipWeapon.displayName: String
	get() {
		val firingArcsDesc = when (firingArcs) {
			FiringArc.FIRE_360 -> "360-Degree "
			FiringArc.FIRE_BROADSIDE -> "Broadside "
			FiringArc.FIRE_FORE_270 -> "Dorsal "
			setOf(FiringArc.ABEAM_PORT) -> "Port "
			setOf(FiringArc.ABEAM_STARBOARD) -> "Starboard "
			setOf(FiringArc.BOW) -> "Fore "
			setOf(FiringArc.STERN) -> "Rear "
			setOf(FiringArc.BOW, FiringArc.ABEAM_PORT) -> "Wide-Angle Port "
			setOf(FiringArc.BOW, FiringArc.ABEAM_STARBOARD) -> "Wide-Angle Starboard "
			else -> null
		}.takeIf { this !is ShipWeapon.Hangar } ?: ""
		
		val weaponIsPlural = numShots > 1
		
		val weaponDesc = when (this) {
			is ShipWeapon.Cannon -> "Cannon" + (if (weaponIsPlural) "s" else "")
			is ShipWeapon.Lance -> "Lance" + (if (weaponIsPlural) "s" else "")
			is ShipWeapon.Hangar -> when (wing) {
				StrikeCraftWing.FIGHTERS -> "Fighters"
				StrikeCraftWing.BOMBERS -> "Bombers"
			}
			is ShipWeapon.Torpedo -> "Torpedo" + (if (weaponIsPlural) "es" else "")
			is ShipWeapon.ParticleClawLauncher -> "Particle Claw" + (if (weaponIsPlural) "s" else "")
			is ShipWeapon.LightningYarn -> "Lightning Yarn"
			is ShipWeapon.MegaCannon -> "Mega Giga Cannon"
			is ShipWeapon.RevelationGun -> "Revelation Gun"
			is ShipWeapon.EmpAntenna -> "EMP Antenna"
		}
		
		return "$firingArcsDesc$weaponDesc"
	}

val ShipWeaponInstance.displayName: String
	get() {
		val weaponParam = when (this) {
			is ShipWeaponInstance.Lance -> " (${charge.toPercent()})"
			is ShipWeaponInstance.Hangar -> " (${wingHealth.toPercent()})"
			is ShipWeaponInstance.MegaCannon -> " ($remainingShots)"
			is ShipWeaponInstance.RevelationGun -> " ($remainingShots)"
			is ShipWeaponInstance.EmpAntenna -> " ($remainingShots)"
			else -> ""
		}
		
		return "${weapon.displayName}$weaponParam"
	}
