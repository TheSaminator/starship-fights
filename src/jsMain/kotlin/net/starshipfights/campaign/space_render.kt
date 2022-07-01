package net.starshipfights.campaign

import externals.threejs.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.starshipfights.data.Id
import net.starshipfights.game.*
import kotlin.math.PI
import kotlin.math.sqrt

object CampaignResources {
	private val spaceboxUrls = StarClusterBackground.values().associateWith { "campaign/spacebox/${it.toUrlSlug()}" }
	
	lateinit var spaceboxes: Map<StarClusterBackground, Texture>
		private set
	
	private lateinit var starTypes: Map<StarType, RenderFactory>
	lateinit var star: CustomRenderFactory<CelestialObjectWithPointer<CelestialObject.Star>>
		private set
	
	private lateinit var planetTypes: Map<PlanetType, RenderFactory>
	lateinit var planet: CustomRenderFactory<CelestialObjectWithPointer<CelestialObject.Planet>>
		private set
	
	lateinit var starSystem: CustomRenderFactory<StarSystemWithId>
		private set
	
	private lateinit var warpLane: CustomRenderFactory<WarpLaneData>
	
	lateinit var starCluster: CustomRenderFactory<StarClusterView>
		private set
	
	suspend fun load() {
		warpLane = CustomRenderFactory { (systemA, systemB) ->
			val warpLaneMaterial = MeshBasicMaterial(configure { color = Color("#FFFFFF") })
			
			val aToBCenter = systemB.position - systemA.position
			val bToACenter = systemA.position - systemB.position
			
			val aCenterToEdge = aToBCenter.normal * systemA.radius
			val bCenterToEdge = bToACenter.normal * systemB.radius
			
			val aEdge = systemA.position + aCenterToEdge
			val bEdge = systemB.position + bCenterToEdge
			
			val warpLaneGeometry = TubeGeometry(
				LineCurve3(
					CampaignScaling.toWorldPosition(aEdge),
					CampaignScaling.toWorldPosition(bEdge),
				),
				1,
				0.24,
				4,
				false
			)
			
			Mesh(warpLaneGeometry, warpLaneMaterial)
		}
		
		coroutineScope {
			launch {
				spaceboxes = spaceboxUrls.mapValues { (_, it) ->
					async { loadTexture(it) }
				}.mapValues { (_, it) ->
					it.await()
				}.onEach { (_, it) ->
					it.mapping = EquirectangularReflectionMapping
				}
			}
			
			launch {
				starTypes = StarType.values().associateWith { starType ->
					async {
						if (starType == StarType.BLACK_HOLE) {
							val bhDiskTextureAsync = async { loadTexture("campaign/star/black-hole-disk") }
							val bhDiskWarpTextureAsync = async { loadTexture("campaign/star/black-hole-disk-warp") }
							
							val bhDiskTexture = bhDiskTextureAsync.await()
							val bhDiskWarpTexture = bhDiskWarpTextureAsync.await()
							
							val bhMat = MeshBasicMaterial(configure { color = Color("#000000") })
							val bhDiskMat = MeshBasicMaterial(configure { map = bhDiskTexture }).apply {
								depthWrite = false
								side = DoubleSide
								
								blending = CustomBlending
								blendEquation = AddEquation
								blendSrc = OneFactor
								blendDst = OneMinusSrcColorFactor
							}
							val bhDiskWarpMat = MeshBasicMaterial(configure { map = bhDiskWarpTexture }).apply { side = BackSide }
							
							val bhGeom = SphereGeometry(0.34)
							val bhDiskGeom = PlaneGeometry(2, 2)
							val bhDiskWarpGeom = SphereGeometry(0.38)
							
							val bhMesh = Mesh(bhGeom, bhMat)
							val bhDiskMesh = Mesh(bhDiskGeom, bhDiskMat)
							val bhDiskWarpMesh = Mesh(bhDiskWarpGeom, bhDiskWarpMat)
							val bhLight = PointLight(starType.lightColor.to3JS())
							
							bhDiskMesh.rotateX(PI / 2)
							
							val blackHole = Group().apply {
								add(bhMesh)
								add(bhDiskMesh)
								add(bhDiskWarpMesh)
								add(bhLight)
							}
							
							RenderFactory {
								blackHole.clone(true)
							}
						} else {
							val starTexture = loadTexture("campaign/star/${starType.toUrlSlug()}")
							val starMat = MeshBasicMaterial(configure {
								map = starTexture
							})
							
							val starGeom = SphereGeometry(1)
							val starMesh = Mesh(starGeom, starMat)
							val starLight = PointLight(starType.lightColor.to3JS())
							
							val star = Group().apply {
								add(starMesh)
								add(starLight)
							}
							
							RenderFactory {
								star.clone(true)
							}
						}
					}
				}.mapValues { (_, it) -> it.await() }
				
				star = CustomRenderFactory { (ptr, star) ->
					val obj = starTypes.getValue(star.type).generate()
					obj.scale.setScalar(CampaignScaling.toWorldScale(star.size))
					obj.position.copy(CampaignScaling.toWorldPosition(star.position))
					obj.userData = CelestialObjectRender(ptr)
					obj
				}
			}
			
			launch {
				planetTypes = PlanetType.values().associateWith { planetType ->
					async {
						when (planetType) {
							PlanetType.TERRESTRIAL -> {
								val planetTextureAsync = async { loadTexture("campaign/planet/terrestrial") }
								val planetCloudsTextureAsync = async { loadTexture("campaign/planet/terrestrial-clouds") }
								val planetSpecularTextureAsync = async { loadTexture("campaign/planet/terrestrial-specular") }
								
								val planetTexture = planetTextureAsync.await()
								val planetCloudsTexture = planetCloudsTextureAsync.await()
								val planetSpecularTexture = planetSpecularTextureAsync.await()
								
								val planetMat = MeshPhongMaterial(configure {
									map = planetTexture
									specular = Color("#777777")
									specularMap = planetSpecularTexture
								})
								val planetCloudsMat = MeshPhongMaterial(configure { map = planetCloudsTexture }).apply {
									depthWrite = false
									side = DoubleSide
									
									blending = CustomBlending
									blendEquation = AddEquation
									blendSrc = OneFactor
									blendDst = OneMinusSrcColorFactor
								}
								
								val planetGeom = SphereGeometry(0.95)
								val planetCloudsGeom = SphereGeometry(1)
								
								val planetMesh = Mesh(planetGeom, planetMat)
								val planetCloudsMesh = Mesh(planetCloudsGeom, planetCloudsMat)
								
								val planet = Group().apply {
									add(planetMesh)
									add(planetCloudsMesh)
								}
								
								RenderFactory {
									planet.clone(true)
								}
							}
							PlanetType.VEILED -> {
								val planetTexture = loadTexture("campaign/planet/veiled")
								
								val planetMat = MeshBasicMaterial(configure {
									map = planetTexture
								})
								
								val planetGeom = SphereGeometry(1)
								val planetMesh = Mesh(planetGeom, planetMat)
								
								RenderFactory {
									planetMesh.clone(true)
								}
							}
							PlanetType.VEILED_GIANT -> {
								val planetTextureAsync = async { loadTexture("campaign/planet/veiled-giant") }
								val planetEmissiveTextureAsync = async { loadTexture("campaign/planet/veiled-giant-emissive") }
								
								val planetTexture = planetTextureAsync.await()
								val planetEmissiveTexture = planetEmissiveTextureAsync.await()
								
								val planetMat = MeshPhongMaterial(configure {
									color = Color("#FFFFFF")
									map = planetTexture
									emissive = Color("#FFFFFF")
									emissiveMap = planetEmissiveTexture
								})
								
								val planetGeom = SphereGeometry(1)
								val planetMesh = Mesh(planetGeom, planetMat)
								
								RenderFactory {
									planetMesh.clone(true)
								}
							}
							PlanetType.ICE_SHELL -> {
								val planetTexture = loadTexture("campaign/planet/ice-shell")
								
								val planetMat = MeshPhongMaterial(configure {
									map = planetTexture
									specular = Color("#AACCEE")
									shininess = 30
								})
								
								val planetGeom = SphereGeometry(1)
								val planetMesh = Mesh(planetGeom, planetMat)
								
								RenderFactory {
									planetMesh.clone(true)
								}
							}
							else -> {
								val planetTexture = loadTexture("campaign/planet/${planetType.toUrlSlug()}")
								
								val planetMat = MeshPhongMaterial(configure { map = planetTexture })
								val planetGeom = SphereGeometry(1)
								val planetMesh = Mesh(planetGeom, planetMat)
								
								RenderFactory {
									planetMesh.clone(true)
								}
							}
						}
					}
				}.mapValues { (_, it) -> it.await() }
				
				planet = CustomRenderFactory { (ptr, planet) ->
					val obj = planetTypes.getValue(planet.type).generate()
					obj.scale.setScalar(CampaignScaling.toWorldScale(planet.size))
					obj.position.copy(CampaignScaling.toWorldPosition(planet.position))
					obj.userData = CelestialObjectRender(ptr)
					obj
				}
			}
		}
		
		starSystem = CustomRenderFactory { (ssId, starSystem) ->
			val torusGeom = TorusGeometry(CampaignScaling.toWorldLength(starSystem.radius), 0.25, 4, 64)
			val torusMat = MeshBasicMaterial(configure {
				color = (starSystem.holder?.getMapColor ?: IntColor(255, 255, 255)).to3JS()
			})
			
			val torus = Mesh(torusGeom, torusMat)
			torus.rotateX(PI / 2)
			
			val bodies = starSystem.bodies.map { (coId, celestialObject) ->
				val ptr = CelestialObjectPointer(ssId, coId)
				when (celestialObject) {
					is CelestialObject.Star -> star.generate(CelestialObjectWithPointer(ptr, celestialObject)).also { star3d ->
						star3d.children.singleOrNull { it.asDynamic().isLight == true }?.unsafeCast<PointLight>()?.let { starLight ->
							starLight.intensity = celestialObject.size.toDouble() / StarType.MAX_STAR_SIZE
							starLight.distance = CampaignScaling.toWorldLength(starSystem.radius) * 2
							starLight.decay = 0.5
						}
					}
					is CelestialObject.Planet -> planet.generate(CelestialObjectWithPointer(ptr, celestialObject))
				}
			}
			
			Group().apply {
				for (body in bodies)
					add(body)
				add(torus)
				
				position.copy(CampaignScaling.toWorldPosition(starSystem.position))
				userData = StarSystemRender(ssId)
			}
		}
		
		starCluster = CustomRenderFactory { cluster ->
			Group().apply {
				add(AmbientLight(cluster.background.ambientColor.to3JS()))
				
				for ((id, system) in cluster.systems)
					add(starSystem.generate(StarSystemWithId(id, system)))
				
				for (lane in cluster.lanes)
					add(warpLane.generate(lane.resolve(cluster) ?: continue))
				
				userData = "star cluster"
			}
		}
	}
}

