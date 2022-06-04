package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id
import kotlin.math.abs
import kotlin.random.Random

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
			if (gameState.doneWithPhase == playerSide) return null
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
			return if (gameState.phase == GamePhase.Deploy && gameState.doneWithPhase != playerSide) PlayerAbilityData.UndeployShip else null
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
			
			val shipInstance = gameState.ships[ship] ?: return null
			if (shipInstance.ship.reactor !is StandardShipReactor) return null
			
			val data = ClientAbilityData.newShipPowerModes.remove(ship) ?: return null
			if (!shipInstance.validatePowerMode(data)) return null
			
			return PlayerAbilityData.DistributePower(data)
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (data !is PlayerAbilityData.DistributePower) return GameEvent.InvalidAction("Internal error from using player ability")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.ship.reactor !is StandardShipReactor) return GameEvent.InvalidAction("Invalid ship reactor type")
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
	data class ConfigurePower(override val ship: Id<ShipInstance>, val powerMode: FelinaeShipPowerMode) : PlayerAbilityType(), ShipAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Power) return null
			
			val shipInstance = gameState.ships[ship] ?: return null
			if (shipInstance.ship.reactor != FelinaeShipReactor) return null
			
			return PlayerAbilityData.ConfigurePower
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.ship.reactor != FelinaeShipReactor) return GameEvent.InvalidAction("Invalid ship reactor type")
			
			val newShipInstance = shipInstance.copy(
				felinaeShipPowerMode = powerMode,
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
			if (!gameState.canShipMove(ship)) return null
			
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
			if (!gameState.canShipMove(ship)) return GameEvent.InvalidAction("You do not have the initiative")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.isDoneCurrentPhase) return GameEvent.InvalidAction("Ships cannot be moved twice")
			
			if ((gameState.ships - ship).any { (_, otherShip) -> (otherShip.position.location - data.newPosition.location).length <= SHIP_BASE_SIZE })
				return GameEvent.InvalidAction("You cannot move that ship there")
			
			val moveOrigin = shipInstance.position.location
			val newFacingNormal = normalDistance(data.newPosition.facing)
			val oldFacingNormal = normalDistance(shipInstance.position.facing)
			val angleDiff = (oldFacingNormal angleBetween newFacingNormal)
			
			if (angleDiff - shipInstance.movement.turnAngle > EPSILON) return GameEvent.InvalidAction("Illegal move - turn angle is too big")
			
			val maxMoveSpeed = shipInstance.movement.moveSpeed
			val minMoveSpeed = maxMoveSpeed * (angleDiff / shipInstance.movement.turnAngle) / 2
			
			val moveFrom = moveOrigin + (newFacingNormal * minMoveSpeed)
			val moveTo = moveOrigin + (newFacingNormal * maxMoveSpeed)
			
			if (data.newPosition.location.distanceToLineSegment(moveFrom, moveTo) > EPSILON) return GameEvent.InvalidAction("Illegal move - must be on facing line")
			
			val newShipInstance = shipInstance.copy(
				position = data.newPosition,
				currentVelocity = (data.newPosition.location - shipInstance.position.location).length,
				isDoneCurrentPhase = true
			)
			
			// Identify enemy ships
			val identifiedEnemyShips = gameState.ships.filterValues { enemyShip ->
				enemyShip.owner != playerSide && (enemyShip.position.location - newShipInstance.position.location).length <= SHIP_SENSOR_RANGE
			}
			
			// Be identified by enemy ships
			val shipsToBeIdentified = identifiedEnemyShips + if (!newShipInstance.isIdentified && identifiedEnemyShips.isNotEmpty())
				mapOf(ship to newShipInstance)
			else emptyMap()
			
			val identifiedShips = shipsToBeIdentified
				.filterValues { !it.isIdentified }
				.mapValues { (_, shipInstance) -> shipInstance.copy(isIdentified = true) }
			
			// Ships that move off the battlefield are considered to disengage
			val isDisengaged = newShipInstance.position.location.vector.let { (x, y) ->
				val mx = gameState.start.battlefieldWidth / 2
				val my = gameState.start.battlefieldLength / 2
				abs(x) > mx || abs(y) > my
			}
			
			val newChatEntries = gameState.chatBox + identifiedShips.map { (id, _) ->
				ChatEntry.ShipIdentified(id, Moment.now)
			} + (if (isDisengaged)
				listOf(ChatEntry.ShipEscaped(ship, Moment.now))
			else emptyList())
			
			val newShips = (gameState.ships + mapOf(ship to newShipInstance) + identifiedShips) - (if (isDisengaged)
				setOf(ship)
			else emptySet())
			
			val newWrecks = gameState.destroyedShips + (if (isDisengaged)
				mapOf(ship to ShipWreck(newShipInstance.ship, newShipInstance.owner, true))
			else emptyMap())
			
			return GameEvent.StateChange(
				gameState.copy(
					ships = newShips,
					destroyedShips = newWrecks,
					chatBox = newChatEntries,
				).withRecalculatedInitiative { calculateMovePhaseInitiative() }
			)
		}
	}
	
	@Serializable
	data class UseInertialessDrive(override val ship: Id<ShipInstance>) : PlayerAbilityType(), ShipAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Move) return null
			if (!gameState.canShipMove(ship)) return null
			
			val shipInstance = gameState.ships[ship] ?: return null
			if (shipInstance.isDoneCurrentPhase) return null
			
			if (!shipInstance.canUseInertialessDrive) return null
			val movement = shipInstance.movement
			if (movement !is FelinaeShipMovement) return null
			
			val positionPickReq = PickRequest(
				PickType.Location(gameState.ships.keys - ship, PickHelper.Circle(SHIP_BASE_SIZE), shipInstance.position.location),
				PickBoundary.Circle(
					shipInstance.position.location,
					movement.inertialessDriveRange,
				)
			)
			val positionPickRes = (pick(positionPickReq) as? PickResponse.Location) ?: return null
			
			return PlayerAbilityData.UseInertialessDrive(positionPickRes.position)
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (data !is PlayerAbilityData.UseInertialessDrive) return GameEvent.InvalidAction("Internal error from using player ability")
			if (!gameState.canShipMove(ship)) return GameEvent.InvalidAction("You do not have the initiative")
			
			if ((gameState.ships - ship).any { (_, otherShip) -> (otherShip.position.location - data.newPosition).length <= SHIP_BASE_SIZE })
				return GameEvent.InvalidAction("You cannot move that ship there")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.isDoneCurrentPhase) return GameEvent.InvalidAction("Ships cannot be moved twice")
			
			if (!shipInstance.canUseInertialessDrive) return GameEvent.InvalidAction("That ship cannot use its inertialess drive")
			val movement = shipInstance.movement
			if (movement !is FelinaeShipMovement) return GameEvent.InvalidAction("That ship does not have an inertialess drive")
			
			val oldPos = shipInstance.position.location
			val newPos = data.newPosition
			
			val deltaPos = newPos - oldPos
			val velocity = deltaPos.length
			
			if (velocity > movement.inertialessDriveRange) return GameEvent.InvalidAction("That move is out of range")
			
			val newFacing = deltaPos.angle
			
			val newShipInstance = shipInstance.copy(
				position = ShipPosition(newPos, newFacing),
				currentVelocity = velocity,
				isDoneCurrentPhase = true,
				usedInertialessDriveShots = shipInstance.usedInertialessDriveShots + 1
			)
			
			// Identify enemy ships
			val identifiedEnemyShips = gameState.ships.filterValues { enemyShip ->
				enemyShip.owner != playerSide && (enemyShip.position.location - newShipInstance.position.location).length <= SHIP_SENSOR_RANGE
			}
			
			// Be identified by enemy ships (Inertialess Drive automatically reveals your ship)
			val shipsToBeIdentified = identifiedEnemyShips + if (!newShipInstance.isIdentified)
				mapOf(ship to newShipInstance)
			else emptyMap()
			
			val identifiedShips = shipsToBeIdentified
				.filterValues { !it.isIdentified }
				.mapValues { (_, shipInstance) -> shipInstance.copy(isIdentified = true) }
			
			// Ships that move off the battlefield are considered to disengage
			val isDisengaged = newShipInstance.position.location.vector.let { (x, y) ->
				val mx = gameState.start.battlefieldWidth / 2
				val my = gameState.start.battlefieldLength / 2
				abs(x) > mx || abs(y) > my
			}
			
			val newChatEntries = gameState.chatBox + identifiedShips.map { (id, _) ->
				ChatEntry.ShipIdentified(id, Moment.now)
			} + (if (isDisengaged)
				listOf(ChatEntry.ShipEscaped(ship, Moment.now))
			else emptyList())
			
			val newShips = (gameState.ships + mapOf(ship to newShipInstance) + identifiedShips) - (if (isDisengaged)
				setOf(ship)
			else emptySet())
			
			val newWrecks = gameState.destroyedShips + (if (isDisengaged)
				mapOf(ship to ShipWreck(newShipInstance.ship, newShipInstance.owner, true))
			else emptyMap())
			
			return GameEvent.StateChange(
				gameState.copy(
					ships = newShips,
					destroyedShips = newWrecks,
					chatBox = newChatEntries,
				).withRecalculatedInitiative { calculateMovePhaseInitiative() }
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
			
			val shipWeapon = shipInstance.armaments[weapon] ?: return null
			if (shipWeapon !is ShipWeaponInstance.Lance) return null
			
			return PlayerAbilityData.ChargeLance
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (gameState.phase !is GamePhase.Attack) return GameEvent.InvalidAction("Ships can only charge lances during Phase III")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.weaponAmount <= 0) return GameEvent.InvalidAction("Not enough power to charge lances")
			if (weapon in shipInstance.usedArmaments) return GameEvent.InvalidAction("Cannot charge used lances")
			
			val shipWeapon = shipInstance.armaments[weapon] ?: return GameEvent.InvalidAction("That weapon does not exist")
			if (shipWeapon !is ShipWeaponInstance.Lance) return GameEvent.InvalidAction("Cannot charge non-lance weapons")
			
			return GameEvent.StateChange(
				gameState.copy(
					ships = gameState.ships + mapOf(
						ship to shipInstance.copy(
							weaponAmount = shipInstance.weaponAmount - 1,
							armaments = shipInstance.armaments + mapOf(
								weapon to shipWeapon.copy(numCharges = shipWeapon.numCharges + shipInstance.firepower.lanceCharging)
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
			
			val shipWeapon = shipInstance.armaments[weapon] ?: return null
			
			val pickResponse = pick(shipInstance.getWeaponPickRequest(shipWeapon.weapon))
			
			return pickResponse?.let { PlayerAbilityData.UseWeapon(it) }
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (data !is PlayerAbilityData.UseWeapon) return GameEvent.InvalidAction("Internal error from using player ability")
			
			if (gameState.phase !is GamePhase.Attack) return GameEvent.InvalidAction("Ships can only attack during Phase III")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That attacking ship does not exist")
			if (!shipInstance.canUseWeapon(weapon)) return GameEvent.InvalidAction("That weapon cannot be used")
			
			val shipWeapon = shipInstance.armaments[weapon] ?: return GameEvent.InvalidAction("That weapon does not exist")
			
			val pickRequest = shipInstance.getWeaponPickRequest(shipWeapon.weapon)
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
			
			val shipWeapon = shipInstance.armaments[weapon] ?: return null
			if (shipWeapon !is ShipWeaponInstance.Hangar) return null
			
			return PlayerAbilityData.RecallStrikeCraft
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (gameState.phase !is GamePhase.Attack) return GameEvent.InvalidAction("Ships can only recall strike craft during Phase III")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (weapon !in shipInstance.usedArmaments) return GameEvent.InvalidAction("Cannot recall unused strike craft")
			
			val shipWeapon = shipInstance.armaments[weapon] ?: return GameEvent.InvalidAction("That weapon does not exist")
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
				).withRecalculatedInitiative { calculateAttackPhaseInitiative() }
			)
		}
	}
	
	@Serializable
	data class DisruptionPulse(override val ship: Id<ShipInstance>) : PlayerAbilityType(), ShipAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Attack) return null
			
			val shipInstance = gameState.ships[ship] ?: return null
			if (!shipInstance.canUseDisruptionPulse) return null
			if (shipInstance.hasUsedDisruptionPulse) return null
			
			return PlayerAbilityData.DisruptionPulse
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (gameState.phase !is GamePhase.Attack) return GameEvent.InvalidAction("Ships can only emit Disruption Pulses during Phase III")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (!shipInstance.canUseDisruptionPulse) return GameEvent.InvalidAction("Cannot use Disruption Pulse")
			if (shipInstance.hasUsedDisruptionPulse) return GameEvent.InvalidAction("Cannot use Disruption Pulse twice")
			
			val durability = shipInstance.durability
			if (durability !is FelinaeShipDurability) return GameEvent.InvalidAction("That ship does not have a Disruption Pulse emitter")
			
			val targetedShips = gameState.ships.filterValues {
				(it.position.location - shipInstance.position.location).length < durability.disruptionPulseRange
			}
			
			val hangars = targetedShips.values.flatMap { target ->
				target.fighterWings + target.bomberWings
			}
			
			val changedShips = hangars.groupBy { it.ship }.mapNotNull { (shipId, hangarWings) ->
				val changedShip = gameState.ships[shipId] ?: return@mapNotNull null
				changedShip.copy(
					armaments = changedShip.armaments + hangarWings.associate {
						it.hangar to ShipWeaponInstance.Hangar(
							changedShip.ship.armaments[it.hangar] as ShipWeapon.Hangar,
							0.0
						)
					}
				)
			}.associateBy { it.id } + mapOf(
				ship to shipInstance.copy(
					hasUsedDisruptionPulse = true,
					usedDisruptionPulseShots = shipInstance.usedDisruptionPulseShots + 1
				)
			)
			
			return GameEvent.StateChange(
				gameState.copy(
					ships = gameState.ships + changedShips
				).withRecalculatedInitiative { calculateAttackPhaseInitiative() }
			)
		}
	}
	
	@Serializable
	data class BoardingParty(override val ship: Id<ShipInstance>) : PlayerAbilityType(), ShipAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Attack) return null
			
			val shipInstance = gameState.ships[ship] ?: return null
			if (!shipInstance.canSendBoardingParty) return null
			
			val pickResponse = pick(shipInstance.getBoardingPickRequest()) as? PickResponse.Ship ?: return null
			return PlayerAbilityData.BoardingParty(pickResponse.id)
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (data !is PlayerAbilityData.BoardingParty) return GameEvent.InvalidAction("Internal error from using player ability")
			
			if (gameState.phase !is GamePhase.Attack) return GameEvent.InvalidAction("Ships can only send Boarding Parties during Phase III")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (!shipInstance.canSendBoardingParty) return GameEvent.InvalidAction("Cannot send a boarding party")
			
			val afterBoarding = shipInstance.afterBoarding() ?: return GameEvent.InvalidAction("Cannot send a boarding party")
			
			val boarded = gameState.ships[data.target] ?: return GameEvent.InvalidAction("That ship does not exist")
			val afterBoarded = shipInstance.board(boarded)
			
			val newShips = (if (afterBoarded is ImpactResult.Damaged)
				gameState.ships + mapOf(data.target to afterBoarded.ship)
			else gameState.ships - data.target) + mapOf(ship to afterBoarding)
			
			val newWrecks = gameState.destroyedShips + (if (afterBoarded is ImpactResult.Destroyed)
				mapOf(data.target to afterBoarded.ship)
			else emptyMap())
			
			val newChatEntries = gameState.chatBox + reportBoardingResult(afterBoarded, ship)
			
			return GameEvent.StateChange(
				gameState.copy(
					ships = newShips,
					destroyedShips = newWrecks,
					chatBox = newChatEntries
				).withRecalculatedInitiative { calculateAttackPhaseInitiative() }
			)
		}
	}
	
	@Serializable
	data class RepairShipModule(override val ship: Id<ShipInstance>, val module: ShipModule) : PlayerAbilityType(), ShipAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Repair) return null
			
			val shipInstance = gameState.ships[ship] ?: return null
			if (shipInstance.durability !is StandardShipDurability) return null
			if (shipInstance.remainingRepairTokens <= 0) return null
			if (!shipInstance.modulesStatus[module].canBeRepaired) return null
			
			return PlayerAbilityData.RepairShipModule
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (gameState.phase !is GamePhase.Repair) return GameEvent.InvalidAction("Ships can only repair modules during Phase IV")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.durability !is StandardShipDurability) return GameEvent.InvalidAction("That ship cannot manually repair subsystems")
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
			if (shipInstance.durability !is StandardShipDurability) return null
			if (shipInstance.remainingRepairTokens <= 0) return null
			if (shipInstance.numFires <= 0) return null
			
			return PlayerAbilityData.ExtinguishFire
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (gameState.phase !is GamePhase.Repair) return GameEvent.InvalidAction("Ships can only extinguish fires during Phase IV")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.durability !is StandardShipDurability) return GameEvent.InvalidAction("That ship cannot manually extinguish fires")
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
	
	@Serializable
	data class Recoalesce(override val ship: Id<ShipInstance>) : PlayerAbilityType(), ShipAbility {
		override suspend fun beginOnClient(gameState: GameState, playerSide: GlobalSide, pick: suspend (PickRequest) -> PickResponse?): PlayerAbilityData? {
			if (gameState.phase !is GamePhase.Repair) return null
			
			val shipInstance = gameState.ships[ship] ?: return null
			if (shipInstance.durability !is FelinaeShipDurability) return null
			if (!shipInstance.canUseRecoalescence) return null
			
			return PlayerAbilityData.Recoalesce
		}
		
		override fun finishOnServer(gameState: GameState, playerSide: GlobalSide, data: PlayerAbilityData): GameEvent {
			if (gameState.phase !is GamePhase.Repair) return GameEvent.InvalidAction("Ships can only extinguish fires during Phase IV")
			
			val shipInstance = gameState.ships[ship] ?: return GameEvent.InvalidAction("That ship does not exist")
			if (shipInstance.durability !is FelinaeShipDurability) return GameEvent.InvalidAction("That ship cannot recoalesce its hull")
			if (!shipInstance.canUseRecoalescence) return GameEvent.InvalidAction("That ship is not in Recoalescence mode")
			
			val newHullAmountRange = (shipInstance.hullAmount + 1) until shipInstance.durability.maxHullPoints
			val (newHullAmount, newMaxHullDamage) = if (newHullAmountRange.isEmpty())
				shipInstance.hullAmount to shipInstance.recoalescenceMaxHullDamage
			else
				newHullAmountRange.random() to (shipInstance.recoalescenceMaxHullDamage + 1)
			
			val repairs = shipInstance.modulesStatus.statuses.filterValues {
				it == ShipModuleStatus.DAMAGED || it == ShipModuleStatus.DESTROYED
			}.keys
			
			var newModules = shipInstance.modulesStatus
			for (repair in repairs) {
				if (Random.nextBoolean())
					newModules = newModules.repair(repair, repairUnrepairable = true)
			}
			
			val newShip = shipInstance.copy(
				hullAmount = newHullAmount,
				recoalescenceMaxHullDamage = newMaxHullDamage,
				modulesStatus = newModules,
				isDoneCurrentPhase = true
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
	object ConfigurePower : PlayerAbilityData()
	
	@Serializable
	data class MoveShip(val newPosition: ShipPosition) : PlayerAbilityData()
	
	@Serializable
	data class UseInertialessDrive(val newPosition: Position) : PlayerAbilityData()
	
	@Serializable
	object ChargeLance : PlayerAbilityData()
	
	@Serializable
	data class UseWeapon(val target: PickResponse) : PlayerAbilityData()
	
	@Serializable
	object RecallStrikeCraft : PlayerAbilityData()
	
	@Serializable
	object DisruptionPulse : PlayerAbilityData()
	
	@Serializable
	data class BoardingParty(val target: Id<ShipInstance>) : PlayerAbilityData()
	
	@Serializable
	object RepairShipModule : PlayerAbilityData()
	
	@Serializable
	object ExtinguishFire : PlayerAbilityData()
	
	@Serializable
	object Recoalesce : PlayerAbilityData()
}

fun GameState.getPossibleAbilities(forPlayer: GlobalSide): List<PlayerAbilityType> = if (doneWithPhase == forPlayer)
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
			.filterValues { it.owner == forPlayer && !it.isDoneCurrentPhase && it.ship.reactor is StandardShipReactor }
			.keys
			.map { PlayerAbilityType.DistributePower(it) }
		
		val configurableShips = ships
			.filterValues { it.owner == forPlayer && !it.isDoneCurrentPhase && it.ship.reactor is FelinaeShipReactor }
			.keys
			.flatMap {
				FelinaeShipPowerMode.values().map { mode ->
					PlayerAbilityType.ConfigurePower(it, mode)
				}
			}
		
		val finishPowering = if (canFinishPhase(forPlayer))
			listOf(PlayerAbilityType.DonePhase(GamePhase.Power(phase.turn)))
		else emptyList()
		
		powerableShips + configurableShips + finishPowering
	}
	is GamePhase.Move -> {
		val movableShips = ships
			.filterKeys { canShipMove(it) }
			.filterValues { it.owner == forPlayer && !it.isDoneCurrentPhase }
			.keys
			.map { PlayerAbilityType.MoveShip(it) }
		
		val inertialessShips = ships
			.filterKeys { canShipMove(it) }
			.filterValues { it.owner == forPlayer && !it.isDoneCurrentPhase && it.canUseInertialessDrive }
			.keys
			.map { PlayerAbilityType.UseInertialessDrive(it) }
		
		val finishMoving = if (canFinishPhase(forPlayer))
			listOf(PlayerAbilityType.DonePhase(GamePhase.Move(phase.turn)))
		else emptyList()
		
		movableShips + inertialessShips + finishMoving
	}
	is GamePhase.Attack -> {
		val chargeableLances = ships
			.filterValues { it.owner == forPlayer && it.weaponAmount > 0 }
			.flatMap { (id, ship) ->
				ship.armaments.mapNotNull { (weaponId, weapon) ->
					PlayerAbilityType.ChargeLance(id, weaponId).takeIf {
						when (weapon) {
							is ShipWeaponInstance.Lance -> weapon.numCharges < 7.0 && weaponId !in ship.usedArmaments
							else -> false
						}
					}
				}
			}
		
		val usableWeapons = ships
			.filterKeys { canShipAttack(it) }
			.filterValues { it.owner == forPlayer }
			.flatMap { (id, ship) ->
				ship.armaments.keys.mapNotNull { weaponId ->
					PlayerAbilityType.UseWeapon(id, weaponId).takeIf {
						weaponId !in ship.usedArmaments && ship.canUseWeapon(weaponId)
					}
				}
			}
		
		val usableDisruptionPulses = ships
			.filterKeys { canShipAttack(it) }
			.filterValues { it.owner == forPlayer && !it.isDoneCurrentPhase && it.canUseDisruptionPulse }
			.keys
			.map { PlayerAbilityType.DisruptionPulse(it) }
		
		val usableBoardingTransportaria = ships
			.filterKeys { canShipAttack(it) }
			.filterValues { it.owner == forPlayer && !it.isDoneCurrentPhase && it.canSendBoardingParty }
			.keys
			.map { PlayerAbilityType.BoardingParty(it) }
		
		val recallableStrikeWings = ships
			.filterValues { it.owner == forPlayer }
			.flatMap { (id, ship) ->
				ship.armaments.mapNotNull { (weaponId, weapon) ->
					PlayerAbilityType.RecallStrikeCraft(id, weaponId).takeIf {
						weaponId in ship.usedArmaments && weapon is ShipWeaponInstance.Hangar
					}
				}
			}
		
		val finishAttacking = if (canFinishPhase(forPlayer))
			listOf(PlayerAbilityType.DonePhase(GamePhase.Attack(phase.turn)))
		else emptyList()
		
		usableBoardingTransportaria + chargeableLances + usableWeapons + recallableStrikeWings + usableDisruptionPulses + finishAttacking
	}
	is GamePhase.Repair -> {
		val repairableModules = ships
			.filterValues { it.owner == forPlayer && it.remainingRepairTokens > 0 }
			.flatMap { (id, ship) ->
				ship.modulesStatus.statuses.filterValues { it.canBeRepaired }.keys.map { module ->
					PlayerAbilityType.RepairShipModule(id, module)
				}
			}
		
		val extinguishableFires = ships
			.filterValues { it.owner == forPlayer && it.remainingRepairTokens > 0 && it.numFires > 0 }
			.keys
			.map {
				PlayerAbilityType.ExtinguishFire(it)
			}
		
		val recoalescibleShips = ships
			.filterValues { it.owner == forPlayer && it.canUseRecoalescence }
			.keys
			.map {
				PlayerAbilityType.Recoalesce(it)
			}
		
		val finishRepairing = if (canFinishPhase(forPlayer))
			listOf(PlayerAbilityType.DonePhase(GamePhase.Repair(phase.turn)))
		else emptyList()
		
		repairableModules + extinguishableFires + recoalescibleShips + finishRepairing
	}
}

object ClientAbilityData {
	val newShipPowerModes = mutableMapOf<Id<ShipInstance>, ShipPowerMode>()
}
