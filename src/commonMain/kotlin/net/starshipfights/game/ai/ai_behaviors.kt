package net.starshipfights.game.ai

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.produceIn
import net.starshipfights.data.Id
import net.starshipfights.game.*
import kotlin.math.pow
import kotlin.random.Random

data class AIPlayer(
	val gameState: StateFlow<GameState>,
	val doActions: SendChannel<PlayerAction>,
	val getErrors: ReceiveChannel<String>,
	val onGameEnd: CompletableJob
)

@OptIn(FlowPreview::class)
suspend fun AIPlayer.behave(instincts: Instincts, mySide: GlobalSide) {
	try {
		coroutineScope {
			val brain = Brain()
			
			val phasePipe = Channel<Pair<GamePhase, Boolean>>(Channel.CONFLATED)
			
			launch(onGameEnd) {
				var prevSentAt = Moment.now
				
				for (state in gameState.produceIn(this)) {
					phasePipe.send(state.phase to (state.doneWithPhase != mySide && (!state.phase.usesInitiative || state.currentInitiative != mySide.other)))
					
					for (msg in state.chatBox.takeLastWhile { msg -> msg.sentAt > prevSentAt }) {
						if (msg.sentAt > prevSentAt)
							prevSentAt = msg.sentAt
						
						when (msg) {
							is ChatEntry.PlayerMessage -> {
								// ignore
							}
							is ChatEntry.ShipIdentified -> {
								val identifiedShip = state.ships[msg.ship] ?: continue
								if (identifiedShip.owner != mySide)
									brain[shipAttackPriority forShip identifiedShip.id] += identifiedShip.ship.shipType.weightClass.tier.toDouble().pow(instincts[combatTargetShipWeight])
							}
							is ChatEntry.ShipEscaped -> {
								// handle escaping ship
							}
							is ChatEntry.ShipAttacked -> {
								val targetedShip = state.ships[msg.ship] ?: continue
								if (targetedShip.owner != mySide)
									brain[shipAttackPriority forShip targetedShip.id] -= Random.nextDouble(msg.damageInflicted - 0.5, msg.damageInflicted + 0.5) * instincts[combatForgiveTarget]
								else if (msg.attacker is ShipAttacker.EnemyShip)
									brain[shipAttackPriority forShip msg.attacker.id] += Random.nextDouble(msg.damageInflicted - 0.5, msg.damageInflicted + 0.5) * instincts[combatAvengeAttacks]
							}
							is ChatEntry.ShipAttackFailed -> {
								val targetedShip = state.ships[msg.ship] ?: continue
								if (targetedShip.owner != mySide)
									brain[shipAttackPriority forShip targetedShip.id] += instincts[combatFrustratedByFailedAttacks]
							}
							is ChatEntry.ShipBoarded -> {
								val targetedShip = state.ships[msg.ship] ?: continue
								if (targetedShip.owner != mySide)
									brain[shipAttackPriority forShip targetedShip.id] -= Random.nextDouble(msg.damageAmount - 0.5, msg.damageAmount + 0.5) * instincts[combatForgiveTarget]
								else
									brain[shipAttackPriority forShip msg.boarder] += Random.nextDouble(msg.damageAmount - 0.5, msg.damageAmount + 0.5) * instincts[combatAvengeAttacks]
							}
							is ChatEntry.ShipDestroyed -> {
								val targetedShip = state.ships[msg.ship] ?: continue
								if (targetedShip.owner == mySide && msg.destroyedBy is ShipAttacker.EnemyShip)
									brain[shipAttackPriority forShip msg.destroyedBy.id] += instincts[combatAvengeShipwrecks] * targetedShip.ship.shipType.weightClass.tier.toDouble().pow(instincts[combatAvengeShipWeight])
							}
						}
					}
				}
			}
			
			launch(onGameEnd) {
				loop@ for ((phase, canAct) in phasePipe) {
					if (!canAct) continue@loop
					
					val state = gameState.value
					
					when (phase) {
						GamePhase.Deploy -> {
							for ((shipId, position) in deploy(state, mySide, instincts)) {
								val abilityType = PlayerAbilityType.DeployShip(shipId.reinterpret())
								val abilityData = PlayerAbilityData.DeployShip(position)
								
								doActions.send(PlayerAction.UseAbility(abilityType, abilityData))
								
								withTimeoutOrNull(50L) { getErrors.receive() }?.let { errorMsg ->
									logWarning("Error when deploying ship ID $shipId - $errorMsg")
								}
							}
							
							doActions.send(PlayerAction.UseAbility(PlayerAbilityType.DonePhase(phase), PlayerAbilityData.DonePhase))
						}
						is GamePhase.Power -> {
							val powerableShips = state.ships.values.filter { ship ->
								ship.owner == mySide && !ship.isDoneCurrentPhase
							}
							
							for (ship in powerableShips)
								when (val reactor = ship.ship.reactor) {
									FelinaeShipReactor -> {
										val newPowerMode = if (ship.hullAmount < ship.durability.maxHullPoints)
											FelinaeShipPowerMode.HULL_RECOALESCENSE
										else
											FelinaeShipPowerMode.INERTIALESS_DRIVE
										
										doActions.send(PlayerAction.UseAbility(PlayerAbilityType.ConfigurePower(ship.id, newPowerMode), PlayerAbilityData.ConfigurePower))
									}
									is StandardShipReactor -> {
										val enginesToShields = when {
											ship.powerMode.engines == 0 -> -1
											ship.shieldAmount == 0 -> 2
											ship.shieldAmount < (ship.powerMode.shields / 2) -> 1
											ship.shieldAmount < ship.powerMode.shields -> (0..1).random()
											else -> 0
										}.coerceIn(-reactor.gridEfficiency..reactor.gridEfficiency)
										
										val currPower = ship.powerMode
										val nextPower = currPower + mapOf(
											ShipSubsystem.SHIELDS to enginesToShields,
											ShipSubsystem.ENGINES to -enginesToShields
										)
										
										val chosenPower = if (ship.validatePowerMode(nextPower)) nextPower else currPower
										doActions.send(PlayerAction.UseAbility(PlayerAbilityType.DistributePower(ship.id), PlayerAbilityData.DistributePower(chosenPower)))
									}
								}
							
							doActions.send(PlayerAction.UseAbility(PlayerAbilityType.DonePhase(phase), PlayerAbilityData.DonePhase))
						}
						is GamePhase.Move -> {
							val movableShips = state.ships.values.filter { ship ->
								ship.owner == mySide && !ship.isDoneCurrentPhase
							}
							
							val smallestShipTier = movableShips.minOfOrNull { ship -> ship.ship.shipType.weightClass.tier }
							
							if (smallestShipTier == null) {
								doActions.send(PlayerAction.UseAbility(PlayerAbilityType.DonePhase(phase), PlayerAbilityData.DonePhase))
								continue@loop
							}
							
							val movableSmallestShips = movableShips.filter { ship ->
								ship.ship.shipType.weightClass.tier == smallestShipTier
							}
							
							val moveThisShip = movableSmallestShips.associateWith { it.calculateSuffering() + 1.0 }.weightedRandom()
							doActions.send(navigate(state, moveThisShip, instincts, brain))
							
							withTimeoutOrNull(50L) { getErrors.receive() }?.let { error ->
								logWarning("Error when moving ship ID ${moveThisShip.id} - $error")
								doActions.send(
									PlayerAction.UseAbility(
										PlayerAbilityType.MoveShip(moveThisShip.id),
										PlayerAbilityData.MoveShip(moveThisShip.position)
									)
								)
							}
						}
						is GamePhase.Attack -> {
							val potentialAttacks = state.ships.values.flatMap { ship ->
								if (ship.owner == mySide)
									ship.armaments.keys.filter {
										ship.canUseWeapon(it)
									}.flatMap { weaponId ->
										weaponId.validTargets(state, ship).map { target ->
											Triple(ship, weaponId, target)
										}
									}
								else emptyList()
							}.associateWith { (ship, weaponId, target) ->
								weaponId.expectedAdvantageFromWeaponUsage(state, ship, target) * smoothNegative(brain[shipAttackPriority forShip target.id].signedPow(instincts[combatPrioritization])) * (1 + target.calculateSuffering()).signedPow(instincts[combatPreyOnTheWeak])
							}
							
							if (potentialAttacks.isEmpty() || Random.nextInt(3) == 0) {
								val potentialBoardings = state.ships.values.flatMap { ship ->
									if (ship.owner == mySide && ship.canSendBoardingParty) {
										val pickRequest = ship.getBoardingPickRequest()
										state.ships.values.filter { target ->
											target.owner == mySide.other && target.position.location in pickRequest.boundary
										}.map { target -> ship to target }
									} else emptyList()
								}.associateWith { (ship, target) ->
									ship.expectedBoardingSuccess(target)
								}
								
								val board = potentialBoardings.weightedRandomOrNull()
								
								if (board != null) {
									val (ship, target) = board
									doActions.send(PlayerAction.UseAbility(PlayerAbilityType.BoardingParty(ship.id), PlayerAbilityData.BoardingParty(target.id)))
									
									withTimeoutOrNull(50L) { getErrors.receive() }?.let { error ->
										logWarning("Error when boarding target ship ID ${target.id} with assault parties of ship ID ${ship.id} - $error")
										
										val nextState = gameState.value
										phasePipe.send(nextState.phase to (nextState.doneWithPhase != mySide && (!nextState.phase.usesInitiative || nextState.currentInitiative != mySide.other)))
									}
									
									continue@loop
								}
							}
							
							val attackWith = potentialAttacks.weightedRandomOrNull()
							
							if (attackWith == null) {
								doActions.send(PlayerAction.UseAbility(PlayerAbilityType.DonePhase(phase), PlayerAbilityData.DonePhase))
								continue@loop
							}
							
							val (ship, weaponId, target) = attackWith
							val targetPickResponse = when (val weaponSpec = ship.armaments[weaponId]?.weapon) {
								is AreaWeapon -> {
									val pickRequest = ship.getWeaponPickRequest(weaponSpec)
									val targetLocation = target.position.location
									val closestValidLocation = pickRequest.boundary.closestPointTo(targetLocation)
									
									val chosenLocation = if ((targetLocation - closestValidLocation).length >= EPSILON)
										closestValidLocation + ((closestValidLocation - targetLocation) * 0.2)
									else closestValidLocation
									
									PickResponse.Location(chosenLocation)
								}
								is ShipWeapon.Lance -> {
									doActions.send(PlayerAction.UseAbility(PlayerAbilityType.ChargeLance(ship.id, weaponId), PlayerAbilityData.ChargeLance))
									withTimeoutOrNull(50L) { getErrors.receive() }?.let { error ->
										logWarning("Error when charging lance weapon $weaponId of ship ID ${ship.id} - $error")
									}
									
									PickResponse.Ship(target.id)
								}
								else -> PickResponse.Ship(target.id)
							}
							
							doActions.send(PlayerAction.UseAbility(PlayerAbilityType.UseWeapon(ship.id, weaponId), PlayerAbilityData.UseWeapon(targetPickResponse)))
							
							withTimeoutOrNull(50L) { getErrors.receive() }?.let { error ->
								logWarning("Error when attacking target ship ID ${target.id} with weapon $weaponId of ship ID ${ship.id} - $error")
								
								val remainingAllAreaWeapons = potentialAttacks.keys.map { (attacker, weaponId, _) ->
									attacker to weaponId
								}.toSet().all { (attacker, weaponId) ->
									attacker.armaments[weaponId]?.weapon is AreaWeapon
								}
								
								if (remainingAllAreaWeapons)
									doActions.send(PlayerAction.UseAbility(PlayerAbilityType.DonePhase(phase), PlayerAbilityData.DonePhase))
								else {
									val nextState = gameState.value
									phasePipe.send(nextState.phase to (nextState.doneWithPhase != mySide && (!nextState.phase.usesInitiative || nextState.currentInitiative != mySide.other)))
								}
							}
						}
						is GamePhase.Repair -> {
							val repairAbility = state.getPossibleAbilities(mySide).filter {
								it !is PlayerAbilityType.DonePhase
							}.randomOrNull()
							
							if (repairAbility == null) {
								doActions.send(PlayerAction.UseAbility(PlayerAbilityType.DonePhase(phase), PlayerAbilityData.DonePhase))
								continue@loop
							}
							
							when (repairAbility) {
								is PlayerAbilityType.RepairShipModule -> PlayerAbilityData.RepairShipModule
								is PlayerAbilityType.ExtinguishFire -> PlayerAbilityData.ExtinguishFire
								is PlayerAbilityType.Recoalesce -> PlayerAbilityData.Recoalesce
								else -> null
							}?.let { repairData ->
								doActions.send(PlayerAction.UseAbility(repairAbility, repairData))
							}
						}
					}
				}
			}
		}
	} catch (ex: Exception) {
		logError(ex)
		doActions.send(PlayerAction.SendChatMessage(ex.stackTraceToString()))
		delay(2000L)
		doActions.send(PlayerAction.Disconnect)
	}
}

