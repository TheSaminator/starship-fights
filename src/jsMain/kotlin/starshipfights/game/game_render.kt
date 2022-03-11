package starshipfights.game

import externals.threejs.*

object GameRender {
	fun renderGameState(scene: Scene, state: GameState) {
		scene.background = RenderResources.spaceboxes.getValue(state.battleInfo.bg)
		scene.getObjectByName("light")?.removeFromParent()
		scene.add(AmbientLight(state.battleInfo.bg.color).apply { name = "light" })
		
		val shipGroup = scene.getObjectByName("ships") ?: Group().apply { name = "ships" }.also { scene.add(it) }
		
		shipGroup.clear()
		
		state.ships.forEach { (_, ship) ->
			when (state.renderShipAs(ship, mySide)) {
				ShipRenderMode.NONE -> {}
				ShipRenderMode.SIGNAL -> shipGroup.add(RenderResources.enemySignal.generate(ship.position.location))
				ShipRenderMode.FULL -> shipGroup.add(RenderResources.shipMesh.generate(ship))
			}
		}
	}
}

object RenderScaling {
	const val METERS_PER_THREEJS_UNIT = 100.0
	const val METERS_PER_3D_MESH_UNIT = 6.9
	
	fun toWorldRotation(facing: Double, obj: Object3D) {
		obj.rotateY(-facing)
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
