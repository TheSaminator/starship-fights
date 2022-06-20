package net.starshipfights.game

sealed class MainMenuOption {
	object Singleplayer : MainMenuOption()
	
	data class Multiplayer1v1(val side: GlobalSide) : MainMenuOption()
	
	data class Multiplayer2v1(val player: Player2v1) : MainMenuOption()
}

enum class Player2v1 {
	PLAYER_1,
	PLAYER_2;
}

sealed class AIFactionChoice {
	object Random : AIFactionChoice()
	
	data class Chosen(val faction: Faction) : AIFactionChoice()
}

sealed class AIFactionFlavorChoice {
	object Random : AIFactionFlavorChoice()
	
	data class Chosen(val flavor: FactionFlavor) : AIFactionFlavorChoice()
}

private suspend fun Popup.Companion.getPlayerInfo(admirals: List<InGameAdmiral>): InGameAdmiral {
	return Popup.ChooseAdmiralScreen(admirals).display()
}

private suspend fun Popup.Companion.getBattleInfo(admiral: InGameAdmiral): BattleInfo? {
	val battleSize = Popup.ChooseBattleSizeScreen(admiral.rank.maxBattleSize).display() ?: return null
	val battleBackground = Popup.ChooseBattleBackgroundScreen.display() ?: return getBattleInfo(admiral)
	return BattleInfo(battleSize, battleBackground)
}

private suspend fun Popup.Companion.getTrainingOpponent(): TrainingOpponent? {
	val faction = Popup.ChooseEnemyFactionScreen.display() ?: return null
	return when (faction) {
		is AIFactionChoice.Chosen -> {
			val flavor = Popup.ChooseEnemyFactionFlavorScreen(faction.faction).display() ?: return getTrainingOpponent()
			when (flavor) {
				is AIFactionFlavorChoice.Chosen -> TrainingOpponent.FactionAndFlavor(faction.faction, flavor.flavor)
				AIFactionFlavorChoice.Random -> TrainingOpponent.FactionWithRandomFlavor(faction.faction)
			}
		}
		AIFactionChoice.Random -> TrainingOpponent.RandomFaction
	}
}

private suspend fun Popup.Companion.getTrainingInfo(admiral: InGameAdmiral): LoginMode? {
	val battleInfo = getBattleInfo(admiral) ?: return getLoginMode(admiral)
	val opponent = getTrainingOpponent() ?: return getTrainingInfo(admiral)
	
	return LoginMode.Train(battleInfo, opponent)
}

private suspend fun Popup.Companion.get2v1HostInfo(admiral: InGameAdmiral): LoginMode? {
	val battleInfo = getBattleInfo(admiral) ?: return getLoginMode(admiral)
	val opponent = getTrainingOpponent() ?: return get2v1HostInfo(admiral)
	
	return LoginMode.Host2v1(battleInfo, opponent)
}

private suspend fun Popup.Companion.getLoginMode(admiral: InGameAdmiral): LoginMode? {
	val mainMenuOption = Popup.MainMenuScreen(admiral).display() ?: return null
	return when (mainMenuOption) {
		MainMenuOption.Singleplayer -> getTrainingInfo(admiral)
		is MainMenuOption.Multiplayer1v1 -> when (mainMenuOption.side) {
			GlobalSide.HOST -> LoginMode.Host1v1(getBattleInfo(admiral) ?: return getLoginMode(admiral))
			GlobalSide.GUEST -> LoginMode.Join1v1
		}
		is MainMenuOption.Multiplayer2v1 -> when (mainMenuOption.player) {
			Player2v1.PLAYER_1 -> get2v1HostInfo(admiral)
			Player2v1.PLAYER_2 -> LoginMode.Join2v1
		}
	}
}

suspend fun Popup.Companion.getPlayerLogin(admirals: List<InGameAdmiral>, cachedAdmiral: InGameAdmiral? = null): PlayerLogin {
	val admiral = cachedAdmiral ?: getPlayerInfo(admirals)
	val loginMode = getLoginMode(admiral) ?: return getPlayerLogin(admirals, null)
	return PlayerLogin(admiral.id, loginMode)
}
