package starshipfights.game

private suspend fun Popup.Companion.getPlayerInfo(admirals: List<InGameAdmiral>): InGameAdmiral {
	return Popup.ChooseAdmiralScreen(admirals).display()
}

private suspend fun Popup.Companion.getBattleInfo(admiral: InGameAdmiral): BattleInfo? {
	val battleSize = Popup.ChooseBattleSizeScreen(admiral.rank.maxBattleSize).display() ?: return null
	val battleBackground = Popup.ChooseBattleBackgroundScreen.display() ?: return getBattleInfo(admiral)
	return BattleInfo(battleSize, battleBackground)
}

private suspend fun Popup.Companion.getLoginMode(admiral: InGameAdmiral): LoginMode? {
	val globalSide = Popup.MainMenuScreen(admiral).display() ?: return null
	return when (globalSide) {
		GlobalSide.HOST -> LoginMode.Host(getBattleInfo(admiral) ?: return getLoginMode(admiral))
		GlobalSide.GUEST -> LoginMode.Join
	}
}

suspend fun Popup.Companion.getPlayerLogin(admirals: List<InGameAdmiral>, cachedAdmiral: InGameAdmiral? = null): PlayerLogin {
	val admiral = cachedAdmiral ?: getPlayerInfo(admirals)
	val loginMode = getLoginMode(admiral) ?: return getPlayerLogin(admirals, null)
	return PlayerLogin(admiral.id, loginMode)
}
