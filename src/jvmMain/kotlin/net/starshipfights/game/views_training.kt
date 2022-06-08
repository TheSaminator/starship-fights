package net.starshipfights.game

import io.ktor.application.*
import io.ktor.request.*
import net.starshipfights.auth.getUserSession
import net.starshipfights.data.Id
import net.starshipfights.data.admiralty.Admiral
import net.starshipfights.data.admiralty.getInGameAdmiral
import net.starshipfights.redirect

suspend fun ApplicationCall.getTrainingClientMode(): ClientMode {
	val userId = getUserSession()?.user ?: redirect("/login")
	val parameters = receiveParameters()
	
	val admiralId = parameters["admiral"]?.let { Id<Admiral>(it) } ?: return ClientMode.Error("An admiral must be specified")
	val admiralData = getInGameAdmiral(admiralId.reinterpret()) ?: return ClientMode.Error("That admiral does not exist")
	
	if (admiralData.user.id != userId.reinterpret<InGameUser>()) return ClientMode.Error("You do not own that admiral")
	
	val battleSize = BattleSize.values().singleOrNull { it.toUrlSlug() == parameters["battle-size"] } ?: return ClientMode.Error("Invalid battle size")
	val battleBg = BattleBackground.values().singleOrNull { it.toUrlSlug() == parameters["battle-bg"] } ?: return ClientMode.Error("Invalid battle background")
	val battleInfo = BattleInfo(battleSize, battleBg)
	
	val enemyFaction = Faction.values().singleOrNull { it.toUrlSlug() == parameters["enemy-faction"] } ?: Faction.values().random()
	
	val initialState = generateTrainingInitialState(admiralData, enemyFaction, battleInfo)
	
	return ClientMode.InTrainingGame(initialState)
}
