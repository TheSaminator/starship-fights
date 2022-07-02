package net.starshipfights.campaign

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
import net.starshipfights.data.Id
import net.starshipfights.game.*
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import kotlin.math.PI

interface CampaignUIResponder {
	fun getStarCluster(): StarClusterView
}

object CampaignUI {
	private lateinit var responder: CampaignUIResponder
	
	private val campaignUI = document.getElementById("ui").unsafeCast<HTMLDivElement>()
	
	private lateinit var topRightBar: HTMLDivElement
	
	private lateinit var systemsOverlay: HTMLElement
	private lateinit var systemsOverlayRenderer: CSS3DRenderer
	private lateinit var systemsOverlayCamera: PerspectiveCamera
	private lateinit var systemsOverlayScene: Scene
	
	private val selectedSystemIndicators = mutableMapOf<Id<StarSystem>, CSS3DObject>()
	private val visibleSelectedSystemIndicators = mutableSetOf<Id<StarSystem>>()
	
	private val selectedObjectIndicators = mutableMapOf<CelestialObjectPointer, CSS3DSprite>()
	private val visibleSelectedObjectIndicators = mutableSetOf<CelestialObjectPointer>()
	
	private lateinit var errorMessages: HTMLParagraphElement
	private lateinit var helpMessages: HTMLParagraphElement
	
	fun initCampaignUI(responder: CampaignUIResponder) {
		this.responder = responder
		
		campaignUI.clear()
		campaignUI.append {
			div(classes = "panel") {
				id = "top-right-bar"
			}
			
			p {
				id = "error-messages"
			}
			
			p {
				id = "help-messages"
			}
		}
		
		topRightBar = document.getElementById("top-right-bar").unsafeCast<HTMLDivElement>()
		
		errorMessages = document.getElementById("error-messages").unsafeCast<HTMLParagraphElement>()
		helpMessages = document.getElementById("help-messages").unsafeCast<HTMLParagraphElement>()
		
		systemsOverlayRenderer = CSS3DRenderer()
		systemsOverlayRenderer.setSize(window.innerWidth, window.innerHeight)
		
		systemsOverlay = systemsOverlayRenderer.domElement
		campaignUI.prepend(systemsOverlay)
		
		systemsOverlayCamera = PerspectiveCamera(69, window.aspectRatio, 0.01, 1_000)
		systemsOverlayScene = Scene()
		
		window.addEventListener("resize", {
			systemsOverlayCamera.aspect = window.aspectRatio
			systemsOverlayCamera.updateProjectionMatrix()
			
			systemsOverlayRenderer.setSize(window.innerWidth, window.innerHeight)
		})
		
		for ((systemId, system) in responder.getStarCluster().systems) {
			val systemWithId = StarSystemWithId(systemId, system)
			systemsOverlayScene.add(CSS3DSprite(document.create.div {
				drawSystemLabel(systemWithId)
			}).apply {
				scale.setScalar(0.125)
				
				element.style.asDynamic().pointerEvents = "none"
				
				position.copy(CampaignScaling.toWorldPosition(system.position))
				position.y = 50
			})
			
			selectedSystemIndicators[systemId] = CSS3DObject(document.create.img(src = "/static/game/images/crosshair-round.svg")).apply {
				scale.setScalar(0.00125 * CampaignScaling.toWorldLength(system.radius))
				
				element.style.asDynamic().pointerEvents = "none"
				
				rotateX(PI / 2)
				
				position.copy(CampaignScaling.toWorldPosition(system.position))
				
				visible = false
			}.also { systemsOverlayScene.add(it) }
			
			for ((objectId, celestialObject) in system.bodies) {
				val ptr = CelestialObjectPointer(systemId, objectId)
				selectedObjectIndicators[ptr] = CSS3DSprite(document.create.img(src = "/static/game/images/crosshair.svg")).apply {
					scale.setScalar(0.00125 * CampaignScaling.toWorldScale(celestialObject.size))
					
					element.style.asDynamic().pointerEvents = "none"
					
					position.copy(CampaignScaling.toWorldPosition(celestialObject.position - Position(Vec2(0.0, 0.0)) + system.position))
					
					visible = false
				}.also { systemsOverlayScene.add(it) }
			}
		}
		
		textFit(document.getElementsByClassName("system-label"))
	}
	