fun deploy(gameState: GameState, mySide: GlobalSide, instincts: Instincts): Map<Id<ShipInstance>, Position> {
	val size = gameState.battleInfo.size
	val totalPoints = size.numPoints
	val maxWC = size.maxWeightClass
	
	val myStart = gameState.start.playerStart(mySide)
	
	val deployable = myStart.deployableFleet.values.filter { it.shipType.weightClass.tier <= maxWC.tier }.toMutableSet()
	val deployed = mutableSetOf<Ship>()
	
	while (true) {
		val deployShip = deployable.filter { ship ->
			deployed.sumOf { it.pointCost } + ship.pointCost <= totalPoints
		}.associateWith { ship ->
			instincts[ship.shipType.weightClass.focus]
		}.weightedRandomOrNull() ?: break
		
		deployable -= deployShip
		deployed += deployShip
	}
	
	return placeShips(deployed, myStart.deployZone)
}

fun navigate(gameState: GameState, ship: ShipInstance, instincts: Instincts, brain: Brain): PlayerAction.UseAbility {
	val noEnemyShipsSeen = gameState.ships.values.none { it.owner != ship.owner && it.isIdentified }
	
	if (noEnemyShipsSeen || !ship.isIdentified) return engage(gameState, ship)
	
	val currPos = ship.position.location
	val currAngle = ship.position.facing
	
	val movement = ship.movement
	
	if (movement is FelinaeShipMovement && Random.nextDouble() < 1.0 / (ship.usedInertialessDriveShots * 3 + 5)) {
		val maxJump = movement.inertialessDriveRange * 0.99
		
		val positions = listOf(
			normalVector(currAngle),
			normalVector(currAngle).let { (x, y) -> Vec2(-y, x) },
			-normalVector(currAngle),
			normalVector(currAngle).let { (x, y) -> Vec2(y, -x) },
		).flatMap {
			listOf(
				ShipPosition(currPos + (Distance(it) * maxJump), it.angle),
				ShipPosition(currPos + (Distance(it) * (maxJump * 2 / 3)), it.angle),
				ShipPosition(currPos + (Distance(it) * (maxJump / 3)), it.angle),
			)
		}.filter { shipPos ->
			(gameState.ships - ship.id).none { (_, otherShip) ->
				(otherShip.position.location - shipPos.location).length <= SHIP_BASE_SIZE
			}
		}
		
		val position = positions.associateWith {
			it.score(gameState, ship, instincts, brain)
		}.weightedRandomOrNull() ?: return pursue(gameState, ship)
		
		return PlayerAction.UseAbility(
			PlayerAbilityType.UseInertialessDrive(ship.id),
			PlayerAbilityData.UseInertialessDrive(position.location)
		)
	}
	
	val maxTurn = movement.turnAngle * 0.99
	val maxMove = movement.moveSpeed * 0.99
	val minMove = movement.moveSpeed * 0.51
	
	val positions = (listOf(
		normalDistance(currAngle) rotatedBy -maxTurn,
		normalDistance(currAngle) rotatedBy (-maxTurn / 2),
		normalDistance(currAngle),
		normalDistance(currAngle) rotatedBy (maxTurn / 2),
		normalDistance(currAngle) rotatedBy maxTurn,
	).flatMap {
		listOf(
			ShipPosition(currPos + (it * maxMove), it.angle),
			ShipPosition(currPos + (it * minMove), it.angle),
		)
	} + listOf(ship.position)).filter { shipPos ->
		(gameState.ships - ship.id).none { (_, otherShip) ->
			(otherShip.position.location - shipPos.location).length <= SHIP_BASE_SIZE
		}
	}
	
	val position = positions.associateWith {
		it.score(gameState, ship, instincts, brain)
	}.weightedRandomOrNull() ?: return pursue(gameState, ship)
	
	return PlayerAction.UseAbility(
		PlayerAbilityType.MoveShip(ship.id),
		PlayerAbilityData.MoveShip(position)
	)
}

