@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package externals.threejs

import org.w3c.dom.events.EventTarget

external interface BattleCameraControlsSettings {
	var panSpeed: Number?
	
	var zoomSpeed: Number?
	
	var rotationSpeed: Number?
	
	var minZoom: Number?
	var maxZoom: Number?
	
	var rotationTop: Number?
	var rotationBottom: Number?
	
	var cameraXBound: Number?
	var cameraZBound: Number?
	
	var rotateMouseButton: Int?
	var panForwardsKey: String?
	var panLeftwardsKey: String?
	var panBackwardsKey: String?
	var panRightwardsKey: String?
	
	var domElement: EventTarget?
	var keyDomElement: EventTarget?
}

external open class BattleCameraControls(camera: Camera, settings: BattleCameraControlsSettings) {
	open fun update(dt: Double)
	open fun dispose()
	
	open val camera: Camera
	open val cameraParent: Group
	
	open var panSpeed: Number
	open var zoomSpeed: Number
	open var rotationSpeed: Number
	open var minZoom: Number
	open var maxZoom: Number
	open var rotationTop: Number
	open var rotationBottom: Number
	open var cameraXBound: Number
	open var cameraZBound: Number
	open var rotateMouseButton: Int
	open var panForwardsKey: String
	open var panLeftwardsKey: String
	open var panBackwardsKey: String
	open var panRightwardsKey: String
	
	open var domElement: EventTarget
	open var keyDomElement: EventTarget
	
	open var verticalRotation: Double
	open var horizontalRotation: Double
}
