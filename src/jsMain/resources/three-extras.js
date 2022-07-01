class BattleCameraControls {
	constructor(camera, settings) {
		this.panSpeed = settings.panSpeed || 25;

		this.zoomSpeed = settings.zoomSpeed || 7.5;

		this.rotationSpeed = settings.rotationSpeed || 0.005;

		this.minZoom = settings.minZoom || 9;
		this.maxZoom = settings.maxZoom || 36;

		this.rotationTop = settings.rotationTop || (Math.PI * 0.499);
		this.rotationBottom = settings.rotationBottom || (Math.PI * 0.001);

		this.cameraXBound = settings.cameraXBound || 1000000;
		this.cameraZBound = settings.cameraZBound || 1000000;

		this.rotateMouseButton = settings.rotateMouseButton || 1;
		this.panForwardsKey = settings.panForwardsKey || "KeyW";
		this.panLeftwardsKey = settings.panLeftwardsKey || "KeyA";
		this.panBackwardsKey = settings.panBackwardsKey || "KeyS";
		this.panRightwardsKey = settings.panRightwardsKey || "KeyD";

		this._panKeys = [
			this.panForwardsKey,
			this.panLeftwardsKey,
			this.panBackwardsKey,
			this.panRightwardsKey,
		];
		this._panKeyStates = {};

		const cameraParent = new THREE.Group();
		camera.getWorldPosition(cameraParent.position);

		const quaternion = camera.getWorldQuaternion(new THREE.Quaternion());
		cameraParent.setRotationFromQuaternion(quaternion);

		const eulerAngles = new THREE.Euler(0, 0, 0, "ZXY");
		eulerAngles.setFromQuaternion(quaternion, "ZXY");
		this.verticalRotation = eulerAngles.x;
		this.horizontalRotation = eulerAngles.y;

		const scene = camera.parent;
		camera.removeFromParent();

		cameraParent.add(camera);
		scene.add(cameraParent);

		camera.position.set(0, 0, 0);
		camera.setRotationFromQuaternion(new THREE.Quaternion());

		this.camera = camera;
		this.cameraParent = cameraParent;

		this.camera.rotateY(Math.PI);

		this.domElement = settings.domElement;
		this.keyDomElement = settings.keyDomElement;
		const scope = this;

		this._handler$mousedown = e => {
			if (e.button === scope.rotateMouseButton)
				scope._isRotating = true;
		};
		this.domElement.addEventListener("mousedown", this._handler$mousedown);

		this._handler$mouseup = e => {
			if (e.button === scope.rotateMouseButton)
				scope._isRotating = false;
		};
		this.domElement.addEventListener("mouseup", this._handler$mouseup);

		this._handler$mousemove = e => {
			scope._currX = e.screenX;
			scope._currY = e.screenY;
		};
		this.domElement.addEventListener("mousemove", this._handler$mousemove);

		this._handler$wheel = e => {
			scope._wDelta = scope._wDelta || 0;
			scope._wDelta -= e.deltaY * 0.001;
		};
		this.domElement.addEventListener("wheel", this._handler$wheel);

		this._keyHandler$keydown = e => {
			if (scope._panKeys.includes(e.code)) {
				scope._panKeyStates[e.code] = true;
			}
		};
		this.keyDomElement.addEventListener("keydown", this._keyHandler$keydown);

		this._keyHandler$keyup = e => {
			if (scope._panKeys.includes(e.code)) {
				scope._panKeyStates[e.code] = false;
			}
		};
		this.keyDomElement.addEventListener("keyup", this._keyHandler$keyup);

		this._handler$contextmenu = e => e.preventDefault();
		this.domElement.addEventListener("contextmenu", this._handler$contextmenu);

		this._vector3$up = new THREE.Vector3(0, 1, 0);
		this._vector3$right = new THREE.Vector3(1, 0, 0);
		this._vector3$pan = new THREE.Vector3();
		this._vector3$pos = new THREE.Vector3();

		this._quaternion$0 = new THREE.Quaternion();
		this._quaternion$1 = new THREE.Quaternion();
	}

	update(dt) {
		// Rotate section

		if (this._isRotating) {
			const x1 = this._currX || 0;
			const x0 = this._prevX || x1;
			const y1 = this._currY || 0;
			const y0 = this._prevY || y1;

			const dx = x1 - x0;
			const dy = y1 - y0;

			this.verticalRotation += dy * this.rotationSpeed;
			this.horizontalRotation += dx * -this.rotationSpeed;
		}

		this._prevX = this._currX || 0;
		this._prevY = this._currY || 0;

		this.verticalRotation = Math.max(this.rotationBottom, Math.min(this.verticalRotation, this.rotationTop));

		this._quaternion$0.setFromAxisAngle(this._vector3$up, this.horizontalRotation);
		this._quaternion$1.setFromAxisAngle(this._vector3$right, this.verticalRotation);
		this._quaternion$0.multiply(this._quaternion$1);

		this.cameraParent.setRotationFromQuaternion(this._quaternion$0);

		// Zoom section

		let newZ = -this.camera.position.z - (this._wDelta || 0) * this.zoomSpeed;
		if (newZ > this.maxZoom) newZ = this.maxZoom;
		if (newZ < this.minZoom) newZ = this.minZoom;
		this.camera.position.z = -newZ;

		this._wDelta = 0;

		// Pan section

		let zDelta = 0;
		let xDelta = 0;

		if (this._panKeyStates[this.panForwardsKey])
			zDelta += 1;

		if (this._panKeyStates[this.panLeftwardsKey])
			xDelta += 1;

		if (this._panKeyStates[this.panBackwardsKey])
			zDelta -= 1;

		if (this._panKeyStates[this.panRightwardsKey])
			xDelta -= 1;

		const panMultiplierTime = dt * this.panSpeed * newZ / this.maxZoom;

		this._vector3$pos.copy(this.cameraParent.position);

		this._vector3$pan.set(xDelta, 0, zDelta);
		this._vector3$pan.normalize();
		this._vector3$pan.multiplyScalar(panMultiplierTime);
		this._vector3$pan.applyAxisAngle(this._vector3$up, this.horizontalRotation);

		this._vector3$pos.add(this._vector3$pan);
		if (Math.abs(this._vector3$pos.x) > this.cameraXBound) {
			this._vector3$pos.x = Math.sign(this._vector3$pos.x) * this.cameraXBound;
		}
		if (Math.abs(this._vector3$pos.z) > this.cameraZBound) {
			this._vector3$pos.z = Math.sign(this._vector3$pos.z) * this.cameraZBound;
		}

		this.cameraParent.position.copy(this._vector3$pos);

		// Misc work
		this.camera.updateMatrixWorld();
	}

	dispose() {
		this.domElement.removeEventListener("mousedown", this._handler$mousedown);
		this.domElement.removeEventListener("mouseup", this._handler$mouseup);
		this.domElement.removeEventListener("mousemove", this._handler$mousemove);
		this.domElement.removeEventListener("wheel", this._handler$wheel);

		this.keyDomElement.removeEventListener("keydown", this._keyHandler$keydown);
		this.keyDomElement.removeEventListener("keyup", this._keyHandler$keyup);

		this.domElement.removeEventListener("contextmenu", this._handler$contextmenu);

		this.camera.removeFromParent();
		const cameraParentParent = this.cameraParent.parent;
		cameraParentParent.attach(this.camera);
		this.cameraParent.removeFromParent();
	}
}