fun engage(gameState: GameState, ship: ShipInstance): PlayerAction.UseAbility {
	val mySideMeanPosition = gameState.ships.values
		.filter { it.owner == ship.owner }
		.map { it.position.location.vector }
		.mean()
	
	val enemySideMeanPosition = gameState.ships.values
		.filter { it.owner != ship.owner }
		.map { it.position.location.vector }
		.mean()
	
	val angleTo = normalVector(ship.position.facing) angleTo (enemySideMeanPosition - mySideMeanPosition)
	val maxTurn = ship.movement.turnAngle * 0.99
	val turnNormal = normalDistance(ship.position.facing) rotatedBy angleTo.coerceIn(-maxTurn..maxTurn)
	
	val move = (ship.movement.moveSpeed * 0.99) * turnNormal
	val newLoc = ship.position.location + move
	
	val position = ShipPosition(newLoc, move.angle)
	
	return PlayerAction.UseAbility(
		PlayerAbilityType.MoveShip(ship.id),
		PlayerAbilityData.MoveShip(position)
	)
}

fun pursue(gameState: GameState, ship: ShipInstance): PlayerAction.UseAbility {
	val targetLocation = gameState.ships.values.filter { it.owner != ship.owner }.map { it.position.location }.minByOrNull { loc ->
		(loc - ship.position.location).length
	} ?: return PlayerAction.UseAbility(
		PlayerAbilityType.MoveShip(ship.id),
		PlayerAbilityData.MoveShip(ship.position)
	)
	
	return ship.navigateTo(targetLocation)
}
