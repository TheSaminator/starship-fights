package net.starshipfights.game

import externals.threejs.Group
import externals.threejs.Raycaster
import kotlinx.browser.document
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.starshipfights.data.Id
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent

val threeCanvas = document.getElementById("three-canvas").unsafeCast<HTMLCanvasElement>()

private val selectedShipMutable = MutableStateFlow<Id<ShipInstance>?>(null)
val selectedShip = selectedShipMutable.asStateFlow()

var isSelecting: Boolean = false
	private set

fun beginSelecting(context: PickContext) {
	if (isSelecting) return
	isSelecting = true
	
	val raycaster = Raycaster()
	
	val ships = context.threeScene.getObjectByName("ships").unsafeCast<Group>()
	
	threeCanvas.addEventListener("mousedown", { e ->
		if (isPicking) return@addEventListener
		if (e.unsafeCast<MouseEvent>().button.toInt() != 0) return@addEventListener
		
		raycaster.initializeFromMouse(context.threeCamera)
		val intersections = raycaster.intersectObjects(ships.children, true)
		selectedShipMutable.value = intersections.firstOrNull()?.`object`?.userData.unsafeCast<ShipRender?>()?.shipId
	})
}

suspend fun handleSelections(context: PickContext) {
	val ships = context.threeScene.getObjectByName("ships").unsafeCast<Group>()
	
	var prevId: Id<ShipInstance>? = null
	selectedShip.collect { shipId ->
		prevId?.let { id ->
			ships.getObjectByName(id.toString())?.userData.unsafeCast<ShipRender?>()?.shipOutline?.visible = false
		}
		
		shipId?.let { id ->
			ships.getObjectByName(id.toString())?.userData.unsafeCast<ShipRender?>()?.shipOutline?.visible = true
		}
		
		prevId = shipId
		
		GameUI.changeShipSelection(context.getGameState(), shipId)
	}
}
