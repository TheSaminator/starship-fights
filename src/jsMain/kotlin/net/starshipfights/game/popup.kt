package net.starshipfights.game

import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLDivElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

sealed class Popup<out T> {
	protected abstract fun TagConsumer<*>.render(context: CoroutineContext, callback: (T) -> Unit)
	private fun renderInto(consumer: TagConsumer<*>, context: CoroutineContext, callback: (T) -> Unit) {
		consumer.render(context, callback)
	}
	
	suspend fun display(): T = popupMutex.withLock {
		suspendCancellableCoroutine { continuation ->
			popupBox.clear()
			
			popupBox.append {
				renderInto(this, continuation.context) {
					hide()
					continuation.resume(it)
				}
			}
			
			continuation.invokeOnCancellation {
				hide()
			}
			
			show()
		}
	}
	
	companion object {
		private val popupMutex = Mutex()
		
		private val popup by lazy {
			document.getElementById("popup").unsafeCast<HTMLDivElement>()
		}
		
		private val popupBox by lazy {
			document.getElementById("popup-box").unsafeCast<HTMLDivElement>()
		}
		
		private fun show() {
			popup.removeClass("hide")
		}
		
		private fun hide() {
			popup.addClass("hide")
		}
	}
	
	// Game matchmaking popups
	class ChooseAdmiralScreen(private val admirals: List<InGameAdmiral>) : Popup<InGameAdmiral>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (InGameAdmiral) -> Unit) {
			if (admirals.isEmpty()) {
				p {
					style = "text-align:center"
					+"You do not have any admirals! You can fix that by "
					a("/admiral/new") { +"creating one" }
					+"."
				}
				return
			}
			
			p {
				style = "text-align:center"
				
				+"Select one of your admirals to continue:"
			}
			
			div(classes = "button-set col") {
				for (admiral in admirals) {
					button {
						+admiral.fullName
						+Entities.nbsp
						img(alt = "(${admiral.faction.shortName})", src = admiral.faction.flagUrl) {
							style = "width:1.2em;height:0.75em"
						}
						
						onClickFunction = { e ->
							e.preventDefault()
							
							callback(admiral)
						}
					}
				}
			}
			
			p {
				style = "text-align:center"
				
				+"Or return to "
				a(href = "/me") { +"your user page" }
				+"."
			}
		}
	}
	
	class MainMenuScreen(private val admiralInfo: InGameAdmiral) : Popup<MainMenuOption?>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (MainMenuOption?) -> Unit) {
			p {
				style = "text-align:center"
				
				img(alt = "Starship Fights", src = RenderResources.LOGO_URL) {
					style = "width:50%"
				}
			}
			
			p {
				style = "text-align:center"
				
				+"Welcome to Starship Fights! You are "
				+admiralInfo.fullName
				+", fighting for "
				+admiralInfo.faction.getDefiniteShortName()
				+". "
				a(href = "#") {
					+"Not you?"
					onClickFunction = { e ->
						e.preventDefault()
						callback(null)
					}
				}
			}
			
			div(classes = "button-set col") {
				button {
					+"Play Singleplayer Battle"
					onClickFunction = { e ->
						e.preventDefault()
						
						callback(MainMenuOption.Singleplayer)
					}
				}
				button {
					+"Host Competitive (1v1) Battle"
					onClickFunction = { e ->
						e.preventDefault()
						
						callback(MainMenuOption.Multiplayer1v1(GlobalSide.HOST))
					}
				}
				button {
					+"Join Competitive (1v1) Battle"
					onClickFunction = { e ->
						e.preventDefault()
						
						callback(MainMenuOption.Multiplayer1v1(GlobalSide.GUEST))
					}
				}
				button {
					+"Host Cooperative (2v1) Battle"
					onClickFunction = { e ->
						e.preventDefault()
						
						callback(MainMenuOption.Multiplayer2v1(Player2v1.PLAYER_1))
					}
				}
				button {
					+"Join Cooperative (2v1) Battle"
					onClickFunction = { e ->
						e.preventDefault()
						
						callback(MainMenuOption.Multiplayer2v1(Player2v1.PLAYER_2))
					}
				}
			}
		}
	}
	
	class ChooseBattleSizeScreen(private val maxBattleSize: BattleSize) : Popup<BattleSize?>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (BattleSize?) -> Unit) {
			p {
				style = "text-align:center"
				
				+"Select a battle size"
			}
			
			div(classes = "button-set col") {
				for (battleSize in BattleSize.values()) {
					if (battleSize <= maxBattleSize)
						button {
							+battleSize.displayName
							+" ("
							+battleSize.numPoints.toString()
							+")"
							onClickFunction = { e ->
								e.preventDefault()
								callback(battleSize)
							}
						}
				}
				button {
					+"Cancel"
					onClickFunction = { e ->
						e.preventDefault()
						callback(null)
					}
				}
			}
		}
	}
	
	object ChooseBattleBackgroundScreen : Popup<BattleBackground?>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (BattleBackground?) -> Unit) {
			p {
				style = "text-align:center"
				
				+"Select a battle background"
			}
			
			div(classes = "button-set col") {
				for (bg in BattleBackground.values()) {
					button {
						+bg.displayName
						onClickFunction = { e ->
							e.preventDefault()
							callback(bg)
						}
					}
				}
				button {
					+"Cancel"
					onClickFunction = { e ->
						e.preventDefault()
						callback(null)
					}
				}
			}
		}
	}
	
	object ChooseEnemyFactionScreen : Popup<AIFactionChoice?>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (AIFactionChoice?) -> Unit) {
			p {
				style = "text-align:center"
				
				+"Select an enemy faction"
			}
			
			div(classes = "button-set col") {
				button {
					+"Random"
					onClickFunction = { e ->
						e.preventDefault()
						callback(AIFactionChoice.Random)
					}
				}
				for (faction in Faction.values()) {
					button {
						+faction.navyName
						+Entities.nbsp
						img(src = faction.flagUrl) {
							style = "width:1.2em;height:0.75em"
						}
						
						onClickFunction = { e ->
							e.preventDefault()
							callback(AIFactionChoice.Chosen(faction))
						}
					}
				}
				button {
					+"Cancel"
					onClickFunction = { e ->
						e.preventDefault()
						callback(null)
					}
				}
			}
		}
	}
	
	class ChooseEnemyFactionFlavorScreen(val forFaction: Faction) : Popup<AIFactionFlavorChoice?>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (AIFactionFlavorChoice?) -> Unit) {
			p {
				style = "text-align:center"
				
				+"Select a color scheme for the enemy faction"
			}
			
			div(classes = "button-set col") {
				button {
					+"Random"
					onClickFunction = { e ->
						e.preventDefault()
						callback(AIFactionFlavorChoice.Random)
					}
				}
				for (flavor in FactionFlavor.optionsForAiEnemy(forFaction)) {
					button {
						+flavor.displayName
						+Entities.nbsp
						img(src = flavor.flagUrl) {
							style = "width:1.2em;height:0.75em"
						}
						
						onClickFunction = { e ->
							e.preventDefault()
							callback(AIFactionFlavorChoice.Chosen(flavor))
						}
					}
				}
				button {
					+"Cancel"
					onClickFunction = { e ->
						e.preventDefault()
						callback(null)
					}
				}
			}
		}
	}
	
	class GuestRequestScreen(private val hostInfo: InGameAdmiral, private val guestInfo: InGameAdmiral) : Popup<Boolean?>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (Boolean?) -> Unit) {
			p {
				style = "text-align:center"
				
				+guestInfo.fullName
				+" ("
				+guestInfo.user.username
				+") wishes to join your battle."
			}
			table {
				style = "table-layout:fixed;width:100%"
				
				tr {
					th { +"HOST" }
					th { +"GUEST" }
				}
				tr {
					td {
						style = "text-align:center"
						
						img(alt = hostInfo.faction.shortName, src = hostInfo.faction.flagUrl) { style = "width:65%;" }
					}
					td {
						style = "text-align:center"
						
						img(alt = guestInfo.faction.shortName, src = guestInfo.faction.flagUrl) { style = "width:65%;" }
					}
				}
				tr {
					td {
						style = "text-align:center"
						
						+hostInfo.fullName
					}
					td {
						style = "text-align:center"
						
						+guestInfo.fullName
					}
				}
				tr {
					td {
						style = "text-align:center"
						
						+"(${hostInfo.user.username})"
					}
					td {
						style = "text-align:center"
						
						+"(${guestInfo.user.username})"
					}
				}
			}
			
			div(classes = "button-set row") {
				button {
					+"Accept"
					onClickFunction = { e ->
						e.preventDefault()
						callback(true)
					}
				}
				button {
					+"Reject"
					onClickFunction = { e ->
						e.preventDefault()
						callback(false)
					}
				}
				button {
					+"Cancel"
					onClickFunction = { e ->
						e.preventDefault()
						callback(null)
					}
				}
			}
		}
	}
	
	class Host1v1SelectScreen(private val hosts: Map<String, Joinable>) : Popup<String?>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (String?) -> Unit) {
			table {
				style = "table-layout:fixed;width:100%"
				
				tr {
					th { +"Host Player" }
					th { +"Host Admiral" }
					th { +"Host Faction" }
					th { +"Battle Size" }
					th { +"Battle Background" }
					th { +Entities.nbsp }
				}
				for ((id, joinable) in hosts) {
					tr {
						td {
							style = "text-align:center"
							
							+joinable.admiral.user.username
						}
						td {
							style = "text-align:center"
							
							+joinable.admiral.fullName
						}
						td {
							style = "text-align:center"
							
							img(alt = joinable.admiral.faction.shortName, src = joinable.admiral.faction.flagUrl) {
								style = "width:4em;height:2.5em"
							}
						}
						td {
							style = "text-align:center"
							
							+joinable.battleInfo.size.displayName
							+" ("
							+joinable.battleInfo.size.numPoints.toString()
							+")"
						}
						td {
							style = "text-align:center"
							
							+joinable.battleInfo.bg.displayName
						}
						td {
							style = "text-align:center"
							
							a(href = "#") {
								+"Join"
								onClickFunction = { e ->
									e.preventDefault()
									callback(id)
								}
							}
						}
					}
				}
				tr {
					td { +Entities.nbsp }
					td { +Entities.nbsp }
					td { +Entities.nbsp }
					td { +Entities.nbsp }
					td { +Entities.nbsp }
					td {
						style = "text-align:center"
						
						a(href = "#") {
							+"Cancel"
							onClickFunction = { e ->
								e.preventDefault()
								callback(null)
							}
						}
					}
				}
			}
		}
	}
	
	class Host2v1SelectScreen(private val hosts: Map<String, Joinable>) : Popup<String?>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (String?) -> Unit) {
			table {
				style = "table-layout:fixed;width:100%"
				
				tr {
					th { +"Host Player" }
					th { +"Host Admiral" }
					th { +"Host Faction" }
					th { +"Enemy Faction" }
					th { +"Battle Size" }
					th { +"Battle Background" }
					th { +Entities.nbsp }
				}
				for ((id, joinable) in hosts) {
					tr {
						td {
							style = "text-align:center"
							
							+joinable.admiral.user.username
						}
						td {
							style = "text-align:center"
							
							+joinable.admiral.fullName
						}
						td {
							style = "text-align:center"
							
							img(alt = joinable.admiral.faction.shortName, src = joinable.admiral.faction.flagUrl) {
								style = "width:4em;height:2.5em"
							}
						}
						td {
							style = "text-align:center"
							
							joinable.enemyFaction?.let { enemy ->
								img(alt = enemy.shortName, src = enemy.flagUrl) {
									style = "width:4em;height:2.5em"
								}
							} ?: (+"Random")
						}
						td {
							style = "text-align:center"
							
							+joinable.battleInfo.size.displayName
							+" ("
							+joinable.battleInfo.size.numPoints.toString()
							+")"
						}
						td {
							style = "text-align:center"
							
							+joinable.battleInfo.bg.displayName
						}
						td {
							style = "text-align:center"
							
							a(href = "#") {
								+"Join"
								onClickFunction = { e ->
									e.preventDefault()
									callback(id)
								}
							}
						}
					}
				}
				tr {
					td { +Entities.nbsp }
					td { +Entities.nbsp }
					td { +Entities.nbsp }
					td { +Entities.nbsp }
					td { +Entities.nbsp }
					td { +Entities.nbsp }
					td {
						style = "text-align:center"
						
						a(href = "#") {
							+"Cancel"
							onClickFunction = { e ->
								e.preventDefault()
								callback(null)
							}
						}
					}
				}
			}
		}
	}
	
	class JoinRejectedScreen(private val hostInfo: InGameAdmiral) : Popup<Unit>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (Unit) -> Unit) {
			p {
				style = "text-align:center"
				
				+hostInfo.fullName
				+" has rejected your request to join."
			}
			
			div(classes = "button-set row") {
				button {
					+"Continue"
					onClickFunction = { e ->
						e.preventDefault()
						callback(Unit)
					}
				}
			}
		}
	}
	
	// Battle popups
	class GameOver(private val winner: GlobalSide?, private val outcome: String, private val subplotStatuses: Map<SubplotKey, SubplotOutcome>, private val finalState: GameState) : Popup<Nothing>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (Nothing) -> Unit) {
			p {
				style = "text-align:center"
				
				strong(classes = "heading") {
					+"${victoryTitle(mySide, winner, subplotStatuses)}!"
				}
			}
			p {
				style = "text-align:center"
				
				+outcome
			}
			p {
				style = "text-align:center"
				
				val admiralId = finalState.admiralInfo(mySide).id
				
				a(href = "/admiral/${admiralId}") {
					+"Exit Battle"
				}
			}
		}
	}
	
	// Utility popups
	class LoadingScreen<T>(private val loadingText: String, private val loadAction: suspend () -> T) : Popup<T>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (T) -> Unit) {
			p {
				style = "text-align:center"
				
				+loadingText
			}
			
			AppScope.launch(context) {
				callback(loadAction())
			}
		}
	}
	
	class CancellableLoadingScreen<T>(private val loadingText: String, private val loadAction: suspend () -> T) : Popup<T?>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (T?) -> Unit) {
			p {
				style = "text-align:center"
				
				+loadingText
			}
			
			val loading = AppScope.launch(context) {
				callback(loadAction())
			}
			
			div(classes = "button-set row") {
				button {
					+"Cancel"
					onClickFunction = { e ->
						e.preventDefault()
						
						loading.cancel()
						callback(null)
					}
				}
			}
		}
	}
	
	class Error(private val errorMessage: String) : Popup<Nothing>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (Nothing) -> Unit) {
			p(classes = "error") {
				style = "text-align:center"
				
				+errorMessage
			}
		}
	}
}
