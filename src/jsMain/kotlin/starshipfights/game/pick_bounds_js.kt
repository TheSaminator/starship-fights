package starshipfights.game

import externals.threejs.*
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import kotlin.coroutines.resume
import kotlin.math.PI

private val validColor = Color("#3399ff")
private val invalidColor = Color("#ff6666")

private fun PickHelper.generateHologram(): Group = when (this) {
	PickHelper.None -> Group()
	is PickHelper.Ship -> {
		Group().also {
			it.add(RenderResources.shipHologramFactory.generate(this))
			RenderScaling.toWorldRotation(facing, it)
		}
	}
	is PickHelper.Circle -> {
		val shape = Shape()
			.ellipse(
				0,
				0,
				RenderScaling.toWorldLength(radius),
				RenderScaling.toWorldLength(radius),
				0,
				2 * PI,
				false,
				0
			)
			.unsafeCast<Shape>()
		val geometry = ShapeGeometry(shape)
		
		val material = MeshBasicMaterial(configure {
			side = DoubleSide
			
			color = "#AAAAAA"
			
			depthWrite = false
			
			blending = CustomBlending
			blendEquation = AddEquation
			blendSrc = OneFactor
			blendDst = OneMinusSrcColorFactor
			
			userData = "screen"
		})
		
		Group().apply {
			add(Mesh(geometry, material).apply {
				rotateX(PI / 2)
			})
		}
	}
}

private fun Line.drawLocation(drawFrom: Position?, drawTo: Position?) {
	if (drawFrom == null || drawTo == null) {
		geometry.setFromPoints(emptyArray<Vector3>())
		return
	}
	
	geometry.setFromPoints(
		arrayOf(
			RenderScaling.toWorldPosition(drawFrom),
			RenderScaling.toWorldPosition(drawTo),
		)
	)
}

private fun Object3D.setLocation(context: PickContext, request: PickRequest, location: Position?) {
	if (children.isEmpty()) return
	
	if (location == null) {
		visible = false
		return
	}
	
	visible = true
	position.copy(RenderScaling.toWorldPosition(location))
	
	val response = PickResponse.Location(location)
	
	val material = children.single().unsafeCast<Mesh>().material.unsafeCast<Material>()
	val materialColor = if (context.getGameState().isValidPick(request, response))
		validColor
	else
		invalidColor
	
	if (material.userData == "hologram")
		material.unsafeCast<ShaderMaterial>().uniforms["glowColor"]!!.value = materialColor
	else
		material.unsafeCast<MeshBasicMaterial>().color = materialColor
}

private var handleCanvasMouseMove: (MouseEvent) -> Unit = { _ -> }
private var handleCanvasMouseDown: (MouseEvent) -> Boolean = { _ -> false }
private var handleWindowEscapeKey: (KeyboardEvent) -> Unit = { _ -> }

fun Raycaster.initializeFromMouse(camera: Camera) {
	setFromCamera(configure {
		x = mouseLocation.x
		y = mouseLocation.y
	}, camera)
}

private var mouseLocation: Vector2 = Vector2(0, 0)

fun initializePicking() {
	threeCanvas.addEventListener("mousemove", { e ->
		val me = e.unsafeCast<MouseEvent>()
		
		val normalX = (me.clientX.toDouble() / window.innerWidth) * 2 - 1
		val normalY = 1 - (me.clientY.toDouble() / window.innerHeight) * 2
		mouseLocation = Vector2(normalX, normalY)
		
		handleCanvasMouseMove(me)
	})
	
	threeCanvas.addEventListener("mousedown", { e ->
		val me = e.unsafeCast<MouseEvent>()
		if (me.button.toInt() == 0)
			if (handleCanvasMouseDown(me))
				me.stopImmediatePropagation()
	})
	
	window.addEventListener("keydown", { e ->
		val ke = e.unsafeCast<KeyboardEvent>()
		if (ke.key == "Escape")
			handleWindowEscapeKey(ke)
	})
}

data class PickContext(
	val threeScene: Scene,
	val threeCamera: Camera,
	val getGameState: () -> GameState,
)

private fun PickRequest.locate(intersections: Array<Intersection>): Position? {
	return when (type) {
		is PickType.Location -> intersections.firstOrNull()?.point?.let { boundary.normalize(RenderScaling.toBattlePosition(it)) }
		else -> error("PickRequest.locate cannot be used on non-PickType.Location picks!")
	}
}

private fun PickRequest.verify(context: PickContext, intersections: Array<Intersection>): PickResponse? {
	return when (type) {
		is PickType.Location -> {
			val intersection3d = (intersections.firstOrNull() ?: return null).point
			val intersection = RenderScaling.toBattlePosition(intersection3d)
			val pickResponse = PickResponse.Location(boundary.normalize(intersection))
			
			pickResponse.takeIf { context.getGameState().isValidPick(this, it) }
		}
		is PickType.Ship -> {
			val shipId = (intersections.firstOrNull() ?: return null).`object`.userData.unsafeCast<ShipRender>().shipId
			val pickResponse = PickResponse.Ship(shipId)
			
			pickResponse.takeIf { context.getGameState().isValidPick(this, it) }
		}
	}
}

