package net.starshipfights.game

import externals.threejs.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.starshipfights.data.Id
import org.w3c.dom.Image
import kotlin.math.PI
import kotlin.math.roundToInt

fun interface RenderFactory {
	fun generate(): Object3D
}

fun interface CustomRenderFactory<T> {
	fun generate(parameter: T): Object3D
}

val ClientMode.isSmallLoad: Boolean
	get() = this !is ClientMode.InGame && this !is ClientMode.InTrainingGame

val ClientMode.usesOwnLoad: Boolean
	get() = this is ClientMode.CampaignMap

object RenderResources {
	const val LOGO_URL = "/static/images/logo.svg"
	
	private val spaceboxUrls = BattleBackground.values().associateWith { "battle/spacebox/${it.toUrlSlug()}" }
	
	lateinit var spaceboxes: Map<BattleBackground, Texture>
		private set
	
	private const val enemySignalUrl = "battle/enemy-signal"
	
	lateinit var enemySignal: CustomRenderFactory<Position>
		private set
	
	private const val gridTileUrl = "battle/grid-tile"
	lateinit var battleGrid: CustomRenderFactory<Pair<Double, Double>>
		private set
	
	private const val friendlyMarkerUrl = "battle/friendly-marker"
	private const val neutralMarkerUrl = "battle/neutral-marker"
	private const val hostileMarkerUrl = "battle/hostile-marker"
	
	lateinit var markerFactory: CustomRenderFactory<LocalSide>
		private set
	
	private val shipMeshesRaw = mutableMapOf<ShipType, RenderFactory>()
	
	private lateinit var shipMeshes: Map<ShipType, CustomRenderFactory<ShipInstance>>
	
	lateinit var shipMesh: CustomRenderFactory<ShipInstance>
		private set
	
	lateinit var shipHologramFactory: CustomRenderFactory<PickHelper.Ship>
		private set
	