	fun renderCampaignUI(controls: CampaignCameraControls) {
		systemsOverlayCamera.position.copy(controls.camera.getWorldPosition(systemsOverlayCamera.position))
		systemsOverlayCamera.quaternion.copy(controls.camera.getWorldQuaternion(systemsOverlayCamera.quaternion))
		systemsOverlayRenderer.render(systemsOverlayScene, systemsOverlayCamera)
	}
	
	fun updateCampaignUI() {
	
	}
	
	private fun selectionRender(selection: Selection) {
		val starCluster = responder.getStarCluster()
		
		for (id in visibleSelectedSystemIndicators) {
			selectedSystemIndicators[id]?.visible = false
		}
		visibleSelectedSystemIndicators.clear()
		
		for (ptr in visibleSelectedObjectIndicators) {
			selectedObjectIndicators[ptr]?.visible = false
		}
		visibleSelectedObjectIndicators.clear()
		
		topRightBar.clear()
		topRightBar.append {
			when (selection) {
				Selection.None -> {
					p {
						style = "text-align:center"
						+"Click on a planet, star, or star system to select it."
					}
				}
				is Selection.System -> {
					val system = selection.id.resolve(starCluster) ?: return@append selectionRender(clearSelection())
					
					p {
						style = "text-align:center"
						strong(classes = "heading") {
							+system.name
							+" system"
						}
					}
					p {
						style = "text-align:center"
						+(system.holder?.loyalties?.first()?.getDefiniteShortName()?.let { "Controlled by $it" } ?: "Wilderness")
					}
					
					selectedSystemIndicators[selection.id]?.visible = true
					visibleSelectedSystemIndicators += selection.id
				}
				is Selection.CelestialObject -> {
					val celestialObject = selection.pointer.resolve(starCluster) ?: return@append selectionRender(clearSelection())
					
					p {
						style = "text-align:center"
						strong(classes = "heading") {
							+celestialObject.name
						}
					}
					p {
						style = "text-align:center"
						+"Size ${celestialObject.size} "
						+when (celestialObject) {
							is CelestialObject.Star -> celestialObject.type.displayName
							is CelestialObject.Planet -> celestialObject.type.displayName
						}
					}
					
					selectedObjectIndicators[selection.pointer]?.visible = true
					visibleSelectedObjectIndicators += selection.pointer
				}
			}
		}
	}
	
	suspend fun selectionUpdate() {
		selection.collect { selection ->
			selectionRender(selection)
		}
	}
	
	suspend fun displayErrorMessage(message: String) {
		errorMessages.textContent = message
		
		delay(5000)
		
		errorMessages.textContent = ""
	}
	
	private fun DIV.drawSystemLabel(system: StarSystemWithId) {
		id = "system-overlay-${system.id}"
		classes = setOf("system-overlay")
		style = "background-color:#999;width:640px;height:125px;opacity:0.8;font-size:4em;text-align:center;vertical-align:middle"
		attributes["data-ship-id"] = system.id.toString()
		
		p(classes = "system-label") {
			val color = system.starSystem.holder?.getMapColor?.toString() ?: "#fff"
			style = "color:$color;margin:0;white-space:nowrap;width:640px;height:125px"
			+system.starSystem.name
		}
		
		p(classes = "system-faction") {
			style = "text-align:center;color:#fff;margin:0;white-space:nowrap;width:640px;height:125px"
			system.starSystem.holder?.let { flavor ->
				img(src = flavor.flagUrl, alt = flavor.displayName) {
					style = "width:200px;height:125px"
				}
			}
		}
	}
}
