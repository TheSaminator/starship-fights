package net.starshipfights.game

import kotlinx.serialization.Serializable
import net.starshipfights.data.Id

enum class BattleSize(val numPoints: Int, val maxTier: ShipTier, val minRank: AdmiralRank, val displayName: String) {
	SKIRMISH(600, ShipTier.CRUISER, AdmiralRank.REAR_ADMIRAL, "Skirmish"),
	RAID(800, ShipTier.CRUISER, AdmiralRank.REAR_ADMIRAL, "Raid"),
	FIREFIGHT(1000, ShipTier.BATTLECRUISER, AdmiralRank.REAR_ADMIRAL, "Firefight"),
	BATTLE(1300, ShipTier.BATTLECRUISER, AdmiralRank.REAR_ADMIRAL, "Battle"),
	GRAND_CLASH(1600, ShipTier.BATTLESHIP, AdmiralRank.ADMIRAL, "Grand Clash"),
	APOCALYPSE(2000, ShipTier.BATTLESHIP, AdmiralRank.ADMIRAL, "Apocalypse"),
	LEGENDARY_STRUGGLE(2400, ShipTier.TITAN, AdmiralRank.ADMIRAL, "Legendary Struggle"),
	CRUCIBLE_OF_HISTORY(3000, ShipTier.TITAN, AdmiralRank.ADMIRAL, "Crucible of History");
}

val BattleSize.numSubplotsPerPlayer: Int
	get() = when (this) {
		BattleSize.SKIRMISH -> 0
		BattleSize.RAID -> 0
		BattleSize.FIREFIGHT -> (0..1).random()
		BattleSize.BATTLE -> 1
		BattleSize.GRAND_CLASH -> 1
		BattleSize.APOCALYPSE -> (1..2).random()
		BattleSize.LEGENDARY_STRUGGLE -> 2
		BattleSize.CRUCIBLE_OF_HISTORY -> 2
	}

enum class BattleBackground(val displayName: String, val color: String) {
	BLUE_BROWN("Milky Way", "#335577"),
	BLUE_MAGENTA("Arcane Anomaly", "#553377"),
	BLUE_PURPLE("Vensca Wormhole", "#444477"),
	BLUE_GREEN("Radiation Risk", "#337755"),
	GRAYBLUE_GRAYBROWN("Fulkreyksk Bloc", "#445566"),
	MAGENTA_PURPLE("Aedon Vortex", "#773355"),
	ORANGE_ORANGE("Solar Flare", "#775533"),
	PURPLE_MAGENTA("Veil Rift", "#663366"),
}

@Serializable
data class BattleInfo(
	val size: BattleSize,
	val bg: BattleBackground,
)

// PACKETS
@Serializable
data class PlayerLogin(
	val admiral: Id<InGameAdmiral>,
	val login: LoginMode,
)

@Serializable
sealed class TrainingOpponent {
	abstract val faction: Faction?
	abstract val flavor: FactionFlavor?
	
	@Serializable
	object RandomFaction : TrainingOpponent() {
		override val faction: Faction?
			get() = null
		
		override val flavor: FactionFlavor?
			get() = null
	}
	
	@Serializable
	data class FactionWithRandomFlavor(override val faction: Faction) : TrainingOpponent() {
		override val flavor: FactionFlavor?
			get() = null
	}
	
	@Serializable
	data class FactionAndFlavor(override val faction: Faction, override val flavor: FactionFlavor) : TrainingOpponent()
}

@Serializable
sealed class LoginMode {
	abstract val mySide: GlobalShipController?
	abstract val otherPlayerSide: GlobalShipController?
	
	@Serializable
	data class Train(val battleInfo: BattleInfo, val enemyFaction: TrainingOpponent) : LoginMode() {
		override val mySide: GlobalShipController?
			get() = null
		
		override val otherPlayerSide: GlobalShipController?
			get() = null
	}
	
	@Serializable
	data class Host1v1(val battleInfo: BattleInfo) : LoginMode() {
		override val mySide: GlobalShipController
			get() = GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation)
		
		override val otherPlayerSide: GlobalShipController
			get() = GlobalShipController(GlobalSide.GUEST, GlobalShipController.Player1Disambiguation)
	}
	
	@Serializable
	object Join1v1 : LoginMode() {
		override val mySide: GlobalShipController
			get() = GlobalShipController(GlobalSide.GUEST, GlobalShipController.Player1Disambiguation)
		
		override val otherPlayerSide: GlobalShipController
			get() = GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation)
	}
	
	@Serializable
	data class Host2v1(val battleInfo: BattleInfo, val enemyFaction: TrainingOpponent) : LoginMode() {
		override val mySide: GlobalShipController
			get() = GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation)
		
		override val otherPlayerSide: GlobalShipController
			get() = GlobalShipController(GlobalSide.HOST, GlobalShipController.Player2Disambiguation)
	}
	
	@Serializable
	object Join2v1 : LoginMode() {
		override val mySide: GlobalShipController
			get() = GlobalShipController(GlobalSide.HOST, GlobalShipController.Player2Disambiguation)
		
		override val otherPlayerSide: GlobalShipController
			get() = GlobalShipController(GlobalSide.HOST, GlobalShipController.Player1Disambiguation)
	}
}

@Serializable
data class GameReady(val connectToken: String)

// HOST FLOW
@Serializable
data class JoinRequest(
	val joiner: InGameAdmiral
)

@Serializable
data class JoinResponse(
	val accepted: Boolean
)

@Serializable
data class JoinResponseResponse(
	val connected: Boolean
)

// GUEST FLOW
@Serializable
data class JoinListing(
	val openGames: Map<String, Joinable>
)

@Serializable
data class Joinable(
	val admiral: InGameAdmiral,
	val battleInfo: BattleInfo,
	val enemyFaction: Faction?,
)

@Serializable
data class JoinSelection(
	val selectedId: String
)
