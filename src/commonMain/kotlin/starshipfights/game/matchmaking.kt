package starshipfights.game

import kotlinx.serialization.Serializable
import starshipfights.data.Id

enum class BattleSize(val numPoints: Int, val maxWeightClass: ShipWeightClass, val displayName: String) {
	SKIRMISH(600, ShipWeightClass.CRUISER, "Skirmish"),
	FIREFIGHT(800, ShipWeightClass.CRUISER, "Firefight"),
	BATTLE(1000, ShipWeightClass.BATTLECRUISER, "Battle"),
	GRAND_CLASH(1300, ShipWeightClass.BATTLESHIP, "Grand Clash"),
	LEGENDARY_STRUGGLE(1600, ShipWeightClass.BATTLESHIP, "Legendary Struggle"),
	CRUCIBLE_OF_HISTORY(2000, ShipWeightClass.COLOSSUS, "Crucible of History");
}

enum class BattleBackground(val displayName: String, val color: String) {
	BLUE_BROWN("Milky Way", "#335577"),
	BLUE_MAGENTA("Arcane Anomaly", "#553377"),
	BLUE_PURPLE("Vensca Wormhole", "#444477"),
	GRAYBLUE_GRAYBROWN("Fulkreyksk Bloc", "#445566"),
	MAGENTA_PURPLE("Aedon Vortex", "#773355"),
	ORANGE_ORANGE("Solar Flare", "#775533"),
	PURPLE_MAGENTA("Shroud Rift", "#663366"),
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
sealed class LoginMode {
	abstract val globalSide: GlobalSide
	
	@Serializable
	data class Host(val battleInfo: BattleInfo) : LoginMode() {
		override val globalSide: GlobalSide
			get() = GlobalSide.HOST
	}
	
	@Serializable
	object Join : LoginMode() {
		override val globalSide: GlobalSide
			get() = GlobalSide.GUEST
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
)

@Serializable
data class JoinSelection(
	val selectedId: String
)