class CampaignCameraControls {
	constructor(camera, settings) {
		this.panSpeed = settings.panSpeed || 25;

		this.zoomSpeed = settings.zoomSpeed || 7.5;

		this.minZoom = settings.minZoom || 9;
		this.maxZoom = settings.maxZoom || 36;

		this.cameraXMin = settings.cameraXMin || -1000000;
		this.cameraXMax = settings.cameraXMax || 1000000;
		this.cameraZMin = settings.cameraZMin || -1000000;
		this.cameraZMax = settings.cameraZMax || 1000000;

		this.panForwardsKey = settings.panForwardsKey || "KeyW";
		this.panLeftwardsKey = settings.panLeftwardsKey || "KeyA";
		this.panBackwardsKey = settings.panBackwardsKey || "KeyS";
		this.panRightwardsKey = settings.panRightwardsKey || "KeyD";

		this._panKeys = [
			this.panForwardsKey,
			this.panLeftwardsKey,
			this.panBackwardsKey,
			this.panRightwardsKey,
		];
		this._panKeyStates = {};

		const cameraParent = new THREE.Group();
		camera.getWorldPosition(cameraParent.position);

		const eulerAngles = new THREE.Euler(Math.PI / 5, 0, 0, "ZXY");

		const scene = camera.parent;
		camera.removeFromParent();

		cameraParent.add(camera);
		scene.add(cameraParent);

		camera.position.set(0, 0, -(this.minZoom + this.maxZoom) / 2);
		cameraParent.setRotationFromEuler(eulerAngles);

		this.camera = camera;
		this.cameraParent = cameraParent;

		this.camera.rotateY(Math.PI);

		this.domElement = settings.domElement;
		this.keyDomElement = settings.keyDomElement;
		const scope = this;

		this._handler$wheel = e => {
			scope._wDelta = scope._wDelta || 0;
			scope._wDelta -= e.deltaY * 0.001;
		};
		this.domElement.addEventListener("wheel", this._handler$wheel);

		this._keyHandler$keydown = e => {
			if (scope._panKeys.includes(e.code)) {
				scope._panKeyStates[e.code] = true;
			}
		};
		this.keyDomElement.addEventListener("keydown", this._keyHandler$keydown);

		this._keyHandler$keyup = e => {
			if (scope._panKeys.includes(e.code)) {
				scope._panKeyStates[e.code] = false;
			}
		};
		this.keyDomElement.addEventListener("keyup", this._keyHandler$keyup);

		this._handler$contextmenu = e => e.preventDefault();
		this.domElement.addEventListener("contextmenu", this._handler$contextmenu);

		this._vector3$up = new THREE.Vector3(0, 1, 0);
		this._vector3$pan = new THREE.Vector3();
		this._vector3$pos = new THREE.Vector3();
	}

