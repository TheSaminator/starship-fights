@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.events.EventTarget

external interface CampaignCameraControlsSettings {
	var panSpeed: Number?
	
	var zoomSpeed: Number?
	
	var minZoom: Number?
	var maxZoom: Number?
	
	var cameraXMin: Number?
	var cameraZMin: Number?
	var cameraXMax: Number?
	var cameraZMax: Number?
	
	var panForwardsKey: String?
	var panLeftwardsKey: String?
	var panBackwardsKey: String?
	var panRightwardsKey: String?
	
	var domElement: EventTarget?
	var keyDomElement: EventTarget?
}

external open class CampaignCameraControls(camera: Camera, settings: CampaignCameraControlsSettings) {
	open fun update(dt: Double)
	open fun dispose()
	
	open val camera: Camera
	open val cameraParent: Group
	
	open var panSpeed: Number
	open var zoomSpeed: Number
	open var minZoom: Number
	open var maxZoom: Number
	open var cameraXMin: Number
	open var cameraZMin: Number
	open var cameraXMax: Number
	open var cameraZMax: Number
	open var panForwardsKey: String
	open var panLeftwardsKey: String
	open var panBackwardsKey: String
	open var panRightwardsKey: String
	
	open var domElement: EventTarget
	open var keyDomElement: EventTarget
	
	open var verticalRotation: Double
	open var horizontalRotation: Double
}
