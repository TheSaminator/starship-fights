package starshipfights.game

import kotlinx.browser.document
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.hasClass
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
	
	suspend fun display(): T {
		pollFlow(100) { popup.hasClass("hide") }.takeWhile { !it }.collect()
		
		popupBox.clear()
		
		return suspendCancellableCoroutine { continuation ->
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
		}
	}
	
	class MainMenuScreen(private val admiralInfo: InGameAdmiral) : Popup<GlobalSide?>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (GlobalSide?) -> Unit) {
			p {
				style = "text-align:center"
				img(alt = "Starship Fights", src = RenderResources.LOGO_URL) {
					style = "width:70%"
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
					+"Host Battle"
					onClickFunction = { e ->
						e.preventDefault()
						
						callback(GlobalSide.HOST)
					}
				}
				button {
					+"Join Battle"
					onClickFunction = { e ->
						e.preventDefault()
						
						callback(GlobalSide.GUEST)
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
	
	class HostSelectScreen(private val hosts: Map<String, Joinable>) : Popup<String?>() {
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
	
	class GameOver(private val winner: LocalSide?, private val outcome: String, private val finalState: GameState) : Popup<Nothing>() {
		override fun TagConsumer<*>.render(context: CoroutineContext, callback: (Nothing) -> Unit) {
			p {
				style = "text-align:center"
				
				strong(classes = "heading") {
					+when (winner) {
						LocalSide.GREEN -> "Victory"
						LocalSide.RED -> "Defeat"
						null -> "Stalemate"
					}
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
}
