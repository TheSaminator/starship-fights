package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id
import kotlin.math.expm1
import kotlin.random.Random

enum class FiringArc {
	BOW, ABEAM_PORT, ABEAM_STARBOARD, STERN;
	
	val displayName: String
		get() = when (this) {
			BOW -> "Bow"
			ABEAM_PORT -> "Port"
			ABEAM_STARBOARD -> "Starboard"
			STERN -> "Stern"
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
		
		override fun instantiate() = ShipWeaponInstance.Lance(this, 10)
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
			get() = 76
		
		override fun instantiate() = ShipWeaponInstance.RevelationGun(numShots)
	}
	
	@Serializable
	object EmpAntenna : ShipWeapon(), AreaWeapon {
		override val numShots: Int
			get() = 4
		
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
	data class Lance(override val weapon: ShipWeapon.Lance, val numCharges: Int) : ShipWeaponInstance() {
		val charge: Double
			get() = -expm1(-numCharges.toDouble())
	}
	
	@Serializable
	data class Torpedo(override val weapon: ShipWeapon.Torpedo) : ShipWeaponInstance()
	
	@Serializable
	data class Hangar(override val weapon: ShipWeapon.Hangar, val wingHealth: Double) : ShipWeaponInstance()
	
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

@Serializable
data class ShipArmaments(
	val weapons: Map<Id<ShipWeapon>, ShipWeapon>
) {
	fun instantiate() = ShipInstanceArmaments(weapons.mapValues { (_, weapon) -> weapon.instantiate() })
}

@Serializable
data class ShipInstanceArmaments(
	val weaponInstances: Map<Id<ShipWeapon>, ShipWeaponInstance>
)

fun cannonChanceToHit(attacker: ShipInstance, targeted: ShipInstance): Double {
	val relativeDistance = attacker.position.location - targeted.position.location
	return SHIP_BASE_SIZE / relativeDistance.length
}

sealed class ImpactResult {
	data class Damaged(val ship: ShipInstance, val amount: Int? = null, val critical: CritResult = CritResult.NoEffect) : ImpactResult()
	data class Destroyed(val ship: ShipWreck) : ImpactResult()
}

fun ShipInstance.impact(damage: Int) = if (damage > shieldAmount) {
	if (damage - shieldAmount >= hullAmount)
		ImpactResult.Destroyed(ShipWreck(ship, owner))
	else ImpactResult.Damaged(copy(shieldAmount = 0, hullAmount = hullAmount - (damage - shieldAmount)), amount = damage)
} else ImpactResult.Damaged(copy(shieldAmount = shieldAmount - damage), amount = damage)

@Serializable
data class ShipHangarWing(
	val ship: Id<ShipInstance>,
	val hangar: Id<ShipWeapon>
)

fun ShipInstance.afterUsing(weaponId: Id<ShipWeapon>) = when (val weapon = armaments.weaponInstances.getValue(weaponId)) {
	is ShipWeaponInstance.Cannon -> {
		copy(weaponAmount = weaponAmount - 1, usedArmaments = usedArmaments + setOf(weaponId))
	}
	is ShipWeaponInstance.Lance -> {
		val newWeapons = armaments.weaponInstances + mapOf(
			weaponId to weapon.copy(numCharges = 0)
		)
		
		copy(armaments = ShipInstanceArmaments(newWeapons), usedArmaments = usedArmaments + setOf(weaponId))
	}
	is ShipWeaponInstance.MegaCannon -> {
		val newWeapons = armaments.weaponInstances + mapOf(
			weaponId to weapon.copy(remainingShots = weapon.remainingShots - 1)
		)
		
		copy(armaments = ShipInstanceArmaments(newWeapons), usedArmaments = usedArmaments + setOf(weaponId))
	}
	is ShipWeaponInstance.RevelationGun -> {
		val newWeapons = armaments.weaponInstances + mapOf(
			weaponId to weapon.copy(remainingShots = weapon.remainingShots - 1)
		)
		
		copy(armaments = ShipInstanceArmaments(newWeapons), usedArmaments = usedArmaments + setOf(weaponId))
	}
	is ShipWeaponInstance.EmpAntenna -> {
		val newWeapons = armaments.weaponInstances + mapOf(
			weaponId to weapon.copy(remainingShots = weapon.remainingShots - 1)
		)
		
		copy(armaments = ShipInstanceArmaments(newWeapons), usedArmaments = usedArmaments + setOf(weaponId))
	}
	else -> copy(usedArmaments = usedArmaments + setOf(weaponId))
}

fun ShipInstance.afterTargeted(by: ShipInstance, weaponId: Id<ShipWeapon>) = when (val weapon = by.armaments.weaponInstances.getValue(weaponId)) {
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
				ImpactResult.Damaged(this, 0)
		} else
			impact(2).applyCriticals(by, weaponId)
	}
	is ShipWeaponInstance.Hangar -> {
		ImpactResult.Damaged(
			if (weapon.weapon.wing == StrikeCraftWing.FIGHTERS)
				copy(fighterWings = fighterWings + setOf(ShipHangarWing(by.id, weaponId)))
			else
				copy(bomberWings = bomberWings + setOf(ShipHangarWing(by.id, weaponId)))
		)
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
			amount = 0
		)
	}
}