var isPicking: Boolean = false
	private set

private fun beginPick(context: PickContext, pickRequest: PickRequest, responseHandler: (PickResponse?) -> Unit) {
	isPicking = true
	
	if (pickRequest.boundary is PickBoundary.AlongLine) {
		val pointA = RenderScaling.toWorldPosition(pickRequest.boundary.pointA)
		val pointB = RenderScaling.toWorldPosition(pickRequest.boundary.pointB)
		val bufferGeometry = BufferGeometry().setFromPoints(arrayOf(pointA, pointB))
		val material = LineBasicMaterial(configure {
			color = "#4477DD"
		})
		
		val line = Line(bufferGeometry, material)
		line.name = "bound"
		context.threeScene.add(line)
	} else {
		val meshGroup = Group().also {
			it.position.set(0, -0.01, 0)
			
			it.name = "bound"
		}
		
		pickRequest.boundary.render().forEach { shape ->
			val shapeGeometry = ShapeGeometry(shape)
			val material = MeshBasicMaterial(configure {
				side = DoubleSide
				
				color = "#3366CC"
				
				depthWrite = false
				
				blending = CustomBlending
				blendEquation = AddEquation
				blendSrc = OneFactor
				blendDst = OneMinusSrcColorFactor
			})
			
			val mesh = Mesh(shapeGeometry, material)
			mesh.rotateX(PI / 2)
			
			meshGroup.add(Group().apply {
				RenderScaling.toWorldRotation(PI / 2, this)
				add(mesh)
			})
		}
		
		context.threeScene.add(meshGroup)
	}
	
	val raycaster = Raycaster()
	
	when (pickRequest.type) {
		is PickType.Location -> {
			val plane = context.threeScene.getObjectByName("plane").unsafeCast<Group>()
			
			raycaster.initializeFromMouse(context.threeCamera)
			val firstIntersections = raycaster.intersectObject(plane, true)
			val firstLocation = pickRequest.locate(firstIntersections)
			
			val pickHelperMesh = pickRequest.type.helper.generateHologram()
			pickHelperMesh.name = "pick-helper"
			pickHelperMesh.setLocation(context, pickRequest, firstLocation)
			
			val drawnLine = Line(BufferGeometry(), LineBasicMaterial(configure {
				color = "#6699FF"
			})).apply {
				name = "pick-line"
			}
			drawnLine.drawLocation(pickRequest.type.drawLineFrom, firstLocation)
			
			context.threeScene.add(pickHelperMesh, drawnLine)
			
			handleCanvasMouseMove = { _ ->
				raycaster.initializeFromMouse(context.threeCamera)
				
				val intersections = raycaster.intersectObject(plane, true)
				val location = pickRequest.locate(intersections)
				
				pickHelperMesh.setLocation(context, pickRequest, location)
				drawnLine.drawLocation(pickRequest.type.drawLineFrom, location)
				
				threeCanvas.style.cursor = if (pickRequest.verify(context, intersections) != null)
					"pointer"
				else "not-allowed"
			}
			
			handleCanvasMouseDown = { _ ->
				raycaster.initializeFromMouse(context.threeCamera)
				
				val intersections = raycaster.intersectObject(plane, true)
				val location = pickRequest.locate(intersections)
				
				pickHelperMesh.setLocation(context, pickRequest, location)
				drawnLine.drawLocation(pickRequest.type.drawLineFrom, location)
				
				responseHandler(pickRequest.verify(context, intersections))
				true
			}
		}
		is PickType.Ship -> {
			val ships = context.threeScene.getObjectByName("ships").unsafeCast<Group>()
			
			handleCanvasMouseMove = { _ ->
				raycaster.initializeFromMouse(context.threeCamera)
				
				val intersections = raycaster.intersectObjects(ships.children, true)
				
				threeCanvas.style.cursor = if (pickRequest.verify(context, intersections) != null)
					"pointer"
				else "not-allowed"
			}
			
			handleCanvasMouseDown = { _ ->
				raycaster.initializeFromMouse(context.threeCamera)
				
				val intersections = raycaster.intersectObjects(ships.children, true)
				
				responseHandler(pickRequest.verify(context, intersections))
				true
			}
		}
	}
	
	handleWindowEscapeKey = { responseHandler(null) }
	
	GameUI.currentHelpMessage = "Press Escape to cancel current action"
}

