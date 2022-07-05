package net.starshipfights.game

import net.starshipfights.data.admiralty.genAI
import net.starshipfights.data.admiralty.generateFleet
import net.starshipfights.data.admiralty.getAdmiralsShips
import kotlin.math.PI

fun battleSize() = ((25..35).random() * 500.0) to ((15..45).random() * 500.0)

suspend fun generate1v1GameInitialState(hostInfo: InGameAdmiral, guestInfo: InGameAdmiral, battleInfo: BattleInfo): GameState {
	val (battleWidth, battleLength) = battleSize()
	
	val deployWidth2 = battleWidth / 2
	val deployLength2 = 875.0
	
	val hostDeployCenter = Position(Vec2(0.0, (-battleLength / 2) + deployLength2))
	val guestDeployCenter = Position(Vec2(0.0, (battleLength / 2) - deployLength2))
	
	val gameStart = GameStart(
		battlefieldWidth = battleWidth, battlefieldLength = battleLength,
		
		hostStarts = mapOf(
			GlobalShipController.Player1Disambiguation to PlayerStart(
				cameraPosition = hostDeployCenter,
				cameraFacing = PI / 2,
				deployZone = PickBoundary.Rectangle(hostDeployCenter, deployWidth2, deployLength2),
				deployFacing = PI / 2,
				deployableFleet = getAdmiralsShips(hostInfo.id.reinterpret()).filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxTier }
			)
		),
		guestStarts = mapOf(
			GlobalShipController.Player1Disambiguation to PlayerStart(
				cameraPosition = guestDeployCenter,
				cameraFacing = -PI / 2,
				deployZone = PickBoundary.Rectangle(guestDeployCenter, deployWidth2, deployLength2),
				deployFacing = -PI / 2,
				deployableFleet = getAdmiralsShips(guestInfo.id.reinterpret()).filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxTier }
			)
		),
	)
	
	return GameState(
		start = gameStart,
		hostInfo = mapOf(GlobalShipController.Player1Disambiguation to hostInfo),
		guestInfo = mapOf(GlobalShipController.Player1Disambiguation to guestInfo),
		battleInfo = battleInfo,
		subplots = generateSubplots(
			battleInfo.size,
			GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation)
		) + generateSubplots(
			battleInfo.size,
			GlobalShipController(GlobalSide.GUEST, GlobalShipController.Player1Disambiguation)
		)
	)
}

