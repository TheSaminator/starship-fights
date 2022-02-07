window.addEventListener("load", function () {
	const canvases = document.getElementsByTagName("canvas");
	for (let canvas of canvases) {
		const modelName = canvas.getAttribute("data-model");
		if (modelName == null) continue;

		let threeData = {};

		threeData.camera = new THREE.PerspectiveCamera(69, 1, 0.01, 1000.0);

		threeData.scene = new THREE.Scene();
		threeData.scene.add(new THREE.AmbientLight("#FFFFFF", 0.35));

		threeData.renderer = new THREE.WebGLRenderer({"canvas": canvas, "antialias": true});

		threeData.controls = new THREE.OrbitControls(threeData.camera, canvas);

		function render() {
			threeData.controls.update();
			threeData.renderer.render(threeData.scene, threeData.camera);
			window.requestAnimationFrame(render);
		}

		window.addEventListener('resize', () => {
			const dim = canvas.getBoundingClientRect();
			threeData.camera.aspect = dim.width / dim.height;
			threeData.camera.updateProjectionMatrix();
			threeData.renderer.setSize(dim.width, dim.height);
		})

		window.requestAnimationFrame(async () => {
			const dim = canvas.getBoundingClientRect();
			threeData.camera.aspect = dim.width / dim.height;
			threeData.camera.updateProjectionMatrix();
			threeData.renderer.setSize(dim.width, dim.height);

			const mtlPath = modelName + ".mtl";
			const mtlLib = await (new THREE.MTLLoader()).setPath("/static/game/meshes/").setResourcePath("/static/game/meshes/").loadAsync(mtlPath);
			mtlLib.preload();

			const objPath = modelName + ".obj";
			const objMesh = await (new THREE.OBJLoader()).setPath("/static/game/meshes/").setResourcePath("/static/game/meshes/").setMaterials(mtlLib).loadAsync(objPath);
			objMesh.scale.setScalar(0.069);

			threeData.scene.add(objMesh);

			const bbox = new THREE.Box3().setFromObject(threeData.scene);
			bbox.dimensions = {
				x: bbox.max.x - bbox.min.x,
				y: bbox.max.y - bbox.min.y,
				z: bbox.max.z - bbox.min.z
			};
			objMesh.position.sub(new THREE.Vector3(bbox.min.x + bbox.dimensions.x / 2, bbox.min.y + bbox.dimensions.y / 2, bbox.min.z + bbox.dimensions.z / 2));

			threeData.camera.position.set(bbox.dimensions.x / 2, bbox.dimensions.y / 2, Math.max(bbox.dimensions.x, bbox.dimensions.y, bbox.dimensions.z));

			threeData.light = new THREE.PointLight("#FFFFFF", 0.65);
			threeData.scene.add(threeData.camera);
			threeData.camera.add(threeData.light);
			threeData.light.position.set(0, 0, 0);

			render();
		});
	}
});

window.addEventListener("load", function () {
	const moments = document.getElementsByClassName("moment");
	for (let moment of moments) {
		let date = new Date(Number(moment.innerHTML.trim()));
		moment.innerHTML = date.toLocaleString();
		moment.attributes["style"] = "";
	}
});