fun ImpactResult.Damaged.withCritResult(critical: CritResult): ImpactResult = when (critical) {
	is CritResult.NoEffect -> this
	is CritResult.FireStarted -> copy(
		ship = critical.ship,
		amount = amount,
		critical = critical
	)
	is CritResult.ModulesDisabled -> copy(
		ship = critical.ship,
		amount = amount,
		critical = critical
	)
	is CritResult.HullDamaged -> copy(
		ship = critical.ship,
		amount = amount?.let { it + critical.amount },
		critical = critical
	)
	is CritResult.Destroyed -> ImpactResult.Destroyed(critical.ship)
}

fun ImpactResult.applyCriticals(attacker: ShipInstance, weaponId: Id<ShipWeapon>): ImpactResult {
	return when (this) {
		is ImpactResult.Destroyed -> this
		is ImpactResult.Damaged -> {
			val critChance = criticalChance(attacker, weaponId, ship)
			if (Random.nextDouble() > critChance)
				this
			else
				withCritResult(ship.doCriticalDamage())
		}
	}
}

fun criticalChance(attacker: ShipInstance, weaponId: Id<ShipWeapon>, targeted: ShipInstance): Double {
	val targetHasShields = targeted.canUseShields && targeted.shieldAmount > 0
	val weapon = attacker.armaments.weaponInstances[weaponId] ?: return 0.0
	
	return when (weapon) {
		is ShipWeaponInstance.Torpedo -> if (targetHasShields) 0.0 else 0.375
		is ShipWeaponInstance.Hangar -> 0.0
		is ShipWeaponInstance.MegaCannon -> 0.5
		else -> if (targetHasShields) 0.125 else 0.25
	}
}

fun getWeaponPickRequest(weapon: ShipWeapon, position: ShipPosition, side: GlobalSide): PickRequest = when (weapon) {
	is AreaWeapon -> PickRequest(
		type = PickType.Location(
			excludesNearShips = emptySet(),
			helper = PickHelper.Circle(radius = weapon.areaRadius),
			drawLineFrom = if (weapon.isLine) null else position.location
		),
		boundary = if (weapon.isLine)
			PickBoundary.AlongLine(
				pointA = position.location + (normalDistance(position.facing) * weapon.minRange),
				pointB = position.location + (normalDistance(position.facing) * weapon.maxRange)
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
			setOf(side)
		else
			setOf(side.other)
		
		PickRequest(
			PickType.Ship(targetSet),
			PickBoundary.WeaponsFire(
				center = position.location,
				facing = position.facing,
				minDistance = weapon.minRange,
				maxDistance = weapon.maxRange,
				firingArcs = weapon.firingArcs,
				canSelfSelect = side in targetSet
			)
		)
	}
}

fun GameState.useWeaponPickResponse(attacker: ShipInstance, weaponId: Id<ShipWeapon>, target: PickResponse): GameEvent {
	val weapon = attacker.armaments.weaponInstances[weaponId] ?: return GameEvent.InvalidAction("That weapon does not exist")
	
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
				when (impact) {
					is ImpactResult.Damaged -> impact.amount?.let { damage ->
						ChatEntry.ShipAttacked(
							impact.ship.id,
							ShipAttacker.EnemyShip(newAttacker.id),
							Moment.now,
							damage,
							weapon.weapon,
							impact.critical.report(),
						)
					}
					is ImpactResult.Destroyed -> {
						ChatEntry.ShipDestroyed(
							impact.ship.id,
							Moment.now,
							ShipAttacker.EnemyShip(newAttacker.id)
						)
					}
				}
			}
			
			GameEvent.StateChange(
				copy(ships = newShips, destroyedShips = newWrecks, chatBox = newChatMessages)
			)
		}
		else -> {
			val targetedShipId = (target as? PickResponse.Ship)?.id ?: return GameEvent.InvalidAction("Invalid pick response type")
			val targetedShip = ships[targetedShipId] ?: return GameEvent.InvalidAction("That ship does not exist")
			
			val impact = targetedShip.afterTargeted(attacker, weaponId)
			val newAttacker = attacker.afterUsing(weaponId)
			
			val newShips = (if (impact is ImpactResult.Damaged)
				ships + mapOf(targetedShipId to impact.ship)
			else ships - targetedShipId) + mapOf(attacker.id to newAttacker)
			
			val newWrecks = destroyedShips + if (impact is ImpactResult.Destroyed)
				mapOf(targetedShipId to impact.ship)
			else emptyMap()
			
			val newChatMessages = chatBox + listOfNotNull(
				when (impact) {
					is ImpactResult.Destroyed -> ChatEntry.ShipDestroyed(
						impact.ship.id,
						Moment.now,
						ShipAttacker.EnemyShip(newAttacker.id)
					)
					is ImpactResult.Damaged -> impact.amount?.let { damage ->
						ChatEntry.ShipAttacked(
							impact.ship.id,
							ShipAttacker.EnemyShip(newAttacker.id),
							Moment.now,
							damage,
							weapon.weapon,
							impact.critical.report(),
						)
					}
				}
			)
			
			GameEvent.StateChange(
				copy(ships = newShips, destroyedShips = newWrecks, chatBox = newChatMessages)
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
