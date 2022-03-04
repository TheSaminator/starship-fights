package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id

sealed interface ShipAbility {
	val ship: Id<ShipInstance>
}

sealed interface CombatAbility {
	val ship: Id<ShipInstance>
	val weapon: Id<ShipWeapon>
}

@Serializable
sealed class PlayerAbilityType {
	abstract suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData?
	abstract fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent
	
	@Serializable
	data class DonePhase(val phase: GamePhase) : PlayerAbilityType() {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase != phase) return null
			return PlayerAbilityData.DonePhase
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			return if (phase == gameState.phase)
				GameEvent.StateChange(gameState.afterPlayerReady(playerSide))
			else GameEvent.InvalidAction("Cannot complete non-current phase")
		}
	}
	
	@Serializable
	data class DeployShip(val ship: Id<Ship>) : PlayerAbilityType() {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase != GamePhase.Deploy) return null
			if (gameState.ready == playerSide) return null
			val pickBoundary = gameState.start.playerStart(playerSide).deployZone
			
			val playerStart = gameState.start.playerStart(playerSide)
			val shipData = playerStart.deployableFleet[ship] ?: return null
			val pickType = PickType.Location(setOf(playerSide), PickHelper.Ship(shipData.shipType, playerStart.deployFacing))
			
			val pickResponse = pick(PickRequest(pickType, pickBoundary))
			val shipPosition = (pickResponse as? PickResponse.Location)?.position ?: return null
			return PlayerAbilityData.DeployShip(shipPosition)
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (data !is PlayerAbilityData.DeployShip) return GameEvent.InvalidAction("Internal error from using player ability")
			val playerStart = gameState.start.playerStart(playerSide)
			val shipData = playerStart.deployableFleet[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			
			val position = data.position
			
			val pickRequest = PickRequest(
				PickType.Location(setOf(GlobalSide.HOST, GlobalSide.GUEST), PickHelper.Ship(shipData.shipType, playerStart.deployFacing)),
				gameState.start.playerStart(playerSide).deployZone
			)
			val pickResponse = PickResponse.Location(position)
			
			if (!gameState.isValidPick(pickRequest, pickResponse)) return GameEvent.InvalidAction("That ship cannot be deployed there")
			
			val prevPosition = Distance(Vec2(-1000.0, 0.0) rotatedBy playerStart.deployFacing) + position
			val shipPosition = ShipPosition(position, prevPosition, playerStart.deployFacing)
			val shipInstance = ShipInstance(shipData, playerSide, shipPosition)
			
			val newShipSet = gameState.ships + mapOf(shipInstance.id to shipInstance)
			
			if (newShipSet.values.filter { it.owner == playerSide }.sumOf { it.ship.pointCost } > gameState.battleInfo.size.numPoints)
				return GameEvent.InvalidAction("Not enough points to deploy this ship")
			
			val deployableShips = playerStart.deployableFleet - ship
			val newPlayerStart = playerStart.copy(deployableFleet = deployableShips)
			
			return GameEvent.StateChange(
				with(gameState) {
					copy(
						start = when (playerSide) {
							GlobalSide.HOST -> start.copy(hostStart = newPlayerStart)
							GlobalSide.GUEST -> start.copy(guestStart = newPlayerStart)
						},
						ships = newShipSet
					)
				}
			)
		}
	}
	
	@Serializable
	data class UndeployShip(val ship: Id<ShipInstance>) : PlayerAbilityType() {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			return if (gameState.phase == GamePhase.Deploy && gameState.ready != playerSide) PlayerAbilityData.UndeployShip else null
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship is not deployed")
			val shipData = shipInstance.ship
			
			val newShipSet = gameState.ships - ship
			
			val playerStart = gameState.start.playerStart(playerSide)
			
			val deployableShips = playerStart.deployableFleet + mapOf(shipData.id to shipData)
			val newPlayerStart = playerStart.copy(deployableFleet = deployableShips)
			
			return GameEvent.StateChange(
				with(gameState) {
					copy(
						start = when (playerSide) {
							GlobalSide.HOST -> start.copy(hostStart = newPlayerStart)
							GlobalSide.GUEST -> start.copy(guestStart = newPlayerStart)
						},
						ships = newShipSet
					)
				}
			)
		}
	}
	
	@Serializable
	data class DistributePower(override val ship: Id<ShipInstance>) : PlayerAbilityType(), ShipAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Power) return null
			
			val data = ClientAbilityData.newShipPowerModes.remove(ship) ?: return null
			val shipInstance = gameState.ships[ship] ?: return null
			if (!shipInstance.validatePowerMode(data)) return null
			
			return PlayerAbilityData.DistributePower(data)
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (data !is PlayerAbilityData.DistributePower) return GameEvent.InvalidAction("Internal error from using player ability")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (!shipInstance.validatePowerMode(data.powerMode)) return GameEvent.InvalidAction("Invalid power distribution")
			
			val prevShieldDamage = shipInstance.powerMode.shields - shipInstance.shieldAmount
			
			val newShipInstance = shipInstance.copy(
				powerMode = data.powerMode,
				isDoneCurrentPhase = true,
				
				weaponAmount = data.powerMode.weapons,
				shieldAmount = (data.powerMode.shields - prevShieldDamage).coerceAtLeast(0),
			)
			val newShips = gameState.ships + mapOf(ship to newShipInstance)
			
			return GameEvent.StateChange(
				gameState.copy(ships = newShips)
			)
		}
	}
	
	@Serializable
	data class MoveShip(override val ship: Id<ShipInstance>) : PlayerAbilityType(), ShipAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Move) return null
			val shipInstance = gameState.ships[ship] ?: return null
			
			val anglePickReq = PickRequest(
				PickType.Location(emptySet(), PickHelper.None, shipInstance.position.currentLocation),
				PickBoundary.Angle(shipInstance.position.currentLocation, shipInstance.position.facingAngle, shipInstance.movement.turnAngle)
			)
			val anglePickRes = (pick(anglePickReq) as? PickResponse.Location) ?: return null
			val facingTowards = (anglePickRes.position - shipInstance.position.currentLocation)
			val newFacing = facingTowards.angle
			
			val oldFacingNormal = normalDistance(shipInstance.position.facingAngle)
			val moveAlong = (oldFacingNormal rotatedBy ((oldFacingNormal angleTo facingTowards) / 2)) * shipInstance.movement.moveSpeed
			
			val moveOrigin = shipInstance.position.currentLocation + shipInstance.position.currentVelocity
			val moveFrom = moveOrigin - moveAlong
			val moveTo = moveOrigin + moveAlong
			
			val positionPickReq = PickRequest(
				PickType.Location(GlobalSide.values().toSet(), PickHelper.Ship(shipInstance.ship.shipType, newFacing), null),
				PickBoundary.AlongLine(moveFrom, moveTo)
			)
			val positionPickRes = (pick(positionPickReq) as? PickResponse.Location) ?: return null
			
			val newPosition = ShipPosition(
				positionPickRes.position,
				shipInstance.position.currentLocation,
				newFacing
			)
			
			return PlayerAbilityData.MoveShip(newPosition)
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (data !is PlayerAbilityData.MoveShip) return GameEvent.InvalidAction("Internal error from using player ability")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.isDoneCurrentPhase) return GameEvent.InvalidAction("Ships cannot be moved twice")
			
			if ((data.newPosition.previousLocation - shipInstance.position.currentLocation).length > EPSILON) return GameEvent.InvalidAction("Invalid ship position")
			
			val oldFacingNormal = normalDistance(shipInstance.position.facingAngle)
			val newFacingNormal = normalDistance(data.newPosition.facingAngle)
			
			if (oldFacingNormal angleBetween newFacingNormal > shipInstance.movement.turnAngle) return GameEvent.InvalidAction("Excessive turn")
			
			val moveAlong = (oldFacingNormal rotatedBy ((oldFacingNormal angleTo newFacingNormal) / 2)) * shipInstance.movement.moveSpeed
			
			val moveOrigin = shipInstance.position.currentLocation + shipInstance.position.currentVelocity
			val moveFrom = moveOrigin - moveAlong
			val moveTo = moveOrigin + moveAlong
			
			if (data.newPosition.currentLocation.distanceToLineSegment(moveFrom, moveTo) > EPSILON) return GameEvent.InvalidAction("Illegal move")
			
			val newShipInstance = shipInstance.copy(position = data.newPosition, isDoneCurrentPhase = true)
			val newShips = gameState.ships + mapOf(ship to newShipInstance)
			
			return GameEvent.StateChange(
				gameState.copy(ships = newShips)
			)
		}
	}
	
	@Serializable
	data class ChargeLance(override val ship: Id<ShipInstance>, override val weapon: Id<ShipWeapon>) : PlayerAbilityType(), CombatAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Attack) return null
			val shipInstance = gameState.ships[ship] ?: return null
			if (shipInstance.weaponAmount <= 0) return null
			if (weapon in shipInstance.usedArmaments) return null
			val shipWeapon = shipInstance.armaments.weaponInstances[weapon] ?: return null
			if (shipWeapon !is ShipWeaponInstance.Lance) return null
			
			return PlayerAbilityData.ChargeLance
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (gameState.phase !is GamePhase.Attack) return GameEvent.InvalidAction("Ships can only charge lances during Phase III")
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.weaponAmount <= 0) return GameEvent.InvalidAction("Not enough power to charge lances")
			if (weapon in shipInstance.usedArmaments) return GameEvent.InvalidAction("Cannot charge used lances")
			val shipWeapon = shipInstance.armaments.weaponInstances[weapon] ?: return GameEvent.InvalidAction("That weapon does not exist")
			if (shipWeapon !is ShipWeaponInstance.Lance) return GameEvent.InvalidAction("Cannot charge non-lance weapons")
			
			return GameEvent.StateChange(
				gameState.copy(
					ships = gameState.ships + mapOf(
						ship to shipInstance.copy(
							weaponAmount = shipInstance.weaponAmount - 1,
							armaments = shipInstance.armaments.copy(
								weaponInstances = shipInstance.armaments.weaponInstances + mapOf(
									weapon to shipWeapon.copy(numCharges = shipWeapon.numCharges + 1)
								)
							)
						)
					)
				)
			)
		}
	}
	
	@Serializable
	data class UseWeapon(override val ship: Id<ShipInstance>, override val weapon: Id<ShipWeapon>) : PlayerAbilityType(), CombatAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Attack) return null
			val shipInstance = gameState.ships[ship] ?: return null
			if (weapon in shipInstance.usedArmaments) return null
			val shipWeapon = shipInstance.armaments.weaponInstances[weapon] ?: return null
			if (!canWeaponBeUsed(shipInstance, shipWeapon)) return null
			
			val pickResponse = pick(getWeaponPickRequest(shipWeapon.weapon, shipInstance.position, shipInstance.owner))
			
			return pickResponse?.let { PlayerAbilityData.UseWeapon(it) }
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (data !is PlayerAbilityData.UseWeapon) return GameEvent.InvalidAction("Internal error from using player ability")
			
			if (gameState.phase !is GamePhase.Attack) return GameEvent.InvalidAction("Ships can only attack during Phase III")
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That attacking ship does not exist")
			if (weapon in shipInstance.usedArmaments) return GameEvent.InvalidAction("That weapon has already been used")
			val shipWeapon = shipInstance.armaments.weaponInstances[weapon] ?: return GameEvent.InvalidAction("That weapon does not exist")
			if (!canWeaponBeUsed(shipInstance, shipWeapon)) return GameEvent.InvalidAction("That weapon cannot be used at this time")
			
			val pickRequest = getWeaponPickRequest(shipWeapon.weapon, shipInstance.position, shipInstance.owner)
			val pickResponse = data.target
			
			if (!gameState.isValidPick(pickRequest, pickResponse)) return GameEvent.InvalidAction("Invalid target")
			
			return gameState.useWeaponPickResponse(shipInstance, weapon, pickResponse)
		}
	}
	
	@Serializable
	data class RecallStrikeCraft(override val ship: Id<ShipInstance>, override val weapon: Id<ShipWeapon>) : PlayerAbilityType(), CombatAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Attack) return null
			val shipInstance = gameState.ships[ship] ?: return null
			if (weapon !in shipInstance.usedArmaments) return null
			val shipWeapon = shipInstance.armaments.weaponInstances[weapon] ?: return null
			if (shipWeapon !is ShipWeaponInstance.Hangar) return null
			
			return PlayerAbilityData.RecallStrikeCraft
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (gameState.phase !is GamePhase.Attack) return GameEvent.InvalidAction("Ships can only recall strike craft during Phase III")
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (weapon !in shipInstance.usedArmaments) return GameEvent.InvalidAction("Cannot recall unused strike craft")
			val shipWeapon = shipInstance.armaments.weaponInstances[weapon] ?: return GameEvent.InvalidAction("That weapon does not exist")
			if (shipWeapon !is ShipWeaponInstance.Hangar) return GameEvent.InvalidAction("Cannot recall non-hangar weapons")
			
			val hangarWing = ShipHangarWing(ship, weapon)
			
			return GameEvent.StateChange(
				gameState.copy(
					ships = gameState.ships.mapValues { (_, targetShip) ->
						targetShip.copy(
							fighterWings = targetShip.fighterWings - hangarWing,
							bomberWings = targetShip.bomberWings - hangarWing,
						)
					}
				)
			)
		}
	}
}

