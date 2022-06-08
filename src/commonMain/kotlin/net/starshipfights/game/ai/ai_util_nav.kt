package net.starshipfights.game.ai

import net.starshipfights.data.Id
import net.starshipfights.game.*
import kotlin.math.expm1
import kotlin.math.pow

val navAggression by instinct(0.5..1.5)
val navPassivity by instinct(0.5..1.5)
val navLustForBlood by instinct(-0.5..0.5)
val navSqueamishness by instinct(0.25..1.25)
val navTunnelVision by instinct(-0.25..1.25)
val navOptimality by instinct(1.25..2.75)

fun ShipPosition.score(gameState: GameState, shipInstance: ShipInstance, instincts: Instincts, brain: Brain): Double {
	val ship = shipInstance.copy(position = this)
	
	val canAttack = ship.canAttackWithDamage(gameState)
	val canBeAttackedBy = ship.attackableWithDamageBy(gameState)
	
	val opportunityScore = canAttack.map { (targetId, potentialDamage) ->
		smoothNegative(brain[shipAttackPriority forShip targetId]).signedPow(instincts[navTunnelVision]) * potentialDamage
	}.sum() + (ship.calculateSuffering() * instincts[navLustForBlood])
	
	val vulnerabilityScore = canBeAttackedBy.map { (targetId, potentialDamage) ->
		smoothNegative(brain[shipAttackPriority forShip targetId]).signedPow(instincts[navTunnelVision]) * potentialDamage
	}.sum() * -expm1(-ship.calculateSuffering() * instincts[navSqueamishness])
	
	return instincts[navOptimality].pow(opportunityScore.signedPow(instincts[navAggression]) - vulnerabilityScore.signedPow(instincts[navPassivity]))
}

fun ShipInstance.canAttackWithDamage(gameState: GameState): Map<Id<ShipInstance>, Double> {
	return attackableTargets(gameState).mapValues { (targetId, weapons) ->
		val target = gameState.ships[targetId] ?: return@mapValues 0.0
		
		weapons.sumOf { weaponId ->
			weaponId.expectedAdvantageFromWeaponUsage(gameState, this, target)
		}
	}
}

fun ShipInstance.attackableTargets(gameState: GameState): Map<Id<ShipInstance>, Set<Id<ShipWeapon>>> {
	return armaments.keys.associateWith { weaponId ->
		weaponId.validTargets(gameState, this).map { it.id }.toSet()
	}.transpose()
}

fun ShipInstance.attackableWithDamageBy(gameState: GameState): Map<Id<ShipInstance>, Double> {
	return gameState.getValidAttackersWith(this).mapValues { (attackerId, weapons) ->
		val attacker = gameState.ships[attackerId] ?: return@mapValues 0.0
		
		weapons.sumOf { weaponId ->
			weaponId.expectedAdvantageFromWeaponUsage(gameState, attacker, this)
		}
	}
}

fun Id<ShipWeapon>.validTargets(gameState: GameState, ship: ShipInstance): List<ShipInstance> {
	if (!ship.canUseWeapon(this)) return emptyList()
	val weaponInstance = ship.armaments[this] ?: return emptyList()
	
	return gameState.getValidTargets(ship, weaponInstance)
}

fun Id<ShipWeapon>.expectedAdvantageFromWeaponUsage(gameState: GameState, ship: ShipInstance, target: ShipInstance): Double {
	if (!ship.canUseWeapon(this)) return 0.0
	val weaponInstance = ship.armaments[this] ?: return 0.0
	val mustBeSameSide = weaponInstance is ShipWeaponInstance.Hangar && weaponInstance.weapon.wing == StrikeCraftWing.FIGHTERS
	if ((ship.owner == target.owner) != mustBeSameSide) return 0.0
	
	return when (weaponInstance) {
		is ShipWeaponInstance.Cannon -> cannonChanceToHit(ship, target) * weaponInstance.weapon.numShots
		is ShipWeaponInstance.Lance -> weaponInstance.charge * weaponInstance.weapon.numShots
		is ShipWeaponInstance.Torpedo -> if (target.shieldAmount > 0) 0.5 else 2.0
		is ShipWeaponInstance.Hangar -> when (weaponInstance.weapon.wing) {
			StrikeCraftWing.BOMBERS -> {
				val calculatedPrevBombing = target.calculateBombing(gameState.ships) ?: 0.0
				val calculatedNextBombing = target.calculateBombing(gameState.ships, extraBombers = weaponInstance.wingHealth) ?: 0.0
				
				calculateShipDamageChanceFromBombing(calculatedNextBombing) - calculateShipDamageChanceFromBombing(calculatedPrevBombing)
			}
			StrikeCraftWing.FIGHTERS -> {
				val calculatedPrevBombing = target.calculateBombing(gameState.ships) ?: 0.0
				val calculatedNextBombing = target.calculateBombing(gameState.ships, extraFighters = weaponInstance.wingHealth) ?: 0.0
				
				calculateShipDamageChanceFromBombing(calculatedPrevBombing) - calculateShipDamageChanceFromBombing(calculatedNextBombing)
			}
		}
		is ShipWeaponInstance.ParticleClawLauncher -> (cannonChanceToHit(ship, target) + 1) * weaponInstance.weapon.numShots
		is ShipWeaponInstance.LightningYarn -> weaponInstance.weapon.numShots.toDouble()
		is ShipWeaponInstance.MegaCannon -> 5.0
		is ShipWeaponInstance.RevelationGun -> (target.shieldAmount + target.hullAmount).toDouble()
		is ShipWeaponInstance.EmpAntenna -> target.shieldAmount * 0.5
	}
}

private fun calculateShipDamageChanceFromBombing(calculatedBombing: Double): Double {
	val maxBomberWingOutput = smoothNegative(calculatedBombing)
	val maxFighterWingOutput = smoothNegative(-calculatedBombing)
	
	return smoothNegative(maxBomberWingOutput - maxFighterWingOutput)
}

fun ShipInstance.navigateTo(targetLocation: Position): PlayerAction.UseAbility {
	val myLocation = position.location
	
	val angleTo = normalDistance(position.facing) angleTo (targetLocation - myLocation)
	val maxTurn = movement.turnAngle * 0.99
	val turnNormal = normalDistance(position.facing) rotatedBy angleTo.coerceIn(-maxTurn..maxTurn)
	
	val move = (movement.moveSpeed * if (turnNormal angleBetween (targetLocation - myLocation) < EPSILON) 0.99 else 0.51) * turnNormal
	val newLoc = position.location + move
	
	val position = ShipPosition(newLoc, move.angle)
	
	return PlayerAction.UseAbility(
		PlayerAbilityType.MoveShip(id),
		PlayerAbilityData.MoveShip(position)
	)
}
