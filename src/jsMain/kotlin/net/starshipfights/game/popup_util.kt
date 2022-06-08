package net.starshipfights.game

sealed class MainMenuOption {
	object Singleplayer : MainMenuOption()
	
	data class Multiplayer(val side: GlobalSide) : MainMenuOption()
}

sealed class AIFactionChoice {
	object Random : AIFactionChoice()
	
	data class Chosen(val faction: Faction) : AIFactionChoice()
}

private suspend fun Popup.Companion.getPlayerInfo(admirals: List<InGameAdmiral>): InGameAdmiral {
	return Popup.ChooseAdmiralScreen(admirals).display()
}

private suspend fun Popup.Companion.getBattleInfo(admiral: InGameAdmiral): BattleInfo? {
	val battleSize = Popup.ChooseBattleSizeScreen(admiral.rank.maxBattleSize).display() ?: return null
	val battleBackground = Popup.ChooseBattleBackgroundScreen.display() ?: return getBattleInfo(admiral)
	return BattleInfo(battleSize, battleBackground)
}

private suspend fun Popup.Companion.getTrainingInfo(admiral: InGameAdmiral): LoginMode? {
	val battleInfo = getBattleInfo(admiral) ?: return getLoginMode(admiral)
	val faction = Popup.ChooseEnemyFactionScreen.display() ?: return getLoginMode(admiral)
	return LoginMode.Train(battleInfo, (faction as? AIFactionChoice.Chosen)?.faction)
}

private suspend fun Popup.Companion.getLoginMode(admiral: InGameAdmiral): LoginMode? {
	val mainMenuOption = Popup.MainMenuScreen(admiral).display() ?: return null
	return when (mainMenuOption) {
		MainMenuOption.Singleplayer -> getTrainingInfo(admiral)
		is MainMenuOption.Multiplayer -> when (mainMenuOption.side) {
			GlobalSide.HOST -> LoginMode.Host(getBattleInfo(admiral) ?: return getLoginMode(admiral))
			GlobalSide.GUEST -> LoginMode.Join
		}
	}
}

suspend fun Popup.Companion.getPlayerLogin(admirals: List<InGameAdmiral>, cachedAdmiral: InGameAdmiral? = null): PlayerLogin {
	val admiral = cachedAdmiral ?: getPlayerInfo(admirals)
	val loginMode = getLoginMode(admiral) ?: return getPlayerLogin(admirals, null)
	return PlayerLogin(admiral.id, loginMode)
}