@Serializable
sealed class PlayerAbilityData {
	@Serializable
	object DonePhase : PlayerAbilityData()
	
	@Serializable
	data class DeployShip(val position: Position) : PlayerAbilityData()
	
	@Serializable
	object UndeployShip : PlayerAbilityData()
	
	@Serializable
	data class DistributePower(val powerMode: ShipPowerMode) : PlayerAbilityData()
	
	@Serializable
	data class MoveShip(val newPosition: ShipPosition) : PlayerAbilityData()
	
	@Serializable
	object ChargeLance : PlayerAbilityData()
	
	@Serializable
	data class UseWeapon(val target: PickResponse) : PlayerAbilityData()
	
	@Serializable
	object RecallStrikeCraft : PlayerAbilityData()
}

fun GameState.getPossibleAbilities(forPlayer: GlobalSide): List<PlayerAbilityType> = if (ready == forPlayer)
	emptyList()
else when (phase) {
	GamePhase.Deploy -> {
		val usedPoints = ships.values
			.filter { it.owner == forPlayer }
			.sumOf { it.ship.pointCost }
		
		val deployShips = start.playerStart(forPlayer).deployableFleet
			.filterValues { usedPoints + it.pointCost <= battleInfo.size.numPoints }.keys
			.map { PlayerAbilityType.DeployShip(it) }
		
		val undeployShips = ships
			.filterValues { it.owner == forPlayer }
			.keys
			.map { PlayerAbilityType.UndeployShip(it) }
		
		val finishDeploying = if (deployShips.isEmpty())
			listOf(PlayerAbilityType.DonePhase(GamePhase.Deploy))
		else emptyList()
		
		deployShips + undeployShips + finishDeploying
	}
	is GamePhase.Power -> {
		val powerableShips = ships
			.filterValues { it.owner == forPlayer && !it.isDoneCurrentPhase }
			.keys
			.map { PlayerAbilityType.DistributePower(it) }
		
		val finishPowering = listOf(PlayerAbilityType.DonePhase(GamePhase.Power(phase.turn)))
		
		powerableShips + finishPowering
	}
	is GamePhase.Move -> {
		val movableShips = ships
			.filterValues { it.owner == forPlayer && !it.isDoneCurrentPhase }
			.keys
			.map { PlayerAbilityType.MoveShip(it) }
		
		val finishMoving = listOf(PlayerAbilityType.DonePhase(GamePhase.Move(phase.turn)))
		
		movableShips + finishMoving
	}
	is GamePhase.Attack -> {
		val chargeableLances = ships
			.filterValues { it.owner == forPlayer && it.weaponAmount > 0 }
			.flatMap { (id, ship) ->
				ship.armaments.weaponInstances.mapNotNull { (weaponId, weapon) ->
					PlayerAbilityType.ChargeLance(id, weaponId).takeIf {
						when (weapon) {
							is ShipWeaponInstance.Lance -> weapon.charge != 1.0 && weaponId !in ship.usedArmaments
							else -> false
						}
					}
				}
			}
		
		val usableWeapons = ships
			.filterValues { it.owner == forPlayer }
			.flatMap { (id, ship) ->
				ship.armaments.weaponInstances.mapNotNull { (weaponId, weapon) ->
					PlayerAbilityType.UseWeapon(id, weaponId).takeIf {
						weaponId !in ship.usedArmaments && canWeaponBeUsed(ship, weapon)
					}
				}
			}
		
		val recallableStrikeWings = ships
			.filterValues { it.owner == forPlayer }
			.flatMap { (id, ship) ->
				ship.armaments.weaponInstances.mapNotNull { (weaponId, weapon) ->
					PlayerAbilityType.RecallStrikeCraft(id, weaponId).takeIf {
						weaponId in ship.usedArmaments && weapon is ShipWeaponInstance.Hangar
					}
				}
			}
		
		val finishAttacking = listOf(PlayerAbilityType.DonePhase(GamePhase.Attack(phase.turn)))
		
		chargeableLances + usableWeapons + recallableStrikeWings + finishAttacking
	}
}

object ClientAbilityData {
	val newShipPowerModes = mutableMapOf<Id<ShipInstance>, ShipPowerMode>()
}