object CampaignScaling {
	private const val CELESTIAL_BODY_SIZE_TO_3JS_UNITS_FACTOR = 0.25
	
	fun toWorldRotation(facing: Double, obj: Object3D) {
		obj.rotateY(facing)
	}
	
	fun toSpaceLength(length3js: Double) = length3js / CELESTIAL_BODY_SIZE_TO_3JS_UNITS_FACTOR
	fun toWorldLength(lengthSc: Double) = lengthSc * CELESTIAL_BODY_SIZE_TO_3JS_UNITS_FACTOR
	
	fun toSpacePosition(v3: Vector3) = Position(
		Vec2(v3.x.toDouble(), v3.z.toDouble()) / CELESTIAL_BODY_SIZE_TO_3JS_UNITS_FACTOR
	)
	
	fun toWorldPosition(pos: Position) = (pos.vector * CELESTIAL_BODY_SIZE_TO_3JS_UNITS_FACTOR).let { (x, y) ->
		Vector3(x, 0, y)
	}
	
	fun toWorldScale(size: Int) = sqrt(size * 32.0) * CELESTIAL_BODY_SIZE_TO_3JS_UNITS_FACTOR
}

external interface CelestialObjectRender {
	var isCelestialObject: Boolean
	var celestialObjectId: String
	var starSystemId: String
}

