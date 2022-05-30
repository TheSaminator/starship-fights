package starshipfights.game

import starshipfights.data.admiralty.genAI
import starshipfights.data.admiralty.generateFleet
import starshipfights.data.admiralty.getAdmiralsShips
import kotlin.math.PI

suspend fun generateGameStart(hostInfo: InGameAdmiral, guestInfo: InGameAdmiral, battleInfo: BattleInfo): GameStart {
	val battleWidth = (25..35).random() * 500.0
	val battleLength = (15..45).random() * 500.0
	
	val deployWidth2 = battleWidth / 2
	val deployLength2 = 875.0
	
	val hostDeployCenter = Position(Vec2(0.0, (-battleLength / 2) + deployLength2))
	val guestDeployCenter = Position(Vec2(0.0, (battleLength / 2) - deployLength2))
	
	return GameStart(
		battleWidth, battleLength,
		
		PlayerStart(
			hostDeployCenter,
			PI / 2,
			PickBoundary.Rectangle(hostDeployCenter, deployWidth2, deployLength2),
			PI / 2,
			getAdmiralsShips(hostInfo.id.reinterpret()).filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxWeightClass.tier }
		),
		
		PlayerStart(
			guestDeployCenter,
			-PI / 2,
			PickBoundary.Rectangle(guestDeployCenter, deployWidth2, deployLength2),
			-PI / 2,
			getAdmiralsShips(guestInfo.id.reinterpret()).filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxWeightClass.tier }
		),
	)
}

suspend fun generateTrainingInitialState(playerInfo: InGameAdmiral, enemyFaction: Faction, battleInfo: BattleInfo): GameState {
	val battleWidth = (25..35).random() * 500.0
	val battleLength = (15..45).random() * 500.0
	
	val deployWidth2 = battleWidth / 2
	val deployLength2 = 875.0
	
	val hostDeployCenter = Position(Vec2(0.0, (-battleLength / 2) + deployLength2))
	val guestDeployCenter = Position(Vec2(0.0, (battleLength / 2) - deployLength2))
	
	val aiAdmiral = genAI(enemyFaction, battleInfo.size)
	
	return GameState(
		GameStart(
			battleWidth, battleLength,
			
			PlayerStart(
				hostDeployCenter,
				PI / 2,
				PickBoundary.Rectangle(hostDeployCenter, deployWidth2, deployLength2),
				PI / 2,
				getAdmiralsShips(playerInfo.id.reinterpret())
					.filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxWeightClass.tier }
			),
			
			PlayerStart(
				guestDeployCenter,
				-PI / 2,
				PickBoundary.Rectangle(guestDeployCenter, deployWidth2, deployLength2),
				-PI / 2,
				generateFleet(aiAdmiral)
					.associate { it.shipData.id to it.shipData }
					.filterValues { it.shipType.weightClass.tier <= battleInfo.size.maxWeightClass.tier }
			)
		),
		playerInfo,
		InGameAdmiral(
			id = aiAdmiral.id.reinterpret(),
			user = InGameUser(
				id = aiAdmiral.owningUser.reinterpret(),
				username = aiAdmiral.name
			),
			name = aiAdmiral.name,
			isFemale = aiAdmiral.isFemale,
			faction = aiAdmiral.faction,
			rank = aiAdmiral.rank
		),
		battleInfo
	)
}
