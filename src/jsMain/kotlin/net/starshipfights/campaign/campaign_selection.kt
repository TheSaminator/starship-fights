package net.starshipfights.campaign

import externals.threejs.PerspectiveCamera
import externals.threejs.Raycaster
import externals.threejs.Scene
import externals.threejs.Vector3
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.starshipfights.data.Id
import net.starshipfights.game.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.MouseEvent

sealed class Selection {
	object None : Selection()
	data class System(val id: Id<StarSystem>) : Selection()
	data class CelestialObject(val pointer: CelestialObjectPointer) : Selection()
}

private val selectionMutable = MutableStateFlow<Selection>(Selection.None)
val selection = selectionMutable.asStateFlow()

fun clearSelection(): Selection {
	selectionMutable.value = Selection.None
	return selectionMutable.value
}

private var selectionHandler: EventListener? = null

fun addSelectionHandler(starClusterView: StarClusterView, camera: PerspectiveCamera, scene: Scene) {
	selectionHandler?.let { return }
	
	val raycaster = Raycaster()
	
	val listener = object : EventListener {
		override fun handleEvent(event: Event) {
			val me = event.unsafeCast<MouseEvent>()
			
			if (me.button.toInt() != 0) return
			
			raycaster.setFromCamera(configure {
				x = (me.clientX.toDouble() / window.innerWidth) * 2 - 1
				y = 1 - (me.clientY.toDouble() / window.innerHeight) * 2
			}, camera)
			
			val position = raycaster.intersectXZPlane()
			if (position == null) {
				selectionMutable.value = Selection.None
				return
			}
			
			val system = starClusterView.systems.entries.singleOrNull { (_, it) -> (it.position - position).length <= it.radius }
			if (system == null) {
				selectionMutable.value = Selection.None
				return
			}
			
			val systemRender = scene.children
				.single { it.isStarCluster }.children
				.singleOrNull { it.starSystemRender == system.key }
			
			if (systemRender == null) {
				selectionMutable.value = Selection.None
				return
			}
			
			val celestialObject = raycaster.intersectObject(systemRender, recursive = true).firstOrNull()?.`object`?.celestialObjectRender
			
			if (celestialObject == null)
				selectionMutable.value = Selection.System(system.key)
			else
				selectionMutable.value = Selection.CelestialObject(celestialObject)
		}
	}
	
	threeCanvas.addEventListener("mousedown", listener)
	selectionHandler = listener
}

fun removeSelectionHandler() {
	selectionHandler?.let { threeCanvas.removeEventListener("mousedown", it) }
	selectionHandler = null
}

private fun Raycaster.intersectXZPlane(): Position? {
	val denominator = -ray.direction.y.toDouble()
	if (denominator > EPSILON) {
		val t = ray.origin.y.toDouble() / denominator
		if (t >= 0) {
			val worldPos = Vector3().add(ray.direction).multiplyScalar(t).add(ray.origin)
			return CampaignScaling.toSpacePosition(worldPos)
		}
	}
	
	return null
}
