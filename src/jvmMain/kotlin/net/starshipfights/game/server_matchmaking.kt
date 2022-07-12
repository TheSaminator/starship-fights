package net.starshipfights.game

import io.ktor.websocket.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import net.starshipfights.data.admiralty.getInGameAdmiral
import net.starshipfights.data.admiralty.lockAdmiral
import net.starshipfights.data.admiralty.unlockAdmiral
import net.starshipfights.data.auth.User

private val open1v1Sessions = ConcurrentCurator(mutableListOf<Host1v1Invitation>())

class Host1v1Invitation(admiral: InGameAdmiral, battleInfo: BattleInfo) {
	val joinable = Joinable(admiral, battleInfo, null)
	val joinInvitations = Channel<Join1v1Invitation>()
	
	val gameIdHandler = CompletableDeferred<String>()
}

class Join1v1Invitation(val joinRequest: JoinRequest, val responseHandler: CompletableDeferred<JoinResponse>) {
	val gameIdHandler = CompletableDeferred<String>()
}

private val open2v1Sessions = ConcurrentCurator(mutableListOf<Host2v1Invitation>())

class Host2v1Invitation(admiral: InGameAdmiral, battleInfo: BattleInfo, opponent: TrainingOpponent) {
	val joinable = Joinable(admiral, battleInfo, opponent.faction)
	val joinInvitations = Channel<Join2v1Invitation>()
	
	val gameIdHandler = CompletableDeferred<String>()
}

class Join2v1Invitation(val joinRequest: JoinRequest, val responseHandler: CompletableDeferred<JoinResponse>) {
	val gameIdHandler = CompletableDeferred<String>()
}

