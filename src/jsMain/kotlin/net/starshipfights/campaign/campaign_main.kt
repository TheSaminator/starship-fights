package net.starshipfights.campaign

import externals.threejs.CampaignCameraControls
import externals.threejs.PerspectiveCamera
import externals.threejs.Scene
import externals.threejs.WebGLRenderer
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import net.starshipfights.data.Id
import net.starshipfights.game.*

var mySide: CampaignAdmiral? = null

suspend fun campaignMain(playingAs: Id<InGameAdmiral>, admirals: Map<Id<InGameAdmiral>, CampaignAdmiral>, clusterToken: Id<StarClusterView>, clusterView: StarClusterView) {
	Popup.LoadingScreen("Loading resources...") {
		CampaignResources.load()
	}.display()
	
	mySide = admirals[playingAs]
	
	val updateLoop = Popup.LoadingScreen<FlowCollector<Double>>("Rendering cluster...") {
		delay(500L)
		
		val camera = PerspectiveCamera(69, window.aspectRatio, 0.25, 4_000)
		
		val renderer = WebGLRenderer(configure {
			canvas = document.getElementById("three-canvas")
			antialias = true
		})
		
		renderer.setPixelRatio(window.devicePixelRatio)
		renderer.setSize(window.innerWidth, window.innerHeight)
		
		val scene = Scene()
		scene.background = CampaignResources.spaceboxes.getValue(clusterView.background)
		scene.add(camera)
		scene.add(CampaignResources.starCluster.generate(clusterView))
		
		val cameraControls = CampaignCameraControls(camera, configure {
			domElement = renderer.domElement
			keyDomElement = window
			
			panSpeed = 384
			zoomSpeed = 144
			
			minZoom = 72
			maxZoom = 512
			
			cameraXMin = CampaignScaling.toWorldLength(clusterView.systems.values.minOf { it.position.vector.x - it.radius })
			cameraXMax = CampaignScaling.toWorldLength(clusterView.systems.values.maxOf { it.position.vector.x + it.radius })
			cameraZMin = CampaignScaling.toWorldLength(clusterView.systems.values.minOf { it.position.vector.y - it.radius })
			cameraZMax = CampaignScaling.toWorldLength(clusterView.systems.values.maxOf { it.position.vector.y + it.radius })
		})
		
		window.addEventListener("resize", {
			camera.aspect = window.aspectRatio
			camera.updateProjectionMatrix()
			
			renderer.setSize(window.innerWidth, window.innerHeight)
		})
		
		renderer.render(scene, camera)
		
		CampaignUI.initCampaignUI(uiResponder(clusterView, scene))
		
		CampaignUI.renderCampaignUI(cameraControls)
		CampaignUI.fitLabels()
		
		addSelectionHandler(clusterView, camera, scene)
		
		delay(500L)
		
		FlowCollector { dt ->
			cameraControls.update(dt)
			renderer.render(scene, camera)
			CampaignUI.renderCampaignUI(cameraControls)
			
			scene.traverse { obj3d ->
				obj3d.celestialObjectRenderImmediate?.let { ptr ->
					ptr.resolve(clusterView)?.let { celestialObject ->
						CampaignScaling.toWorldRotation(dt * celestialObject.rotationSpeed, obj3d)
					}
				}
			}
		}
	}.display()
	
	coroutineScope {
		launch { deltaTimeFlow.collect(updateLoop) }
		launch { CampaignUI.selectionUpdate() }
	}
}
