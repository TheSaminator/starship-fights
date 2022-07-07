package net.starshipfights.game

import externals.threejs.*
import net.starshipfights.data.Id

object GameRender {
	private val shipMeshCache = mutableMapOf<Id<ShipInstance>, Object3D>()
	
	fun renderGameState(scene: Scene, state: GameState) {
		scene.background = RenderResources.spaceboxes.getValue(state.battleInfo.bg)
		scene.getObjectByName("light")?.removeFromParent()
		scene.add(AmbientLight(state.battleInfo.bg.color).apply { name = "light" })
		
		val shipGroup = scene.getObjectByName("ships") ?: Group().apply { name = "ships" }.also { scene.add(it) }
		
		shipGroup.clear()
		
		for (ship in state.ships.values) {
			when (state.renderShipAs(ship, mySide)) {
				ShipRenderMode.NONE -> {}
				ShipRenderMode.SIGNAL -> shipGroup.add(RenderResources.enemySignal.generate(ship.position.location))
				ShipRenderMode.FULL -> shipGroup.add(shipMeshCache[ship.id]?.also { render ->
					RenderScaling.toWorldRotation(ship.position.facing, render)
					render.position.copy(RenderScaling.toWorldPosition(ship.position.location))
				} ?: RenderResources.shipMesh.generate(ship).also { shipMeshCache[ship.id] = it })
			}
		}
	}
}

object RenderScaling {
	const val METERS_PER_THREEJS_UNIT = 100.0
	const val METERS_PER_3D_MESH_UNIT = 6.9
	
	fun toWorldRotation(facing: Double, obj: Object3D) {
		obj.rotation.y = -facing
	}
	
	fun toBattleLength(length3js: Double) = length3js * METERS_PER_THREEJS_UNIT
	fun toWorldLength(lengthSf: Double) = lengthSf / METERS_PER_THREEJS_UNIT
	
	fun toBattlePosition(v3: Vector3) = Position(
		Vec2(v3.z.toDouble(), -v3.x.toDouble()) * METERS_PER_THREEJS_UNIT
	)
	
	fun toWorldPosition(pos: Position) = (pos.vector / METERS_PER_THREEJS_UNIT).let { (x, y) ->
		Vector3(-y, 0, x)
	}
}