suspend fun DefaultWebSocketServerSession.matchmakingEndpoint(user: User) {
	val playerLogin = receiveObject(PlayerLogin.serializer()) { closeAndReturn { return } }
	val admiralId = playerLogin.admiral
	val inGameAdmiral = getInGameAdmiral(admiralId) ?: closeAndReturn("That admiral does not exist") { return }
	if (inGameAdmiral.user.id != user.id.reinterpret<InGameUser>()) closeAndReturn("You do not own that admiral") { return }
	
	if (!lockAdmiral(admiralId.reinterpret()))
		closeAndReturn("That admiral is not available") { return }
	
	when (val loginMode = playerLogin.login) {
		is LoginMode.Train -> {
			closeAndReturn("Invalid input: LoginMode.Train should redirect you directly to training endpoint") { return unlockAdmiral(admiralId.reinterpret()) }
		}
		is LoginMode.Host1v1 -> {
			val battleInfo = loginMode.battleInfo
			val hostInvitation = Host1v1Invitation(inGameAdmiral, battleInfo)
			
			open1v1Sessions.use { it.add(hostInvitation) }
			
			closeReason.invokeOnCompletion {
				hostInvitation.joinInvitations.close()
				
				@OptIn(DelicateCoroutinesApi::class)
				GlobalScope.launch {
					unlockAdmiral(admiralId.reinterpret())
					
					open1v1Sessions.use {
						it.remove(hostInvitation)
					}
				}
			}
			
			for (joinInvitation in hostInvitation.joinInvitations) {
				sendObject(JoinRequest.serializer(), joinInvitation.joinRequest)
				val joinResponse = receiveObject(JoinResponse.serializer()) {
					closeAndReturn {
						joinInvitation.responseHandler.complete(JoinResponse(false))
						return unlockAdmiral(admiralId.reinterpret())
					}
				}
				
				if (joinInvitation.responseHandler.isCancelled) {
					if (joinResponse.accepted)
						sendObject(JoinResponseResponse.serializer(), JoinResponseResponse(false))
					continue
				}
				
				joinInvitation.responseHandler.complete(joinResponse)
				
				if (joinResponse.accepted) {
					sendObject(JoinResponseResponse.serializer(), JoinResponseResponse(true))
					
					val (hostId, joinId) = GameManager.init1v1Game(inGameAdmiral, joinInvitation.joinRequest.joiner, loginMode.battleInfo)
					hostInvitation.gameIdHandler.complete(hostId)
					joinInvitation.gameIdHandler.complete(joinId)
					
					break
				}
			}
			
			val gameId = hostInvitation.gameIdHandler.await()
			sendObject(GameReady.serializer(), GameReady(gameId))
		}
		LoginMode.Join1v1 -> {
			val joinRequest = JoinRequest(inGameAdmiral)
			
			while (true) {
				val openGames = open1v1Sessions.use {
					it.toList()
				}.filter { sess ->
					sess.joinable.battleInfo.size <= inGameAdmiral.rank.maxBattleSize
				}.mapIndexed { i, host -> "$i" to host }.toMap()
				
				val joinListing = JoinListing(openGames.mapValues { (_, invitation) -> invitation.joinable })
				sendObject(JoinListing.serializer(), joinListing)
				
				val joinSelection = receiveObject(JoinSelection.serializer()) { closeAndReturn { return unlockAdmiral(admiralId.reinterpret()) } }
				val hostInvitation = openGames.getValue(joinSelection.selectedId)
				
				val joinResponseHandler = CompletableDeferred<JoinResponse>()
				val joinInvitation = Join1v1Invitation(joinRequest, joinResponseHandler)
				closeReason.invokeOnCompletion {
					joinResponseHandler.cancel()
					
					@OptIn(DelicateCoroutinesApi::class)
					GlobalScope.launch {
						unlockAdmiral(admiralId.reinterpret())
					}
				}
				
				try {
					hostInvitation.joinInvitations.send(joinInvitation)
				} catch (ex: ClosedSendChannelException) {
					sendObject(JoinResponse.serializer(), JoinResponse(false))
					continue
				}
				
				val joinResponse = joinResponseHandler.await()
				sendObject(JoinResponse.serializer(), joinResponse)
				
				if (joinResponse.accepted) {
					val gameId = joinInvitation.gameIdHandler.await()
					sendObject(GameReady.serializer(), GameReady(gameId))
					break
				}
			}
		}
		is LoginMode.Host2v1 -> {
			val battleInfo = loginMode.battleInfo
			val hostInvitation = Host2v1Invitation(inGameAdmiral, battleInfo, loginMode.enemyFaction)
			
			open2v1Sessions.use { it.add(hostInvitation) }
			
			closeReason.invokeOnCompletion {
				hostInvitation.joinInvitations.close()
				
				@OptIn(DelicateCoroutinesApi::class)
				GlobalScope.launch {
					unlockAdmiral(admiralId.reinterpret())
					
					open2v1Sessions.use {
						it.remove(hostInvitation)
					}
				}
			}
			
			for (joinInvitation in hostInvitation.joinInvitations) {
				sendObject(JoinRequest.serializer(), joinInvitation.joinRequest)
				val joinResponse = receiveObject(JoinResponse.serializer()) {
					closeAndReturn {
						joinInvitation.responseHandler.complete(JoinResponse(false))
						return unlockAdmiral(admiralId.reinterpret())
					}
				}
				
				if (joinInvitation.responseHandler.isCancelled) {
					if (joinResponse.accepted)
						sendObject(JoinResponseResponse.serializer(), JoinResponseResponse(false))
					continue
				}
				
				joinInvitation.responseHandler.complete(joinResponse)
				
				if (joinResponse.accepted) {
					sendObject(JoinResponseResponse.serializer(), JoinResponseResponse(true))
					
					val enemyFaction = loginMode.enemyFaction.faction ?: Faction.values().random()
					val enemyFlavor = loginMode.enemyFaction.flavor ?: FactionFlavor.optionsForAiEnemy(enemyFaction).random()
					
					val (hostId, joinId) = GameManager.init2v1Game(inGameAdmiral, joinInvitation.joinRequest.joiner, enemyFaction, enemyFlavor, loginMode.battleInfo)
					hostInvitation.gameIdHandler.complete(hostId)
					joinInvitation.gameIdHandler.complete(joinId)
					
					break
				}
			}
			
			val gameId = hostInvitation.gameIdHandler.await()
			sendObject(GameReady.serializer(), GameReady(gameId))
		}
		LoginMode.Join2v1 -> {
			val joinRequest = JoinRequest(inGameAdmiral)
			
			while (true) {
				val openGames = open2v1Sessions.use {
					it.toList()
				}.filter { sess ->
					sess.joinable.battleInfo.size <= inGameAdmiral.rank.maxBattleSize
				}.mapIndexed { i, host -> "$i" to host }.toMap()
				
				val joinListing = JoinListing(openGames.mapValues { (_, invitation) -> invitation.joinable })
				sendObject(JoinListing.serializer(), joinListing)
				
				val joinSelection = receiveObject(JoinSelection.serializer()) { closeAndReturn { return unlockAdmiral(admiralId.reinterpret()) } }
				val hostInvitation = openGames.getValue(joinSelection.selectedId)
				
				val joinResponseHandler = CompletableDeferred<JoinResponse>()
				val joinInvitation = Join2v1Invitation(joinRequest, joinResponseHandler)
				closeReason.invokeOnCompletion {
					joinResponseHandler.cancel()
					
					@OptIn(DelicateCoroutinesApi::class)
					GlobalScope.launch {
						unlockAdmiral(admiralId.reinterpret())
					}
				}
				
				try {
					hostInvitation.joinInvitations.send(joinInvitation)
				} catch (ex: ClosedSendChannelException) {
					sendObject(JoinResponse.serializer(), JoinResponse(false))
					continue
				}
				
				val joinResponse = joinResponseHandler.await()
				sendObject(JoinResponse.serializer(), joinResponse)
				
				if (joinResponse.accepted) {
					val gameId = joinInvitation.gameIdHandler.await()
					sendObject(GameReady.serializer(), GameReady(gameId))
					break
				}
			}
		}
	}
}