	update(dt) {
		// Zoom section

		let newZ = -this.camera.position.z - (this._wDelta || 0) * this.zoomSpeed;
		if (newZ > this.maxZoom) newZ = this.maxZoom;
		if (newZ < this.minZoom) newZ = this.minZoom;
		this.camera.position.z = -newZ;

		this._wDelta = 0;

		// Pan section

		let zDelta = 0;
		let xDelta = 0;

		if (this._panKeyStates[this.panForwardsKey])
			zDelta += 1;

		if (this._panKeyStates[this.panLeftwardsKey])
			xDelta += 1;

		if (this._panKeyStates[this.panBackwardsKey])
			zDelta -= 1;

		if (this._panKeyStates[this.panRightwardsKey])
			xDelta -= 1;

		const panMultiplierTime = dt * this.panSpeed * Math.sqrt(newZ / this.maxZoom);

		this._vector3$pos.copy(this.cameraParent.position);

		this._vector3$pan.set(xDelta, 0, zDelta);
		this._vector3$pan.normalize();
		this._vector3$pan.multiplyScalar(panMultiplierTime);

		this._vector3$pos.add(this._vector3$pan);
		if (this._vector3$pos.x < this.cameraXMin) {
			this._vector3$pos.x = this.cameraXMin;
		}
		if (this._vector3$pos.x > this.cameraXMax) {
			this._vector3$pos.x = this.cameraXMax;
		}
		if (this._vector3$pos.z < this.cameraZMin) {
			this._vector3$pos.z = this.cameraZMin;
		}
		if (this._vector3$pos.z > this.cameraZMax) {
			this._vector3$pos.z = this.cameraZMax;
		}

		this.cameraParent.position.copy(this._vector3$pos);

		// Misc work
		this.camera.updateMatrixWorld();
	}

	dispose() {
		this.domElement.removeEventListener("wheel", this._handler$wheel);

		this.keyDomElement.removeEventListener("keydown", this._keyHandler$keydown);
		this.keyDomElement.removeEventListener("keyup", this._keyHandler$keyup);

		this.domElement.removeEventListener("contextmenu", this._handler$contextmenu);

		this.camera.removeFromParent();
		const cameraParentParent = this.cameraParent.parent;
		cameraParentParent.attach(this.camera);
		this.cameraParent.removeFromParent();
	}
}
