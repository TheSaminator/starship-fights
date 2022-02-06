package starshipfights.game

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
import org.w3c.dom.*
import org.w3c.dom.events.KeyboardEvent
import starshipfights.data.Id

interface GameUIResponder {
	fun doAction(action: PlayerAction)
	fun useAbility(ability: PlayerAbilityType)
	fun clientError(errorMessage: String)
}

object GameUI {
	private lateinit var responder: GameUIResponder
	
	private val gameUI = document.getElementById("ui").unsafeCast<HTMLDivElement>()
	private lateinit var chatHistory: HTMLDivElement
	private lateinit var chatInput: HTMLInputElement
	private lateinit var chatSend: HTMLButtonElement
	
	private lateinit var topMiddleInfo: HTMLDivElement
	private lateinit var topRightBar: HTMLDivElement
	
	private lateinit var errorMessages: HTMLParagraphElement
	
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
					style = "text-align:center;margin:0"
					+"Battle has not started yet"
				}
			}
			
			div(classes = "panel") {
				id = "top-right-bar"
			}
			
			p {
				id = "error-messages"
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
		
		errorMessages = document.getElementById("error-messages").unsafeCast<HTMLParagraphElement>()
		
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
		
		delay(3750)
		
		errorMessages.textContent = ""
	}
	
	fun updateGameUI(controls: CameraControls) {
		shipsOverlayCamera.position.copy(controls.camera.getWorldPosition(shipsOverlayCamera.position))
		shipsOverlayCamera.quaternion.copy(controls.camera.getWorldQuaternion(shipsOverlayCamera.quaternion))
		shipsOverlayRenderer.render(shipsOverlayScene, shipsOverlayCamera)
		
		textFit(document.getElementsByClassName("ship-label"))
	}
	
	fun drawGameUI(state: GameState) {
		chatHistory.clear()
		chatHistory.append {
			state.chatBox.sortedBy { it.sentAt.toMillis() }.forEach { entry ->
				p {
					title = "At ${entry.sentAt.date}"
					
					when (entry) {
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
							strong {
								style = "color:${owner.htmlColor}"
								+ship.fullName
							}
							+" has been sighted"
							if (owner == LocalSide.BLUE)
								+" by the enemy"
							+"!"
						}
						is ChatEntry.ShipEscaped -> {
							val ship = state.getShipInfo(entry.ship)
							val owner = state.getShipOwner(entry.ship).relativeTo(mySide)
							if (owner == LocalSide.RED)
								+"The enemy ship "
							else
								+"Our ship, the "
							strong {
								style = "color:${owner.htmlColor}"
								+ship.fullName
							}
							+" has "
							if (owner == LocalSide.RED)
								+"fled like a coward from"
							else
								+"disengaged from"
							+" the battlefield!"
						}
						is ChatEntry.ShipDestroyed -> {
							val ship = state.getShipInfo(entry.ship)
							val owner = state.getShipOwner(entry.ship).relativeTo(mySide)
							if (owner == LocalSide.RED)
								+"The enemy ship "
							else
								+"Our ship, the "
							strong {
								style = "color:${owner.htmlColor}"
								+ship.fullName
							}
							+" has been destroyed by "
							when (entry.destroyedBy) {
								is ShipDestructionType.EnemyShip -> {
									+"the "
									span {
										style = "color:${state.getShipOwner(entry.destroyedBy.id).relativeTo(mySide).htmlColor}"
										+state.getShipInfo(entry.destroyedBy.id).fullName
									}
								}
								ShipDestructionType.Bombers -> {
									if (owner == LocalSide.RED)
										+"our "
									else
										+"enemy "
									+"bombers"
								}
							}
							+"!"
						}
					}
				}
			}
		}
		
		val abilities = state.getPossibleAbilities(mySide)
		
		topMiddleInfo.clear()
		topMiddleInfo.append {
			p {
				style = "text-align:center;margin:0"
				
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
		state.ships.forEach { (shipId, ship) ->
			if (state.renderShipAs(ship, mySide) == ShipRenderMode.FULL)
				shipsOverlayScene.add(CSS3DSprite(document.create.div {
					drawShipLabel(state, abilities, shipId, ship)
				}).apply {
					scale.setScalar(0.00625)
					
					element.style.asDynamic().pointerEvents = "none"
					
					position.copy(RenderScaling.toWorldPosition(ship.position.currentLocation))
					position.y = 7.5
				})
		}
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
					val totalShield = ship.powerMode.shields
					val activeShield = ship.shieldAmount
					val downShield = totalShield - activeShield
					
					table {
						style = "width:100%;table-layout:fixed;background-color:#555;margin:0;margin-bottom:25px"
						
						tr {
							repeat(activeShield) {
								td {
									style = "background-color:#69F;margin:10px;height:15px"
								}
							}
							repeat(downShield) {
								td {
									style = "background-color:#46A;margin:10px;height:15px"
								}
							}
						}
					}
					
					val totalHull = ship.ship.durability.maxHullPoints
					val activeHull = ship.hullAmount
					val downHull = totalHull - activeHull
					
					table {
						style = "width:100%;table-layout:fixed;background-color:#555;margin:0;margin-bottom:25px"
						
						tr {
							repeat(activeHull) {
								td {
									style = "background-color:${if (ship.owner == mySide) "#39F" else "#F66"};margin:10px;height:15px"
								}
							}
							repeat(downHull) {
								td {
									style = "background-color:${if (ship.owner == mySide) "#135" else "#522"};margin:10px;height:15px"
								}
							}
						}
					}
					
					val totalWeapons = ship.powerMode.weapons
					val activeWeapons = ship.weaponAmount
					val downWeapons = totalWeapons - activeWeapons
					
					if (ship.owner == mySide)
						table {
							style = "width:100%;table-layout:fixed;background-color:#555;margin:0"
							
							tr {
								repeat(activeWeapons) {
									td {
										style = "background-color:#F63;margin:10px;height:15px"
									}
								}
								repeat(downWeapons) {
									td {
										style = "background-color:#A42;margin:10px;height:15px"
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
				
				val fighterSide = ship.owner.relativeTo(mySide)
				val bomberSide = ship.owner.other.relativeTo(mySide)
				
				if (ship.fighterWings.isNotEmpty()) {
					span {
						val (borderColor, fillColor) = when (fighterSide) {
							LocalSide.BLUE -> "#39F" to "#135"
							LocalSide.RED -> "#F66" to "#522"
						}
						
						style = "display:inline-block;border:5px solid $borderColor;border-radius:15px;background-color:$fillColor;color:#fff"
						
						img(src = StrikeCraftWing.FIGHTERS.iconUrl, alt = StrikeCraftWing.FIGHTERS.displayName) {
							style = "width:1.125em"
						}
						
						+Entities.nbsp
						
						+ship.fighterWings.sumOf { (carrierId, wingId) ->
							(state.ships[carrierId]?.armaments?.weaponInstances?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
						}.toPercent()
					}
				}
				
				+Entities.nbsp
				
				if (ship.bomberWings.isNotEmpty()) {
					span {
						val (borderColor, fillColor) = when (bomberSide) {
							LocalSide.BLUE -> "#39F" to "#135"
							LocalSide.RED -> "#F66" to "#522"
						}
						
						style = "display:inline-block;border:5px solid $borderColor;border-radius:15px;background-color:$fillColor;color:#fff"
						
						img(src = StrikeCraftWing.BOMBERS.iconUrl, alt = StrikeCraftWing.BOMBERS.displayName) {
							style = "width:1.125em"
						}
						
						+Entities.nbsp
						
						+ship.bomberWings.sumOf { (carrierId, wingId) ->
							(state.ships[carrierId]?.armaments?.weaponInstances?.get(wingId) as? ShipWeaponInstance.Hangar)?.wingHealth ?: 0.0
						}.toPercent()
					}
				}
				
				+Entities.nbsp
				
				if (ship.strikeCraftDisrupted) {
					span {
						val (borderColor, fillColor) = when (ship.owner.relativeTo(mySide)) {
							LocalSide.BLUE -> "#39F" to "#135"
							LocalSide.RED -> "#F66" to "#522"
						}
						
						style = "display:inline-block;border:5px solid $borderColor;border-radius:15px;background-color:$fillColor;color:#fff"
						
						img(src = StrikeCraftWing.disruptedIconUrl, alt = "Strike Craft Disrupted") {
							style = "width:1.125em"
						}
					}
				}
			}
		}
	}
	
	private fun TagConsumer<*>.drawDeployPhase(state: GameState, abilities: List<PlayerAbilityType>) {
		val deployableShips = state.start.playerStart(mySide).deployableFleet
		val remainingPoints = state.battleInfo.size.numPoints - state.ships.values.filter { it.owner == mySide }.sumOf { it.ship.pointCost }
		
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
			deployableShips.forEach { (id, ship) ->
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
					
					hr { style = "border-color:#555" }
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
					
					strong(classes = "heading") {
						+ship.ship.fullName
					}
					
					br
					
					+ship.ship.shipType.fullerDisplayName
					
					if (ship.owner == mySide)
						table {
							style = "width:100%;table-layout:fixed;background-color:#555"
							tr {
								ShipSubsystem.values().forEach { subsystem ->
									val amount = ship.powerMode[subsystem]
									
									repeat(amount) {
										td {
											style = "background-color:${subsystem.htmlColor};margin:1px;height:0.55em"
										}
									}
								}
							}
						}
				}
				
				hr { style = "border-color:#555" }
				
				p {
					style = "height:69%;margin:0"
					
					shipAbilities.forEach { ability ->
						when (ability) {
							is PlayerAbilityType.DistributePower -> {
								val shipPowerMode = ClientAbilityData.newShipPowerModes[ship.id] ?: ship.powerMode
								
								table {
									style = "width:100%;table-layout:fixed;background-color:#555"
									tr {
										ShipSubsystem.values().forEach { subsystem ->
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
									+"Power Output: ${ship.ship.reactor.powerOutput}"
									br
									+"Remaining Transfers: ${ship.remainingGridEfficiency(shipPowerMode)}"
								}
								
								ShipSubsystem.values().forEach { transferFrom ->
									div(classes = "button-set row") {
										ShipSubsystem.values().filter { it != transferFrom }.forEach { transferTo ->
											button {
												style = "font-size:0.8em;padding:0 0.25em"
												title = "${transferFrom.displayName} to ${transferTo.displayName}"
												
												img(src = transferFrom.imageUrl, alt = transferFrom.displayName) {
													style = "width:0.95em;"
												}
												+Entities.nbsp
												img(src = ShipSubsystem.transferImageUrl, alt = " to ") {
													style = "width:0.95em"
												}
												+Entities.nbsp
												img(src = transferTo.imageUrl, alt = transferTo.displayName) {
													style = "width:0.95em;"
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
								
								p {
									style = "text-align:center"
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
								}
							}
							is PlayerAbilityType.MoveShip -> {
								p {
									style = "text-align:center"
									button {
										+"Move Ship"
										onClickFunction = { e ->
											e.preventDefault()
											responder.useAbility(ability)
										}
									}
								}
							}
						}
						
						hr { style = "border-color:#555" }
					}
					
					combatAbilities.forEach { ability ->
						br
						
						val weaponInstance = ship.armaments.weaponInstances.getValue(ability.weapon)
						
						val firingArcs = weaponInstance.weapon.firingArcs
						val firingArcsDesc = when (firingArcs) {
							FiringArc.FIRE_360 -> "360-Degree"
							FiringArc.FIRE_BROADSIDE -> "Broadside"
							setOf(FiringArc.ABEAM_PORT) -> "Port"
							setOf(FiringArc.ABEAM_STARBOARD) -> "Starboard"
							setOf(FiringArc.BOW) -> "Fore"
							setOf(FiringArc.STERN) -> "Rear"
							else -> null
						}.takeIf { weaponInstance !is ShipWeaponInstance.Hangar }
						
						val weaponVerb = if (weaponInstance is ShipWeaponInstance.Hangar) "Release" else "Fire"
						
						val weaponIsPlural = weaponInstance.weapon.numShots > 1
						
						val weaponDesc = when (weaponInstance) {
							is ShipWeaponInstance.Cannon -> "Cannon" + (if (weaponIsPlural) "s" else "")
							is ShipWeaponInstance.Lance -> "Lance" + (if (weaponIsPlural) "s" else "") + " (${weaponInstance.charge.toPercent()})"
							is ShipWeaponInstance.Hangar -> when (weaponInstance.weapon.wing) {
								StrikeCraftWing.FIGHTERS -> "Fighters"
								StrikeCraftWing.BOMBERS -> "Bombers"
							} + " (${weaponInstance.wingHealth.toPercent()})"
							is ShipWeaponInstance.Torpedo -> "Torpedo" + (if (weaponIsPlural) "es" else "")
							is ShipWeaponInstance.MegaCannon -> "Mega Giga Cannon (" + weaponInstance.remainingShots + ")"
							is ShipWeaponInstance.RevelationGun -> "Revelation Gun"
							is ShipWeaponInstance.PulseBeam -> "Small Craft Disruptor"
						}
						
						when (ability) {
							is PlayerAbilityType.ChargeLance -> {
								a(href = "#") {
									+"Charge "
									if (firingArcsDesc != null)
										+"$firingArcsDesc "
									+weaponDesc
									onClickFunction = { e ->
										e.preventDefault()
										responder.useAbility(ability)
									}
								}
							}
							is PlayerAbilityType.UseWeapon -> {
								a(href = "#") {
									+"$weaponVerb "
									if (firingArcsDesc != null)
										+"$firingArcsDesc "
									+weaponDesc
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
			
			p {
				style = "height:9%;margin:0"
				
				val finishPhase = abilities.filterIsInstance<PlayerAbilityType.DonePhase>().singleOrNull()
				if (finishPhase != null)
					a(href = "#") {
						+"End Your Phase"
						id = "done-phase"
						
						onClickFunction = { e ->
							e.preventDefault()
							finalizePhase(finishPhase, abilities)
						}
					}
				else
					span {
						style = "color:#333;cursor:not-allowed"
						+"End Your Phase"
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
				
				ClientAbilityData.newShipPowerModes.forEach { (shipId, _) ->
					val powerAbility = powerAbilities[shipId] ?: return@forEach
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
