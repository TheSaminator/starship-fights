package net.starshipfights.game

import externals.textfit.textFit
import externals.threejs.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import net.starshipfights.data.Id
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent

interface GameUIResponder {
	fun doAction(action: PlayerAction)
	fun useAbility(ability: PlayerAbilityType)
}

object GameUI {
	private lateinit var responder: GameUIResponder
	
	private val gameUI = document.getElementById("ui").unsafeCast<HTMLDivElement>()
	private lateinit var chatHistory: HTMLDivElement
	private lateinit var chatInput: HTMLInputElement
	private lateinit var chatSend: HTMLButtonElement
	
	private lateinit var topMiddleInfo: HTMLDivElement
	private lateinit var topRightBar: HTMLDivElement
	
	private lateinit var objectives: HTMLDivElement
	
	private lateinit var errorMessages: HTMLParagraphElement
	private lateinit var helpMessages: HTMLParagraphElement
	
	private lateinit var shipsOverlay: HTMLElement
	private lateinit var shipsOverlayRenderer: CSS3DRenderer
	private lateinit var shipsOverlayCamera: PerspectiveCamera
	private lateinit var shipsOverlayScene: Scene
	
	fun initGameUI(uiResponder: GameUIResponder) {
		responder = uiResponder
		
		gameUI.clear()
		
		gameUI.append {
			div(classes = "panel") {
				id = "chat-box"
				
				div(classes = "inset") {
					id = "chat-history"
				}
				
				div {
					id = "chat-entry"
					
					textInput {
						id = "chat-input"
						placeholder = "Write a friendly chat message"
					}
					
					button {
						id = "chat-send"
						+"Send"
					}
				}
			}
			
			div(classes = "panel") {
				id = "top-middle-info"
				
				p {
					+"Battle has not started yet"
				}
			}
			
			div(classes = "panel") {
				id = "top-right-bar"
			}
			
			div {
				id = "objectives"
			}
			
			p {
				id = "error-messages"
			}
			
			p {
				id = "help-messages"
			}
		}
		
		chatHistory = document.getElementById("chat-history").unsafeCast<HTMLDivElement>()
		chatInput = document.getElementById("chat-input").unsafeCast<HTMLInputElement>()
		chatSend = document.getElementById("chat-send").unsafeCast<HTMLButtonElement>()
		
		chatSend.addEventListener("click", { e ->
			e.preventDefault()
			
			val chatAction = PlayerAction.SendChatMessage(chatInput.value)
			responder.doAction(chatAction)
			chatInput.value = ""
		})
		
		chatInput.addEventListener("keydown", { e ->
			val ke = e.unsafeCast<KeyboardEvent>()
			if (ke.key == "Enter") {
				val chatAction = PlayerAction.SendChatMessage(chatInput.value)
				responder.doAction(chatAction)
				chatInput.value = ""
			}
			if (document.activeElement == chatInput && document.hasFocus())
				ke.stopPropagation()
		})
		
		topMiddleInfo = document.getElementById("top-middle-info").unsafeCast<HTMLDivElement>()
		topRightBar = document.getElementById("top-right-bar").unsafeCast<HTMLDivElement>()
		
		objectives = document.getElementById("objectives").unsafeCast<HTMLDivElement>()
		
		errorMessages = document.getElementById("error-messages").unsafeCast<HTMLParagraphElement>()
		helpMessages = document.getElementById("help-messages").unsafeCast<HTMLParagraphElement>()
		
		shipsOverlayRenderer = CSS3DRenderer()
		shipsOverlayRenderer.setSize(window.innerWidth, window.innerHeight)
		
		shipsOverlay = shipsOverlayRenderer.domElement
		gameUI.prepend(shipsOverlay)
		
		shipsOverlayCamera = PerspectiveCamera(69, window.aspectRatio, 0.01, 1_000)
		shipsOverlayScene = Scene()
		shipsOverlayScene.add(shipsOverlayCamera)
		
		window.addEventListener("resize", {
			shipsOverlayCamera.aspect = window.aspectRatio
			shipsOverlayCamera.updateProjectionMatrix()
			
			shipsOverlayRenderer.setSize(window.innerWidth, window.innerHeight)
		})
	}
	