suspend fun generate2v1GameInitialState(player1Info: InGameAdmiral, player2Info: InGameAdmiral, enemyFaction: Faction, enemyFlavor: FactionFlavor, battleInfo: BattleInfo): GameState {
	val (battleWidth, battleLength) = battleSize()
	
	val deployWidth2 = battleWidth / 2
	val deployLength2 = 875.0
	
	val deployWidth4 = deployWidth2 / 2
	
	val hostDeployCenter1 = Position(Vec2(deployWidth4, (-battleLength / 2) + deployLength2))
	val hostDeployCenter2 = Position(Vec2(-deployWidth4, (-battleLength / 2) + deployLength2))
	val guestDeployCenter = Position(Vec2(0.0, (battleLength / 2) - deployLength2))
	
	val aiAdmiral = genAI(enemyFaction, battleInfo.size)
	
	val gameStart = GameStart(
		battlefieldWidth = battleWidth, battlefieldLength = battleLength,
		
		hostStarts = mapOf(
			GlobalShipController.Player1Disambiguation to PlayerStart(
				cameraPosition = hostDeployCenter1,
				cameraFacing = PI / 2,
				deployZone = PickBoundary.Rectangle(hostDeployCenter1, deployWidth4, deployLength2),
				deployFacing = PI / 2,
				deployableFleet = getAdmiralsShips(player1Info.id.reinterpret()).filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxTier },
				deployPointsFactor = 0.75
			),
			GlobalShipController.Player2Disambiguation to PlayerStart(
				cameraPosition = hostDeployCenter2,
				cameraFacing = PI / 2,
				deployZone = PickBoundary.Rectangle(hostDeployCenter2, deployWidth4, deployLength2),
				deployFacing = PI / 2,
				deployableFleet = getAdmiralsShips(player2Info.id.reinterpret()).filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxTier },
				deployPointsFactor = 0.75
			)
		),
		
		guestStarts = mapOf(
			GlobalShipController.Player1Disambiguation to PlayerStart(
				cameraPosition = guestDeployCenter,
				cameraFacing = -PI / 2,
				deployZone = PickBoundary.Rectangle(guestDeployCenter, deployWidth2, deployLength2),
				deployFacing = -PI / 2,
				deployableFleet = generateFleet(aiAdmiral, enemyFlavor)
					.associate { it.shipData.id to it.shipData }
					.filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxTier },
				deployPointsFactor = 1.75
			)
		),
	)
	
	return GameState(
		start = gameStart,
		hostInfo = mapOf(
			GlobalShipController.Player1Disambiguation to player1Info,
			GlobalShipController.Player2Disambiguation to player2Info,
		),
		guestInfo = mapOf(
			GlobalShipController.Player1Disambiguation to InGameAdmiral(
				id = aiAdmiral.id.reinterpret(),
				user = InGameUser(
					id = aiAdmiral.owningUser.reinterpret(),
					username = aiAdmiral.name
				),
				name = aiAdmiral.name,
				isFemale = aiAdmiral.isFemale,
				faction = aiAdmiral.faction,
				rank = aiAdmiral.rank
			)
		),
		battleInfo = battleInfo,
		subplots = generateSubplots(
			battleInfo.size,
			GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation)
		) + generateSubplots(
			battleInfo.size,
			GlobalShipController(GlobalSide.HOST, GlobalShipController.Player2Disambiguation)
		)
	)
}

suspend fun generateTrainingInitialState(playerInfo: InGameAdmiral, enemyFaction: Faction, enemyFlavor: FactionFlavor, battleInfo: BattleInfo): GameState {
	val (battleWidth, battleLength) = battleSize()
	
	val deployWidth2 = battleWidth / 2
	val deployLength2 = 875.0
	
	val hostDeployCenter = Position(Vec2(0.0, (-battleLength / 2) + deployLength2))
	val guestDeployCenter = Position(Vec2(0.0, (battleLength / 2) - deployLength2))
	
	val aiAdmiral = genAI(enemyFaction, battleInfo.size)
	
	return GameState(
		start = GameStart(
			battleWidth, battleLength,
			
			mapOf(
				GlobalShipController.Player1Disambiguation to PlayerStart(
					cameraPosition = hostDeployCenter,
					cameraFacing = PI / 2,
					deployZone = PickBoundary.Rectangle(hostDeployCenter, deployWidth2, deployLength2),
					deployFacing = PI / 2,
					deployableFleet = getAdmiralsShips(playerInfo.id.reinterpret())
						.filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxTier }
				)
			),
			
			mapOf(
				GlobalShipController.Player1Disambiguation to PlayerStart(
					cameraPosition = guestDeployCenter,
					cameraFacing = -PI / 2,
					deployZone = PickBoundary.Rectangle(guestDeployCenter, deployWidth2, deployLength2),
					deployFacing = -PI / 2,
					deployableFleet = generateFleet(aiAdmiral, enemyFlavor)
						.associate { it.shipData.id to it.shipData }
						.filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxTier }
				)
			)
		),
		hostInfo = mapOf(GlobalShipController.Player1Disambiguation to playerInfo),
		guestInfo = mapOf(
			GlobalShipController.Player1Disambiguation to InGameAdmiral(
				id = aiAdmiral.id.reinterpret(),
				user = InGameUser(
					id = aiAdmiral.owningUser.reinterpret(),
					username = aiAdmiral.name
				),
				name = aiAdmiral.name,
				isFemale = aiAdmiral.isFemale,
				faction = aiAdmiral.faction,
				rank = aiAdmiral.rank
			)
		),
		battleInfo = battleInfo,
		subplots = generateSubplots(battleInfo.size, GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation))
	)
}