	suspend fun load(isSmallLoad: Boolean = false) {
		coroutineScope {
			launch {
				val img = Image()
				val job = launch { img.awaitEvent("load") }
				img.src = LOGO_URL
				job.join()
			}
			
			launch {
				Faction.values().map { faction ->
					val img = Image()
					val job = launch { img.awaitEvent("load") }
					img.src = faction.flagUrl
					job
				}.joinAll()
			}
			
			launch {
				spaceboxes = spaceboxUrls.mapValues { (_, it) ->
					async { loadTexture(it) }
				}.mapValues { (_, it) ->
					it.await()
				}.onEach { (_, it) ->
					it.mapping = EquirectangularReflectionMapping
				}
			}
			
			if (isSmallLoad) return@coroutineScope
			
			initResCache()
			
			launch {
				val texture = loadTexture(enemySignalUrl)
				val material = SpriteMaterial(configure {
					map = texture
					blending = CustomBlending
					blendEquation = AddEquation
					blendSrc = OneFactor
					blendDst = OneMinusSrcColorFactor
				})
				
				val sprite = Sprite(material)
				sprite.scale.setScalar(4)
				sprite.position.set(0, 4, 0)
				
				enemySignal = CustomRenderFactory { pos ->
					Group().apply {
						add(sprite.clone(true))
						position.copy(RenderScaling.toWorldPosition(pos))
					}
				}
			}
			
			launch {
				val texture = loadTexture(gridTileUrl)
				texture.minFilter = LinearFilter
				texture.magFilter = LinearFilter
				
				battleGrid = CustomRenderFactory { (bfW, bfL) ->
					val w3d = RenderScaling.toWorldLength(bfW)
					val l3d = RenderScaling.toWorldLength(bfL)
					
					val gridTex = texture.clone().apply {
						wrapS = RepeatWrapping
						wrapT = RepeatWrapping
						repeat.set((w3d / 5).roundToInt(), (l3d / 5).roundToInt())
						needsUpdate = true
					}
					
					val material = MeshBasicMaterial(configure {
						map = gridTex
						
						side = DoubleSide
						depthWrite = false
						
						blending = CustomBlending
						blendEquation = AddEquation
						blendSrc = OneFactor
						blendDst = OneMinusSrcColorFactor
					})
					
					val plane = PlaneGeometry(w3d, l3d)
					val mesh = Mesh(plane, material)
					mesh.rotateX(PI / 2)
					
					Group().also {
						it.add(Group().apply {
							RenderScaling.toWorldRotation(PI / 2, this)
							add(mesh)
						})
						
						it.position.set(0, -0.02, 0)
						
						it.name = "plane"
					}
				}
			}
			
			launch {
				val friendlyMarkerAsync = async { loadTexture(friendlyMarkerUrl) }
				val neutralMarkerAsync = async { loadTexture(neutralMarkerUrl) }
				val hostileMarkerAsync = async { loadTexture(hostileMarkerUrl) }
				
				val friendlyMarkerTexture = friendlyMarkerAsync.await()
				friendlyMarkerTexture.minFilter = LinearFilter
				friendlyMarkerTexture.magFilter = LinearFilter
				
				val neutralMarkerTexture = neutralMarkerAsync.await()
				neutralMarkerTexture.minFilter = LinearFilter
				neutralMarkerTexture.magFilter = LinearFilter
				
				val hostileMarkerTexture = hostileMarkerAsync.await()
				hostileMarkerTexture.minFilter = LinearFilter
				hostileMarkerTexture.magFilter = LinearFilter
				
				val friendlyMarkerMaterial = MeshBasicMaterial(configure {
					map = friendlyMarkerTexture
					alphaTest = 0.5
					side = DoubleSide
				})
				
				val neutralMarkerMaterial = MeshBasicMaterial(configure {
					map = neutralMarkerTexture
					alphaTest = 0.5
					side = DoubleSide
				})
				
				val hostileMarkerMaterial = MeshBasicMaterial(configure {
					map = hostileMarkerTexture
					alphaTest = 0.5
					side = DoubleSide
				})
				
				val plane = PlaneGeometry(4, 4)
				
				val friendlyMarkerMesh = Mesh(plane, friendlyMarkerMaterial)
				val neutralMarkerMesh = Mesh(plane, neutralMarkerMaterial)
				val hostileMarkerMesh = Mesh(plane, hostileMarkerMaterial)
				
				friendlyMarkerMesh.rotateX(PI / 2)
				neutralMarkerMesh.rotateX(PI / 2)
				hostileMarkerMesh.rotateX(PI / 2)
				
				markerFactory = CustomRenderFactory { side ->
					when (side) {
						LocalSide.GREEN -> friendlyMarkerMesh
						LocalSide.BLUE -> neutralMarkerMesh
						LocalSide.RED -> hostileMarkerMesh
					}.clone(true)
				}
			}
			
			launch {
				val outlineMaterial = ShaderMaterial(configure {
					vertexShader = """
						|uniform float outlineGrow;

						|void main() {
						|	vec3 grownPosition = position + (normal * outlineGrow);
						|	gl_Position = projectionMatrix * modelViewMatrix * vec4(grownPosition, 1.0);
						|}
					""".trimMargin()
					
					fragmentShader = """
						|uniform vec3 outlineColor;

						|void main () {
						|	gl_FragColor = vec4( outlineColor, 1.0 );
						|}
					""".trimMargin()
					
					uniforms = configure<AnonymousStruct8> {
						this["outlineGrow"] = configure {
							value = 0.75
						}
						this["outlineColor"] = configure {
							value = Color("#AAAAAA")
						}
					}
				}).apply {
					side = BackSide
				}
				
				shipMeshes = ShipType.values().associateWith { st ->
					async { loadModel(st.meshName) }
				}.mapValues { (st, loadingMesh) ->
					val mesh = loadingMesh.await()
					mesh.scale.setScalar(RenderScaling.METERS_PER_3D_MESH_UNIT / RenderScaling.METERS_PER_THREEJS_UNIT)
					mesh.position.set(0, 4, 0)
					
					shipMeshesRaw[st] = RenderFactory { mesh.clone(true) }
					
					val greenOutlineMaterial = outlineMaterial.clone().unsafeCast<ShaderMaterial>().apply {
						uniforms["outlineColor"]!!.value = Color(LocalSide.GREEN.htmlColor)
					}
					
					val blueOutlineMaterial = outlineMaterial.clone().unsafeCast<ShaderMaterial>().apply {
						uniforms["outlineColor"]!!.value = Color(LocalSide.BLUE.htmlColor)
					}
					
					val redOutlineMaterial = outlineMaterial.clone().unsafeCast<ShaderMaterial>().apply {
						uniforms["outlineColor"]!!.value = Color(LocalSide.RED.htmlColor)
					}
					
					val outlineGreen = mesh.clone(true).unsafeCast<Mesh>()
					outlineGreen.material = greenOutlineMaterial
					
					val outlineBlue = mesh.clone(true).unsafeCast<Mesh>()
					outlineBlue.material = blueOutlineMaterial
					
					val outlineRed = mesh.clone(true).unsafeCast<Mesh>()
					outlineRed.material = redOutlineMaterial
					
					CustomRenderFactory { ship ->
						val faction = ship.ship.shipType.faction
						val flavor = ship.ship.shipFlavor
						val side = ship.owner.relativeTo(mySide)
						
						ShipRender(
							ship.id,
							markerFactory.generate(side).unsafeCast<Mesh>(),
							mesh.clone(true).unsafeCast<Mesh>().apply {
								material = material.unsafeCast<MeshPhongMaterial>().forShip(faction, flavor)
							},
							when (side) {
								LocalSide.GREEN -> outlineGreen
								LocalSide.BLUE -> outlineBlue
								LocalSide.RED -> outlineRed
							}.clone(true).unsafeCast<Mesh>()
						).group
					}
				}
				
				shipMesh = CustomRenderFactory { shipInstance ->
					shipMeshes.getValue(shipInstance.ship.shipType).generate(shipInstance).also { render ->
						RenderScaling.toWorldRotation(shipInstance.position.facing, render)
						render.position.copy(RenderScaling.toWorldPosition(shipInstance.position.location))
					}
				}
			}
			
			launch {
				val hologramMaterial = ShaderMaterial(configure {
					extensions = configure<AnonymousStruct81> {
						derivatives = true
					}
					
					vertexShader = """
						|varying float vNormalZ;

						|void main() {
						|	vNormalZ = normalize( normalMatrix * normal ).z;
						|	gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
						|}
					""".trimMargin()
					
					fragmentShader = """
						|uniform vec3 glowColor;
						|uniform float glowAmount;
						|varying float vNormalZ;

						|void main () {
						|	float colorCoeff = smoothstep( 0.0, 1.0, fwidth( vNormalZ * glowAmount ) );
						|	gl_FragColor = vec4( glowColor * colorCoeff, 1.0 );
						|}
					""".trimMargin()
					
					uniforms = configure<AnonymousStruct8> {
						this["glowColor"] = configure {
							value = Color("#ffffff")
						}
						this["glowAmount"] = configure {
							value = 3.5
						}
					}
				}).apply {
					userData = "hologram"
					
					depthTest = false
					side = DoubleSide
					
					blending = CustomBlending
					blendEquation = AddEquation
					blendSrc = OneFactor
					blendDst = OneMinusSrcColorFactor
				}
				
				shipHologramFactory = CustomRenderFactory { shipHelper ->
					val shipMesh = shipMeshesRaw.getValue(shipHelper.type).generate().unsafeCast<Mesh>()
					
					shipMesh.material = hologramMaterial.clone()
					
					shipMesh
				}
			}
		}
	}
}

external interface ShipRender {
	var shipIdString: String
	
	var bottomMarker: Mesh
	var shipMesh: Mesh
	var shipOutline: Mesh
	var group: Group
}

val ShipRender.shipId: Id<ShipInstance>
	get() = Id(shipIdString)

fun ShipRender(
	shipId: Id<ShipInstance>,
	
	bottomMarker: Mesh,
	shipMesh: Mesh,
	shipOutline: Mesh
) = configure<ShipRender> {
	this.shipIdString = shipId.id
	this.bottomMarker = bottomMarker
	this.shipMesh = shipMesh
	this.shipOutline = shipOutline
	this.group = Group().also { g ->
		bottomMarker.userData = this
		shipMesh.userData = this
		shipOutline.userData = this
		
		shipOutline.visible = false
		
		g.add(bottomMarker, shipMesh, shipOutline)
		g.name = shipId.toString()
		g.userData = this
	}
}