fun CelestialObjectRender(pointer: CelestialObjectPointer) = configure<CelestialObjectRender> {
	isCelestialObject = true
	celestialObjectId = pointer.celestialObject.id
	starSystemId = pointer.starSystem.id
}

val CelestialObjectRender.pointer: CelestialObjectPointer
	get() = CelestialObjectPointer(Id(starSystemId), Id(celestialObjectId))

val Object3D.celestialObjectRender: CelestialObjectPointer?
	get() = if (userData.isCelestialObject == true)
		userData.unsafeCast<CelestialObjectRender>().pointer
	else
		parent?.celestialObjectRender

val Object3D.celestialObjectRenderImmediate: CelestialObjectPointer?
	get() = if (userData.isCelestialObject == true)
		userData.unsafeCast<CelestialObjectRender>().pointer
	else
		null

external interface StarSystemRender {
	var isStarSystem: Boolean
	var starSystemIdString: String
}

fun StarSystemRender(id: Id<StarSystem>) = configure<StarSystemRender> {
	isStarSystem = true
	starSystemIdString = id.id
}

val StarSystemRender.starSystemId: Id<StarSystem>
	get() = Id(starSystemIdString)

val Object3D.starSystemRender: Id<StarSystem>?
	get() = if (userData.isStarSystem == true)
		userData.unsafeCast<StarSystemRender>().starSystemId
	else
		parent?.starSystemRender

val StarClusterBackground.ambientColor: IntColor
	get() = when (this) {
		StarClusterBackground.BLUE -> IntColor(34, 51, 85)
		StarClusterBackground.GOLD -> IntColor(85, 68, 17)
		StarClusterBackground.GRAY -> IntColor(51, 51, 51)
		StarClusterBackground.GREEN -> IntColor(0, 102, 34)
		StarClusterBackground.ORANGE -> IntColor(85, 51, 34)
		StarClusterBackground.PINK -> IntColor(85, 34, 51)
		StarClusterBackground.PURPLE -> IntColor(68, 34, 85)
		StarClusterBackground.RED -> IntColor(85, 0, 0)
	}

val Object3D.isStarCluster: Boolean
	get() = userData == "star cluster"
