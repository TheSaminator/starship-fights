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
			return if (gameState.canFinishPhase(playerSide))
				PlayerAbilityData.DonePhase
			else null
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			return if (phase == gameState.phase) {
				if (gameState.canFinishPhase(playerSide))
					GameEvent.StateChange(gameState.afterPlayerReady(playerSide))
				else GameEvent.InvalidAction("You cannot complete the current phase yet")
			} else GameEvent.InvalidAction("Cannot complete non-current phase")
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
			val pickType = PickType.Location(gameState.ships.keys, PickHelper.Ship(shipData.shipType, playerStart.deployFacing))
			
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
				PickType.Location(gameState.ships.keys, PickHelper.Ship(shipData.shipType, playerStart.deployFacing)),
				gameState.start.playerStart(playerSide).deployZone
			)
			val pickResponse = PickResponse.Location(position)
			
			if (!gameState.isValidPick(pickRequest, pickResponse)) return GameEvent.InvalidAction("That ship cannot be deployed there")
			
			val shipPosition = ShipPosition(position, playerStart.deployFacing)
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
				shieldAmount = if (shipInstance.canUseShields)
					(data.powerMode.shields - prevShieldDamage).coerceAtLeast(0)
				else 0,
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
			if (shipInstance.isDoneCurrentPhase) return null
			
			val anglePickReq = PickRequest(
				PickType.Location(emptySet(), PickHelper.None, shipInstance.position.location),
				PickBoundary.Angle(shipInstance.position.location, shipInstance.position.facing, shipInstance.movement.turnAngle)
			)
			val anglePickRes = (pick(anglePickReq) as? PickResponse.Location) ?: return null
			
			val newFacingNormal = (anglePickRes.position - shipInstance.position.location).normal
			val newFacing = newFacingNormal.angle
			
			val oldFacingNormal = normalDistance(shipInstance.position.facing)
			val angleDiff = (oldFacingNormal angleBetween newFacingNormal)
			val maxMoveSpeed = shipInstance.movement.moveSpeed
			val minMoveSpeed = maxMoveSpeed * (angleDiff / shipInstance.movement.turnAngle) / 2
			
			val moveOrigin = shipInstance.position.location
			val moveFrom = moveOrigin + (newFacingNormal * minMoveSpeed)
			val moveTo = moveOrigin + (newFacingNormal * maxMoveSpeed)
			
			val positionPickReq = PickRequest(
				PickType.Location(gameState.ships.keys - ship, PickHelper.Ship(shipInstance.ship.shipType, newFacing), null),
				PickBoundary.AlongLine(moveFrom, moveTo)
			)
			val positionPickRes = (pick(positionPickReq) as? PickResponse.Location) ?: return null
			
			val newPosition = ShipPosition(
				positionPickRes.position,
				newFacing
			)
			
			return PlayerAbilityData.MoveShip(newPosition)
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (data !is PlayerAbilityData.MoveShip) return GameEvent.InvalidAction("Internal error from using player ability")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.isDoneCurrentPhase) return GameEvent.InvalidAction("Ships cannot be moved twice")
			
			val moveOrigin = shipInstance.position.location
			val newFacingNormal = (data.newPosition.location - moveOrigin).normal
			val oldFacingNormal = normalDistance(shipInstance.position.facing)
			val angleDiff = (oldFacingNormal angleBetween newFacingNormal)
			
			if (angleDiff - shipInstance.movement.turnAngle > EPSILON) return GameEvent.InvalidAction("Illegal move - turn angle is too big")
			
			val maxMoveSpeed = shipInstance.movement.moveSpeed
			val minMoveSpeed = maxMoveSpeed * (angleDiff / shipInstance.movement.turnAngle) / 2
			
			val moveFrom = moveOrigin + (newFacingNormal * minMoveSpeed)
			val moveTo = moveOrigin + (newFacingNormal * maxMoveSpeed)
			
			if (data.newPosition.location.distanceToLineSegment(moveFrom, moveTo) > EPSILON) return GameEvent.InvalidAction("Illegal move - must be on facing line")
			
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
			if (!shipInstance.canUseWeapon(weapon)) return null
			val shipWeapon = shipInstance.armaments.weaponInstances[weapon] ?: return null
			
			val pickResponse = pick(getWeaponPickRequest(shipWeapon.weapon, shipInstance.position, shipInstance.owner))
			
			return pickResponse?.let { PlayerAbilityData.UseWeapon(it) }
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (data !is PlayerAbilityData.UseWeapon) return GameEvent.InvalidAction("Internal error from using player ability")
			
			if (gameState.phase !is GamePhase.Attack) return GameEvent.InvalidAction("Ships can only attack during Phase III")
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That attacking ship does not exist")
			if (!shipInstance.canUseWeapon(weapon)) return GameEvent.InvalidAction("That weapon cannot be used")
			val shipWeapon = shipInstance.armaments.weaponInstances[weapon] ?: return GameEvent.InvalidAction("That weapon does not exist")
			
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
			
			val newShip = shipInstance.copy(
				usedArmaments = shipInstance.usedArmaments - weapon
			)
			
			return GameEvent.StateChange(
				gameState.copy(
					ships = gameState.ships.mapValues { (_, targetShip) ->
						targetShip.copy(
							fighterWings = targetShip.fighterWings - hangarWing,
							bomberWings = targetShip.bomberWings - hangarWing,
						)
					} + mapOf(ship to newShip)
				)
			)
		}
	}
	
	@Serializable
	data class RepairShipModule(override val ship: Id<ShipInstance>, val module: ShipModule) : PlayerAbilityType(), ShipAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Repair) return null
			val shipInstance = gameState.ships[ship] ?: return null
			if (shipInstance.remainingRepairTokens <= 0) return null
			if (!shipInstance.modulesStatus[module].canBeRepaired) return null
			
			return PlayerAbilityData.RepairShipModule
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (gameState.phase !is GamePhase.Repair) return GameEvent.InvalidAction("Ships can only repair modules during Phase IV")
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.remainingRepairTokens <= 0) return GameEvent.InvalidAction("That ship has no remaining repair tokens")
			if (!shipInstance.modulesStatus[module].canBeRepaired) return GameEvent.InvalidAction("That module cannot be repaired")
			
			val newShip = shipInstance.copy(
				modulesStatus = shipInstance.modulesStatus.repair(module),
				usedRepairTokens = shipInstance.usedRepairTokens + 1
			)
			
			return GameEvent.StateChange(
				gameState.copy(
					ships = gameState.ships + mapOf(
						ship to newShip
					)
				)
			)
		}
	}
	
	@Serializable
	data class ExtinguishFire(override val ship: Id<ShipInstance>) : PlayerAbilityType(), ShipAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Repair) return null
			val shipInstance = gameState.ships[ship] ?: return null
			if (shipInstance.remainingRepairTokens <= 0) return null
			if (shipInstance.numFires <= 0) return null
			
			return PlayerAbilityData.ExtinguishFire
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (gameState.phase !is GamePhase.Repair) return GameEvent.InvalidAction("Ships can only extinguish fires during Phase IV")
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.remainingRepairTokens <= 0) return GameEvent.InvalidAction("That ship has no remaining repair tokens")
			if (shipInstance.numFires <= 0) return GameEvent.InvalidAction("Cannot extinguish non-existent fires")
			
			val newShip = shipInstance.copy(
				numFires = shipInstance.numFires - 1,
				usedRepairTokens = shipInstance.usedRepairTokens + 1
			)
			
			return GameEvent.StateChange(
				gameState.copy(
					ships = gameState.ships + mapOf(
						ship to newShip
					)
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
	
	@Serializable
	object RepairShipModule : PlayerAbilityData()
	
	@Serializable
	object ExtinguishFire : PlayerAbilityData()
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
		
		val finishDeploying = if (canFinishPhase(forPlayer))
			listOf(PlayerAbilityType.DonePhase(GamePhase.Deploy))
		else emptyList()
		
		deployShips + undeployShips + finishDeploying
	}
	is GamePhase.Power -> {
		val powerableShips = ships
			.filterValues { it.owner == forPlayer && !it.isDoneCurrentPhase }
			.keys
			.map { PlayerAbilityType.DistributePower(it) }
		
		val finishPowering = if (canFinishPhase(forPlayer))
			listOf(PlayerAbilityType.DonePhase(GamePhase.Power(phase.turn)))
		else emptyList()
		
		powerableShips + finishPowering
	}
	is GamePhase.Move -> {
		val movableShips = ships
			.filterValues { it.owner == forPlayer && !it.isDoneCurrentPhase }
			.keys
			.map { PlayerAbilityType.MoveShip(it) }
		
		val finishMoving = if (canFinishPhase(forPlayer))
			listOf(PlayerAbilityType.DonePhase(GamePhase.Move(phase.turn)))
		else emptyList()
		
		movableShips + finishMoving
	}
	is GamePhase.Attack -> {
		val chargeableLances = ships
			.filterValues { it.owner == forPlayer && it.weaponAmount > 0 }
			.flatMap { (id, ship) ->
				ship.armaments.weaponInstances.mapNotNull { (weaponId, weapon) ->
					PlayerAbilityType.ChargeLance(id, weaponId).takeIf {
						when (weapon) {
							is ShipWeaponInstance.Lance -> weapon.numCharges < 7 && weaponId !in ship.usedArmaments
							else -> false
						}
					}
				}
			}
		
		val usableWeapons = ships
			.filterValues { it.owner == forPlayer }
			.flatMap { (id, ship) ->
				ship.armaments.weaponInstances.keys.mapNotNull { weaponId ->
					PlayerAbilityType.UseWeapon(id, weaponId).takeIf {
						weaponId !in ship.usedArmaments && ship.canUseWeapon(weaponId)
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
		
		val finishAttacking = if (canFinishPhase(forPlayer))
			listOf(PlayerAbilityType.DonePhase(GamePhase.Attack(phase.turn)))
		else emptyList()
		
		chargeableLances + usableWeapons + recallableStrikeWings + finishAttacking
	}
	is GamePhase.Repair -> {
		val repairableModules = ships
			.filterValues { it.owner == forPlayer }
			.flatMap { (id, ship) ->
				ship.modulesStatus.statuses.filterValues { it.canBeRepaired }.keys.map { module ->
					PlayerAbilityType.RepairShipModule(id, module)
				}
			}
		
		val extinguishableFires = ships
			.filterValues { it.owner == forPlayer }
			.mapNotNull { (id, ship) ->
				if (ship.numFires <= 0)
					null
				else
					PlayerAbilityType.ExtinguishFire(id)
			}
		
		val finishRepairing = if (canFinishPhase(forPlayer))
			listOf(PlayerAbilityType.DonePhase(GamePhase.Repair(phase.turn)))
		else emptyList()
		
		repairableModules + extinguishableFires + finishRepairing
	}
}

object ClientAbilityData {
	val newShipPowerModes = mutableMapOf<Id<ShipInstance>, ShipPowerMode>()
}