private fun endPick(scene: Scene) {
	isPicking = false
	
	handleCanvasMouseMove = { _ -> }
	handleCanvasMouseDown = { _ -> false }
	handleWindowEscapeKey = { _ -> }
	
	threeCanvas.style.cursor = "auto"
	
	scene.getObjectByName("bound")?.removeFromParent()
	scene.getObjectByName("pick-helper")?.removeFromParent()
	scene.getObjectByName("pick-line")?.removeFromParent()
	
	GameUI.currentHelpMessage = ""
}

private val pickMutex = Mutex()
suspend fun PickRequest.pick(context: PickContext): PickResponse? = pickMutex.withLock {
	suspendCancellableCoroutine { cancellableContinuation ->
		beginPick(context, this) {
			endPick(context.threeScene)
			cancellableContinuation.resume(it)
		}
		
		cancellableContinuation.invokeOnCancellation {
			endPick(context.threeScene)
		}
	}
}

private fun FiringArc.getStartAngle(shipFacing: Double) = (when (this) {
	FiringArc.BOW -> Vec2(1.0, -1.0)
	FiringArc.ABEAM_PORT -> Vec2(-1.0, -1.0)
	FiringArc.ABEAM_STARBOARD -> Vec2(1.0, 1.0)
	FiringArc.STERN -> Vec2(-1.0, 1.0)
} rotatedBy shipFacing).angle

private fun FiringArc.getEndAngle(shipFacing: Double) = (when (this) {
	FiringArc.BOW -> Vec2(1.0, 1.0)
	FiringArc.ABEAM_PORT -> Vec2(1.0, -1.0)
	FiringArc.ABEAM_STARBOARD -> Vec2(-1.0, 1.0)
	FiringArc.STERN -> Vec2(-1.0, -1.0)
} rotatedBy shipFacing).angle

private fun PickBoundary.render(): List<Shape> {
	return when (this) {
		is PickBoundary.Angle -> {
			val position = center.vector
			
			val startTheta = (normalVector(midAngle) rotatedBy -maxAngle).angle
			val endTheta = (normalVector(midAngle) rotatedBy maxAngle).angle
			
			val innerDistance = 275.0
			val outerDistance = 350.0
			
			val startVec = polarVector(innerDistance, startTheta) + position
			
			listOf(
				Shape()
					.moveTo(RenderScaling.toWorldLength(startVec.x), RenderScaling.toWorldLength(startVec.y))
					.absarc(RenderScaling.toWorldLength(position.x), RenderScaling.toWorldLength(position.y), RenderScaling.toWorldLength(innerDistance), startTheta, endTheta, false)
					.absarc(RenderScaling.toWorldLength(position.x), RenderScaling.toWorldLength(position.y), RenderScaling.toWorldLength(outerDistance), endTheta, startTheta, true)
					.unsafeCast<Shape>()
			)
		}
		is PickBoundary.AlongLine -> emptyList() // Handled in a special case
		is PickBoundary.Rectangle -> listOf(
			Shape()
				.moveTo(RenderScaling.toWorldLength(center.vector.x + width2), RenderScaling.toWorldLength(center.vector.y + length2))
				.lineTo(RenderScaling.toWorldLength(center.vector.x - width2), RenderScaling.toWorldLength(center.vector.y + length2))
				.lineTo(RenderScaling.toWorldLength(center.vector.x - width2), RenderScaling.toWorldLength(center.vector.y - length2))
				.lineTo(RenderScaling.toWorldLength(center.vector.x + width2), RenderScaling.toWorldLength(center.vector.y - length2))
				.unsafeCast<Shape>()
		)
		is PickBoundary.Ellipse -> listOf(
			Shape()
				.ellipse(
					RenderScaling.toWorldLength(center.vector.x),
					RenderScaling.toWorldLength(center.vector.y),
					RenderScaling.toWorldLength(widthRadius),
					RenderScaling.toWorldLength(lengthRadius),
					0,
					2 * PI,
					false,
					rotationAngle
				)
				.unsafeCast<Shape>()
		)
		is PickBoundary.WeaponsFire -> firingArcs.map { firingArc ->
			val position = center.vector
			
			val startTheta = firingArc.getStartAngle(facing)
			val endTheta = firingArc.getEndAngle(facing)
			
			val startPosMin = polarVector(minDistance, startTheta) + position
			
			Shape()
				.moveTo(RenderScaling.toWorldLength(startPosMin.x), RenderScaling.toWorldLength(startPosMin.y))
				.absarc(RenderScaling.toWorldLength(position.x), RenderScaling.toWorldLength(position.y), RenderScaling.toWorldLength(minDistance), startTheta, endTheta, false)
				.absarc(RenderScaling.toWorldLength(position.x), RenderScaling.toWorldLength(position.y), RenderScaling.toWorldLength(maxDistance), endTheta, startTheta, true)
				.unsafeCast<Shape>()
		}
	}
}
