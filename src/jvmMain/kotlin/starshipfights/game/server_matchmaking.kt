package starshipfights.game

import io.ktor.websocket.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import starshipfights.data.admiralty.getInGameAdmiral
import starshipfights.data.auth.User

private val openSessions = ConcurrentCurator(mutableListOf<HostInvitation>())

class HostInvitation(admiral: InGameAdmiral, battleInfo: BattleInfo) {
	val joinable = Joinable(admiral, battleInfo)
	val joinInvitations = Channel<JoinInvitation>()
	
	val gameIdHandler = CompletableDeferred<String>()
}

class JoinInvitation(val joinRequest: JoinRequest, val responseHandler: CompletableDeferred<JoinResponse>) {
	val gameIdHandler = CompletableDeferred<String>()
}

suspend fun DefaultWebSocketServerSession.matchmakingEndpoint(user: User): Boolean {
	val playerLogin = receiveObject(PlayerLogin.serializer()) { closeAndReturn { return false } }
	val admiralId = playerLogin.admiral
	val inGameAdmiral = getInGameAdmiral(admiralId) ?: closeAndReturn("That admiral does not exist") { return false }
	if (inGameAdmiral.user.id != user.id) closeAndReturn("You do not own that admiral") { return false }
	
	when (val loginMode = playerLogin.login) {
		is LoginMode.Host -> {
			val battleInfo = loginMode.battleInfo
			val hostInvitation = HostInvitation(inGameAdmiral, battleInfo)
			
			openSessions.use { it.add(hostInvitation) }
			
			closeReason.invokeOnCompletion {
				hostInvitation.joinInvitations.close()
				
				@OptIn(DelicateCoroutinesApi::class)
				GlobalScope.launch {
					openSessions.use {
						it.remove(hostInvitation)
					}
				}
			}
			
			for (joinInvitation in hostInvitation.joinInvitations) {
				sendObject(JoinRequest.serializer(), joinInvitation.joinRequest)
				val joinResponse = receiveObject(JoinResponse.serializer()) {
					closeAndReturn {
						joinInvitation.responseHandler.complete(JoinResponse(false))
						return false
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
					
					val (hostId, joinId) = GameManager.initGame(inGameAdmiral, joinInvitation.joinRequest.joiner, loginMode.battleInfo)
					hostInvitation.gameIdHandler.complete(hostId)
					joinInvitation.gameIdHandler.complete(joinId)
					
					break
				}
			}
			
			val gameId = hostInvitation.gameIdHandler.await()
			sendObject(GameReady.serializer(), GameReady(gameId))
		}
		LoginMode.Join -> {
			val joinRequest = JoinRequest(inGameAdmiral)
			
			while (true) {
				val openGames = openSessions.use {
					var index = 0
					it.associateBy { "${++index}" }
				}
				val joinListing = JoinListing(openGames.mapValues { (_, invitation) -> invitation.joinable })
				sendObject(JoinListing.serializer(), joinListing)
				
				val joinSelection = receiveObject(JoinSelection.serializer()) { closeAndReturn { return false } }
				val hostInvitation = openGames.getValue(joinSelection.selectedId)
				
				val joinResponseHandler = CompletableDeferred<JoinResponse>()
				val joinInvitation = JoinInvitation(joinRequest, joinResponseHandler)
				closeReason.invokeOnCompletion {
					joinResponseHandler.cancel()
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
	
	return true
}