	suspend fun displayErrorMessage(message: String) {
		errorMessages.textContent = message
		
		delay(5000)
		
		errorMessages.textContent = ""
	}
	
	var currentHelpMessage: String
		get() = helpMessages.textContent ?: ""
		set(value) {
			helpMessages.textContent = value
		}
	
	fun renderGameUI(controls: BattleCameraControls) {
		shipsOverlayCamera.position.copy(controls.camera.getWorldPosition(shipsOverlayCamera.position))
		shipsOverlayCamera.quaternion.copy(controls.camera.getWorldQuaternion(shipsOverlayCamera.quaternion))
		shipsOverlayRenderer.render(shipsOverlayScene, shipsOverlayCamera)
		
		fitLabels()
	}
	
	fun updateGameUI(state: GameState) {
		chatHistory.clear()
		chatHistory.append {
			for (entry in state.chatBox.sortedBy { it.sentAt }) {
				p {
					title = "At ${entry.sentAt.date}"
					
					when (entry) {
						is ChatEntry.AdminAnnouncement -> {
							+entry.message
						}
						is ChatEntry.PlayerMessage -> {
							val senderInfo = state.admiralInfo(entry.senderSide)
							val senderSide = entry.senderSide.relativeTo(mySide)
							strong {
								style = "color:${senderSide.htmlColor}"
								+senderInfo.user.username
								+Entities.nbsp
								img(alt = senderInfo.faction.shortName, src = senderInfo.faction.flagUrl) {
									style = "height:0.75em;width:1.2em"
								}
							}
							+Entities.nbsp
							+entry.message
						}
						is ChatEntry.ShipIdentified -> {
							val ship = state.getShipInfo(entry.ship)
							val owner = state.getShipOwner(entry.ship).relativeTo(mySide)
							+"The "
							if (owner == LocalSide.RED)
								+"enemy ship "
							else if (owner == LocalSide.BLUE)
								+"allied ship "
							strong {
								style = "color:${owner.htmlColor}"
								+ship.fullName
							}
							+" has been sighted"
							if (owner != LocalSide.RED)
								+" by the enemy"
							+"!"
						}
						is ChatEntry.ShipEscaped -> {
							val ship = state.getShipInfo(entry.ship)
							val owner = state.getShipOwner(entry.ship).relativeTo(mySide)
							+if (owner == LocalSide.RED)
								"The enemy ship "
							else
								"Our ship, the "
							strong {
								style = "color:${owner.htmlColor}"
								+ship.fullName
							}
							+" has "
							+if (owner == LocalSide.RED)
								"fled like a coward"
							else
								"disengaged"
							+" from the battlefield!"
						}
						is ChatEntry.ShipAttacked -> {
							val ship = state.getShipInfo(entry.ship)
							val owner = state.getShipOwner(entry.ship).relativeTo(mySide)
							+if (owner == LocalSide.RED)
								"The enemy ship "
							else
								"Our ship, the "
							strong {
								style = "color:${owner.htmlColor}"
								+ship.fullName
							}
							+" has taken "
							
							+if (entry.weapon is ShipWeapon.EmpAntenna)
								"subsystem-draining"
							else
								entry.damageInflicted.toString()
							
							if (entry.critical != null)
								+" critical"
							
							+" damage from "
							when (entry.attacker) {
								is ShipAttacker.EnemyShip -> {
									val attackerSide = state.getShipOwner(entry.attacker.id).relativeTo(mySide)
									if (entry.weapon != null) {
										+"the "
										+when (entry.weapon) {
											is ShipWeapon.Cannon -> "cannons"
											is ShipWeapon.Lance -> "lances"
											is ShipWeapon.Hangar -> "bombers"
											is ShipWeapon.Torpedo -> "torpedoes"
											is ShipWeapon.ParticleClawLauncher -> "particle claws"
											is ShipWeapon.LightningYarn -> "lightning yarn"
											ShipWeapon.MegaCannon -> "Mega Giga Cannon"
											ShipWeapon.RevelationGun -> "Revelation Gun"
											ShipWeapon.EmpAntenna -> "EMP antenna"
										}
										+" of "
									}
									+"the "
									strong {
										style = "color:${attackerSide.htmlColor}"
										+state.getShipInfo(entry.attacker.id).fullName
									}
								}
								ShipAttacker.Fire -> {
									+"onboard fires"
								}
								ShipAttacker.Bombers -> {
									if (owner == LocalSide.RED)
										+"our "
									else
										+"enemy "
									+"bombers"
								}
							}
							
							+when (entry.critical) {
								ShipCritical.Fire -> ", starting a fire"
								is ShipCritical.TroopsKilled -> ", killing ${entry.critical.number} troops"
								is ShipCritical.ModulesHit -> ", disabling ${entry.critical.module.joinToDisplayString { it.getDisplayName(ship) }}"
								else -> ""
							}
							+"."
						}
						is ChatEntry.ShipAttackFailed -> {
							val ship = state.getShipInfo(entry.ship)
							val owner = state.getShipOwner(entry.ship).relativeTo(mySide)
							+if (owner == LocalSide.RED)
								"The enemy ship "
							else
								"Our ship, the "
							strong {
								style = "color:${owner.htmlColor}"
								+ship.fullName
							}
							+" has ignored an attack from "
							when (entry.attacker) {
								is ShipAttacker.EnemyShip -> {
									val attackerSide = state.getShipOwner(entry.attacker.id).relativeTo(mySide)
									if (entry.weapon != null) {
										+"the "
										+when (entry.weapon) {
											is ShipWeapon.Cannon -> "cannons"
											is ShipWeapon.Lance -> "lances"
											is ShipWeapon.Hangar -> "bombers"
											is ShipWeapon.Torpedo -> "torpedoes"
											is ShipWeapon.ParticleClawLauncher -> "particle claws"
											is ShipWeapon.LightningYarn -> "lightning yarn"
											ShipWeapon.MegaCannon -> "Mega Giga Cannon"
											ShipWeapon.RevelationGun -> "Revelation Gun"
											ShipWeapon.EmpAntenna -> "EMP antenna"
										}
										+" of "
									}
									+"the "
									strong {
										style = "color:${attackerSide.htmlColor}"
										+state.getShipInfo(entry.attacker.id).fullName
									}
								}
								ShipAttacker.Fire -> {
									+"onboard fires"
								}
								ShipAttacker.Bombers -> {
									if (owner == LocalSide.RED)
										+"our "
									else
										+"enemy "
									+"bombers"
								}
							}
							
							+when (entry.damageIgnoreType) {
								DamageIgnoreType.FELINAE_ARMOR -> " using its relativistic armor"
							}
							+"."
						}
						is ChatEntry.ShipBoarded -> {
							val ship = state.getShipInfo(entry.ship)
							val owner = state.getShipOwner(entry.ship).relativeTo(mySide)
							val attackerOwner = state.getShipOwner(entry.boarder).relativeTo(mySide)
							+if (owner == LocalSide.RED)
								"The enemy ship "
							else
								"Our ship, the "
							strong {
								style = "color:${owner.htmlColor}"
								+ship.fullName
							}
							
							+" has been boarded by the "
							strong {
								style = "color:${attackerOwner.htmlColor}"
								+state.getShipInfo(entry.boarder).fullName
							}
							
							+when (entry.critical) {
								ShipCritical.ExtraDamage -> ", dealing ${entry.damageAmount} hull damage"
								ShipCritical.Fire -> ", starting a fire"
								is ShipCritical.TroopsKilled -> ", killing ${entry.critical.number} troops"
								is ShipCritical.ModulesHit -> ", disabling ${entry.critical.module.joinToDisplayString { it.getDisplayName(ship) }}"
								else -> ", to no effect"
							}
							+"."
						}
						is ChatEntry.ShipDestroyed -> {
							val ship = state.getShipInfo(entry.ship)
							val owner = state.getShipOwner(entry.ship).relativeTo(mySide)
							+if (owner == LocalSide.RED)
								"The enemy ship "
							else
								"Our ship, the "
							strong {
								style = "color:${owner.htmlColor}"
								+ship.fullName
							}
							+" has been destroyed by "
							when (entry.destroyedBy) {
								is ShipAttacker.EnemyShip -> {
									val attackerOwner = state.getShipOwner(entry.destroyedBy.id).relativeTo(mySide)
									+"the "
									strong {
										style = "color:${attackerOwner.htmlColor}"
										+state.getShipInfo(entry.destroyedBy.id).fullName
									}
								}
								ShipAttacker.Fire -> {
									+"onboard fires"
								}
								ShipAttacker.Bombers -> {
									+if (owner == LocalSide.RED)
										"our "
									else
										"enemy "
									+"bombers"
								}
							}
							+"!"
						}
					}
				}
			}
		}.lastOrNull()?.scrollIntoView()
		
		objectives.clear()
		objectives.append {
			for (objective in state.objectives(mySide)) {
				val classes = when (objective.succeeded) {
					true -> "item succeeded"
					false -> "item failed"
					else -> "item"
				}
				div(classes = classes) {
					+objective.displayText
				}
			}
		}
		
		val abilities = state.getPossibleAbilities(mySide)
		
		topMiddleInfo.clear()
		topMiddleInfo.append {
			p {
				when (state.phase) {
					GamePhase.Deploy -> {
						strong(classes = "heading") {
							+"Pre-Battle Deployment"
						}
					}
					is GamePhase.Power -> {
						strong(classes = "heading") {
							+"Turn ${state.phase.turn}"
						}
						br
						+"Phase I - Power Distribution"
					}
					is GamePhase.Move -> {
						strong(classes = "heading") {
							+"Turn ${state.phase.turn}"
						}
						br
						+"Phase II - Ship Movement"
					}
					is GamePhase.Attack -> {
						strong(classes = "heading") {
							+"Turn ${state.phase.turn}"
						}
						br
						+"Phase III - Weapons Fire"
					}
					is GamePhase.Repair -> {
						strong(classes = "heading") {
							+"Turn ${state.phase.turn}"
						}
						br
						+"Phase IV - Onboard Repairs"
					}
				}
				
				if (state.phase.usesInitiative) {
					br
					+if (mySide in state.doneWithPhase)
						"You have ended your phase"
					else if (state.currentInitiative == null || state.currentInitiative == mySide)
						"You have the initiative!"
					else if (state.currentInitiative?.side == mySide.side)
						"Your ally has the initiative!"
					else
						"Your opponent has the initiative"
				} else if (mySide in state.doneWithPhase) {
					br
					+"You have ended your phase"
				}
			}
		}
		
		topRightBar.clear()
		topRightBar.append {
			when (state.phase) {
				GamePhase.Deploy -> {
					drawDeployPhase(state, abilities)
				}
				else -> {
					drawShipActions(state, selectedShip.value)
				}
			}
		}
		
		shipsOverlayScene.clear()
		for ((shipId, ship) in state.ships) {
			if (state.renderShipAs(ship, mySide) == ShipRenderMode.FULL)
				shipsOverlayScene.add(CSS3DSprite(document.create.div {
					drawShipLabel(state, abilities, shipId, ship)
				}).apply {
					scale.setScalar(0.00625)
					
					element.style.asDynamic().pointerEvents = "none"
					
					position.copy(RenderScaling.toWorldPosition(ship.position.location))
					position.y = 7.5
				})
		}
		
		hasFitLabels = false
	}
	
	var hasFitLabels = false
	private fun fitLabels() {
		if (hasFitLabels) return
		textFit(document.getElementsByClassName("ship-label"))
		hasFitLabels = true
	}
	
	private fun DIV.drawShipLabel(state: GameState, abilities: List<PlayerAbilityType>, shipId: Id<ShipInstance>, ship: ShipInstance) {
		id = "ship-overlay-$shipId"
		classes = setOf("ship-overlay")
		style = "background-color:#999;width:800px;height:300px;opacity:0.8;font-size:4em;text-align:center;vertical-align:middle"
		attributes["data-ship-id"] = shipId.toString()
		
		p(classes = "ship-label") {
			style = "color:#fff;margin:0;white-space:nowrap;width:800px;height:100px"
			+ship.ship.fullName
		}
		p(classes = "ship-label") {
			style = "color:#fff;margin:0;white-space:nowrap;width:800px;height:75px"
			+ship.ship.shipType.fullDisplayName
		}
		
		p {
			style = "margin:0;white-space:nowrap;width:800px;height:125px"
			when (state.phase) {
				GamePhase.Deploy -> {
					button {
						style = "pointer-events:auto"
						+"Undeploy"
						
						val undeployAbility = PlayerAbilityType.UndeployShip(shipId)
						if (undeployAbility in abilities)
							onClickFunction = { e ->
								e.preventDefault()
								
								responder.useAbility(undeployAbility)
							}
					}
				}
				else -> {
					if (ship.canUseShields) {
						val totalShield = ship.powerMode.shields
						val activeShield = ship.shieldAmount
						val downShield = totalShield - activeShield
						
						table {
							style = "width:100%;table-layout:fixed;background-color:#555;margin:0;margin-bottom:10px"
							
							tr {
								repeat(activeShield) {
									td {
										style = "background-color:#69F;height:15px;box-shadow:inset 0 0 0 3px #555"
									}
								}
								repeat(downShield) {
									td {
										style = "background-color:#46A;height:15px;box-shadow:inset 0 0 0 3px #555"
									}
								}
							}
						}
					}
					
					val totalHull = ship.durability.maxHullPoints
					val activeHull = ship.hullAmount
					val downHull = totalHull - activeHull
					
					table {
						style = "width:100%;table-layout:fixed;background-color:#555;margin:0;margin-bottom:10px"
						
						tr {
							val (activeHullColor, downHullColor) = when (ship.owner.relativeTo(mySide)) {
								LocalSide.GREEN -> "#5F5" to "#262"
								LocalSide.BLUE -> "#55F" to "#226"
								LocalSide.RED -> "#F55" to "#622"
							}
							
							repeat(activeHull) {
								td {
									style = "background-color:$activeHullColor;height:15px;box-shadow:inset 0 0 0 3px #555"
								}
							}
							repeat(downHull) {
								td {
									style = "background-color:$downHullColor;height:15px;box-shadow:inset 0 0 0 3px #555"
								}
							}
						}
					}
					
					val totalTroops = ship.durability.troopsDefense
					val activeTroops = ship.troopsAmount
					val downTroops = totalTroops - activeTroops
					
					table {
						style = "width:100%;table-layout:fixed;background-color:#555;margin:0;margin-bottom:10px"
						
						tr {
							repeat(activeTroops) {
								td {
									style = "background-color:#AAA;height:15px;box-shadow:inset 0 0 0 3px #555"
								}
							}
							repeat(downTroops) {
								td {
									style = "background-color:#444;height:15px;box-shadow:inset 0 0 0 3px #555"
								}
							}
						}
					}
					
					if (ship.ship.reactor is StandardShipReactor) {
						if (ship.owner.side == mySide.side) {
							val totalWeapons = ship.powerMode.weapons
							val activeWeapons = ship.weaponAmount
							val downWeapons = totalWeapons - activeWeapons
							
							table {
								style = "width:100%;table-layout:fixed;background-color:#555;margin:0"
								
								tr {
									repeat(activeWeapons) {
										td {
											style = "background-color:#F63;height:15px;box-shadow:inset 0 0 0 3px #555"
										}
									}
									repeat(downWeapons) {
										td {
											style = "background-color:#A42;height:15px;box-shadow:inset 0 0 0 3px #555"
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		if (state.phase is GamePhase.Attack) {
			div {
				style = "margin:0;white-space:nowrap;text-align:center"
				br
				
				val shipSide = ship.owner.relativeTo(mySide)
				val (fighterSide, bomberSide) = when (shipSide) {
					LocalSide.GREEN -> LocalSide.GREEN to LocalSide.RED
					LocalSide.BLUE -> LocalSide.GREEN to LocalSide.RED
					LocalSide.RED -> LocalSide.RED to LocalSide.GREEN
				}
				
				if (ship.fighterWings.isNotEmpty()) {
					span {
						val (borderColor, fillColor) = when (fighterSide) {
							LocalSide.GREEN -> "#5F5" to "#262"
							LocalSide.BLUE -> "#5F5" to "#262"
							LocalSide.RED -> "#F55" to "#622"
						}
						
						style = "display:inline-block;border:5px solid $borderColor;border-radius:15px;background-color:$fillColor;color:#fff"
						
						img(src = StrikeCraftWing.FIGHTERS.iconUrl, alt = StrikeCraftWing.FIGHTERS.displayName) {
							style = "width:1.125em"
						}
						
						+Entities.nbsp
						
						+ship.fighterWings.sumOf { (carrierId, wingId) ->
							(state.ships[carrierId]?.armaments?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
						}.toPercent()
					}
				}
				
				+Entities.nbsp
				
				if (ship.bomberWings.isNotEmpty()) {
					span {
						val (borderColor, fillColor) = when (bomberSide) {
							LocalSide.GREEN -> "#5F5" to "#262"
							LocalSide.BLUE -> "#5F5" to "#262"
							LocalSide.RED -> "#F55" to "#622"
						}
						
						style = "display:inline-block;border:5px solid $borderColor;border-radius:15px;background-color:$fillColor;color:#fff"
						
						img(src = StrikeCraftWing.BOMBERS.iconUrl, alt = StrikeCraftWing.BOMBERS.displayName) {
							style = "width:1.125em"
						}
						
						+Entities.nbsp
						
						+ship.bomberWings.sumOf { (carrierId, wingId) ->
							(state.ships[carrierId]?.armaments?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
						}.toPercent()
					}
				}
			}
		}
	}
	
	private fun TagConsumer<*>.drawDeployPhase(state: GameState, abilities: List<PlayerAbilityType>) {
		val deployableShips = state.start.playerStart(mySide).deployableFleet
		val remainingPoints = state.getUsablePoints(mySide) - state.ships.values.filter { it.owner == mySide }.sumOf { it.ship.pointCost }
		
		div {
			style = "height:19%;font-size:0.9em"
			
			p {
				style = "text-align:center;margin:1.25em"
				+"Deploy your fleet"
				br
				+"Points remaining: $remainingPoints"
			}
		}
		
		div {
			style = "height:69%;overflow-y:auto;font-size:0.9em"
			
			hr { style = "border-color:#555" }
			for ((id, ship) in deployableShips.toList().sortedBy { (_, ship) -> ship.pointCost }) {
				p {
					style = "text-align:center;margin:0"
					+ship.name
					br
					+ship.shipType.fullDisplayName
					br
					+"${ship.pointCost} points | "
					
					val deployAbility = PlayerAbilityType.DeployShip(id)
					if (deployAbility in abilities)
						a(href = "#") {
							+"Deploy"
							
							onClickFunction = { e ->
								e.preventDefault()
								responder.useAbility(deployAbility)
							}
						}
					else
						span {
							style = "color:#333;cursor:not-allowed"
							+"Deploy"
						}
				}
				
				hr { style = "border-color:#555" }
			}
		}
		
		div {
			style = "height:9%;font-size:0.9em"
			
			p {
				style = "text-align:center;margin:0.75em"
				
				val donePhase = abilities.filterIsInstance<PlayerAbilityType.DonePhase>().singleOrNull()
				if (donePhase != null)
					a(href = "#") {
						id = "done-phase"
						+"Confirm Deployment"
						
						onClickFunction = { e ->
							e.preventDefault()
							
							finalizePhase(donePhase, abilities)
						}
					}
				else
					span {
						style = "color:#333;cursor:not-allowed"
						+"Confirm Deployment"
					}
			}
		}
	}
	
	private fun TagConsumer<*>.drawShipActions(gameState: GameState, selectedId: Id<ShipInstance>?) {
		val ship = selectedId?.let { gameState.ships[it] }
		
		div {
			style = "text-align:center"
			
			val abilities = gameState
				.getPossibleAbilities(mySide)
			
			if (ship == null) {
				p {
					style = "text-align:center;margin:0"
					
					+"No ship selected. Click on a ship to select it."
				}
			} else {
				val shipAbilities = abilities
					.filterIsInstance<ShipAbility>()
					.filter { it.ship == ship.id }
				
				val combatAbilities = abilities
					.filterIsInstance<CombatAbility>()
					.filter { it.ship == ship.id }
				
				p {
					style = "height:19%;margin:0"
					
					strong(classes = "heading") { +ship.ship.fullName }
					br
					
					+ship.ship.shipType.fullerDisplayName
					br
					
					if (ship.owner == mySide) {
						when (ship.ship.reactor) {
							is StandardShipReactor -> table {
								style = "width:100%;table-layout:fixed;background-color:#555"
								tr {
									for (subsystem in ShipSubsystem.values()) {
										val amount = ship.powerMode[subsystem]
										
										repeat(amount) {
											td {
												style = "background-color:${subsystem.htmlColor};margin:1px;height:0.55em"
											}
										}
									}
								}
							}
							is FelinaeShipReactor -> p {
								+"Reactor Priority: ${ship.felinaeShipPowerMode.displayName}"
							}
						}
					}
					
					for ((module, status) in ship.modulesStatus.statuses) {
						when (status) {
							ShipModuleStatus.INTACT -> {}
							ShipModuleStatus.DAMAGED -> {
								span {
									style = "color:#ec4"
									+"${module.getDisplayName(ship.ship)} Damaged"
								}
								br
							}
							ShipModuleStatus.DESTROYED -> {
								span {
									style = "color:#d22"
									+"${module.getDisplayName(ship.ship)} Destroyed"
								}
								br
							}
							ShipModuleStatus.ABSENT -> {}
						}
					}
					
					if (ship.numFires > 0)
						span {
							style = "color:#e94"
							+"${ship.numFires} Onboard Fire${if (ship.numFires == 1) "" else "s"}"
						}
				}
				
				if (ship.owner == mySide) {
					hr { style = "border-color:#555" }
					
					p {
						style = "height:69%;margin:0"
						
						if (gameState.phase is GamePhase.Repair && ship.durability is StandardShipDurability) {
							+"${ship.remainingRepairTokens} Repair Tokens"
							br
						}
						
						for (ability in shipAbilities) {
							when (ability) {
								is PlayerAbilityType.DistributePower -> {
									val shipReactor = ship.ship.reactor as StandardShipReactor
									val shipPowerMode = ClientAbilityData.newShipPowerModes[ship.id] ?: ship.powerMode
									
									table {
										style = "width:100%;table-layout:fixed;background-color:#555"
										tr {
											for (subsystem in ShipSubsystem.values()) {
												val amount = shipPowerMode[subsystem]
												
												repeat(amount) {
													td {
														style = "background-color:${subsystem.htmlColor};margin:1px;height:0.55em"
													}
												}
											}
										}
									}
									
									p {
										style = "text-align:center"
										+"Power Output: ${shipReactor.powerOutput}"
										br
										+"Remaining Transfers: ${ship.remainingGridEfficiency(shipPowerMode)}"
									}
									
									for (transferFrom in ShipSubsystem.values()) {
										div(classes = "button-set row") {
											for (transferTo in ShipSubsystem.values()) {
												if (transferFrom == transferTo) continue
												
												button {
													style = "font-size:0.8em;padding:0 0.25em"
													title = "${transferFrom.displayName} to ${transferTo.displayName}"
													
													img(src = transferFrom.imageUrl, alt = transferFrom.displayName) {
														style = "width:0.95em"
													}
													+Entities.nbsp
													img(src = ShipSubsystem.transferImageUrl, alt = " to ") {
														style = "width:0.95em"
													}
													+Entities.nbsp
													img(src = transferTo.imageUrl, alt = transferTo.displayName) {
														style = "width:0.95em"
													}
													
													val delta = mapOf(transferFrom to -1, transferTo to 1)
													val newPowerMode = shipPowerMode + delta
													
													if (ship.validatePowerMode(newPowerMode))
														onClickFunction = { e ->
															e.preventDefault()
															ClientAbilityData.newShipPowerModes[ship.id] = newPowerMode
															updateAbilityData(gameState)
														}
													else {
														disabled = true
														style += ";cursor:not-allowed"
													}
												}
											}
										}
									}
									
									button {
										+"Confirm"
										if (ship.validatePowerMode(shipPowerMode))
											onClickFunction = { e ->
												e.preventDefault()
												responder.useAbility(ability)
											}
										else {
											disabled = true
											style = "cursor:not-allowed"
										}
									}
									
									button {
										+"Reset"
										onClickFunction = { e ->
											e.preventDefault()
											ClientAbilityData.newShipPowerModes[ship.id] = ship.powerMode
											updateAbilityData(gameState)
										}
									}
								}
								is PlayerAbilityType.ConfigurePower -> {
									a(href = "#") {
										+"Set Priority: ${ability.powerMode.displayName}"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
									br
								}
								is PlayerAbilityType.MoveShip -> {
									a(href = "#") {
										+"Move Ship"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
									br
								}
								is PlayerAbilityType.UseInertialessDrive -> {
									a(href = "#") {
										+"Activate Inertialess Drive (${ship.remainingInertialessDriveJumps})"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
									br
								}
								is PlayerAbilityType.DisruptionPulse -> {
									a(href = "#") {
										+"Activate Strike-Craft Disruption Pulse (${ship.remainingDisruptionPulseEmissions})"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
									br
								}
								is PlayerAbilityType.BoardingParty -> {
									a(href = "#") {
										+"Board Enemy Vessel"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
									br
								}
								is PlayerAbilityType.RepairShipModule -> {
									a(href = "#") {
										+"Repair ${ability.module.getDisplayName(ship.ship)}"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
									br
								}
								is PlayerAbilityType.ExtinguishFire -> {
									a(href = "#") {
										+"Extinguish Fire"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
									br
								}
								is PlayerAbilityType.Recoalesce -> {
									a(href = "#") {
										+"Activate Recoalescence"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
									br
								}
							}
						}
						
						for (ability in combatAbilities) {
							br
							
							val weaponInstance = ship.armaments.getValue(ability.weapon)
							
							val weaponVerb = if (weaponInstance is ShipWeaponInstance.Hangar) "Release" else "Fire"
							val weaponDesc = weaponInstance.displayName
							
							when (ability) {
								is PlayerAbilityType.ChargeLance -> {
									a(href = "#") {
										+"Charge $weaponDesc"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
								}
								is PlayerAbilityType.UseWeapon -> {
									a(href = "#") {
										+"$weaponVerb $weaponDesc"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
								}
								is PlayerAbilityType.RecallStrikeCraft -> {
									a(href = "#") {
										+"Recall $weaponDesc"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
								}
							}
						}
					}
				}
			}
			
			p {
				style = "height:9%;margin:0"
				
				hr { style = "border-color:#555" }
				
				val finishPhase = abilities.filterIsInstance<PlayerAbilityType.DonePhase>().singleOrNull()
				if (finishPhase != null)
					a(href = "#") {
						+"End Phase"
						id = "done-phase"
						
						onClickFunction = { e ->
							e.preventDefault()
							finalizePhase(finishPhase, abilities)
						}
					}
				else
					span {
						style = "color:#333;cursor:not-allowed"
						+"End Phase"
					}
			}
		}
	}
	
	fun changeShipSelection(gameState: GameState, selectedId: Id<ShipInstance>?) {
		topRightBar.clear()
		topRightBar.append {
			drawShipActions(gameState, selectedId)
		}
	}
	
	private fun updateAbilityData(gameState: GameState) {
		window.requestAnimationFrame {
			changeShipSelection(gameState, selectedShip.value)
		}
	}
	
	private fun finalizePhase(finishPhase: PlayerAbilityType.DonePhase, otherAbilities: List<PlayerAbilityType>) {
		val donePhase = document.getElementById("done-phase").unsafeCast<HTMLAnchorElement>()
		donePhase.replaceWith(document.create.span {
			style = "color:#333;cursor:not-allowed"
			+"Waiting..."
		})
		
		var shouldWait = false
		
		when (finishPhase.phase) {
			is GamePhase.Power -> {
				val powerAbilities = otherAbilities.filterIsInstance<PlayerAbilityType.DistributePower>().associateBy { it.ship }
				
				for (shipId in ClientAbilityData.newShipPowerModes.keys) {
					val powerAbility = powerAbilities[shipId] ?: continue
					responder.useAbility(powerAbility)
					
					shouldWait = true
				}
				
				ClientAbilityData.newShipPowerModes.clear()
			}
			else -> {
				// nothing needs to be done
			}
		}
		
		if (shouldWait)
			window.setTimeout({
				responder.useAbility(finishPhase)
			}, 500)
		else
			responder.useAbility(finishPhase)
	}
}
