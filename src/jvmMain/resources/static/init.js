(function () {
	function animationFrame() {
		return new Promise(resolve => {
			window.requestAnimationFrame(resolve);
		});
	}

	function loadScript(scriptLoc) {
		return new Promise(resolve => {
			const scriptElem = document.createElement("script");
			scriptElem.async = true;
			scriptElem.src = scriptLoc;
			scriptElem.addEventListener("load", () => resolve());

			document.body.append(scriptElem);
		});
	}

	window.addEventListener("load", async function () {
		// Load and render OBJ meshes
		if (!window.sfShipMeshViewer) return;

		const canvases = document.getElementsByTagName("canvas");
		if (canvases.length < 1) return;

		for (const scriptLoc of [
			"/static/game/three.js",
			"/static/game/three-examples.js",
			"/static/game/three-extras.js",
		]) {
			await loadScript(scriptLoc);
		}

		const canvasLoads = [];
		for (const canvas of canvases) {
			const modelName = canvas.getAttribute("data-model");
			if (modelName == null) continue;

			const p = (async () => {
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
					threeData.renderer.setSize(dim.width, dim.height, false);
				})

				await animationFrame();

				const dim = canvas.getBoundingClientRect();
				threeData.camera.aspect = dim.width / dim.height;
				threeData.camera.updateProjectionMatrix();
				threeData.renderer.setSize(dim.width, dim.height, false);

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
			})();
			canvasLoads.push(p);
		}

		for (const p of canvasLoads) {
			await p;
		}
	});

	window.addEventListener("load", function () {
		// Localize dates and times
		const moments = document.getElementsByClassName("moment");
		for (const moment of moments) {
			let date = new Date(Number(moment.innerHTML.trim()));
			moment.innerHTML = date.toLocaleString();
			moment.style.display = "inline";
		}
	});

	window.addEventListener("load", function () {
		// Enforce female-only Felinae Felices faction
		if (!window.sfFactionSelect) return;

		const factionInputs = document.getElementsByName("faction");
		const sexInputs = document.getElementsByName("sex");
		for (const factionInput of factionInputs) {
			factionInput.addEventListener("click", () => {
				if (factionInput.hasAttribute("data-force-gender")) {
					const forceGender = factionInput.getAttribute("data-force-gender");
					for (const sexInput of sexInputs) {
						if (sexInput.value === forceGender) {
							sexInput.checked = true;
						} else {
							sexInput.disabled = true;
							sexInput.checked = false;
						}
					}
				} else {
					for (const sexInput of sexInputs) {
						sexInput.disabled = false;
					}
				}
			});
		}
	});

	window.addEventListener("load", function () {
		// Generate random admiral names
		if (!window.sfAdmiralNameGen) return;

		const nameBox = document.getElementById("name");
		const isFemaleButton = document.getElementById("sex-female");
		const generators = document.getElementsByClassName("generate-admiral-name");
		for (const generator of generators) {
			const flavor = generator.getAttribute("data-flavor");
			generator.onclick = (e) => {
				e.preventDefault();
				(async () => {
					nameBox.value = await (await fetch("/generate-name/" + flavor + "/" + (isFemaleButton.checked ? "female" : "male"))).text();
				})();
			};
		}
	});

	window.addEventListener("load", function () {
		// Indicate maximum and used length of <textarea>s
		const textareas = document.getElementsByTagName("textarea");
		for (const textarea of textareas) {
			if (!textarea.hasAttribute("maxLength")) continue;

			const maxLengthIndicator = document.createElement("p");
			maxLengthIndicator.style.fontSize = "0.8em";
			maxLengthIndicator.style.fontStyle = "italic";
			maxLengthIndicator.style.color = "#555";

			textarea.after(maxLengthIndicator);

			function updateIndicator() {
				const maxLengthAttr = textarea.getAttribute("maxLength");
				if (!maxLengthAttr) return;
				const maxLength = Number(maxLengthAttr);

				maxLengthIndicator.innerHTML = "" + textarea.value.length + "/" + maxLength;
			}

			textarea.addEventListener("input", () => {
				updateIndicator();
			});

			updateIndicator();
		}
	});

	window.addEventListener("load", function () {
		// Allow POSTing with <a>s
		const anchors = document.getElementsByTagName("a");
		for (const anchor of anchors) {
			const method = anchor.getAttribute("data-method");
			if (method == null) continue;

			anchor.onclick = e => {
				e.preventDefault();

				let form = document.createElement("form");
				form.style.display = "none";
				form.action = anchor.href;
				form.method = method;

				const csrfToken = anchor.getAttribute("data-csrf-token");
				if (csrfToken != null) {
					let csrfInput = document.createElement("input");
					csrfInput.name = "csrf-token";
					csrfInput.type = "hidden";
					csrfInput.value = csrfToken;
					form.append(csrfInput);
				}

				document.body.append(form);
				form.submit();
			};
		}
	});

	window.addEventListener("load", function () {
		// Preview themes
		if (!window.sfThemeChoice) return;

		const themeChoices = document.getElementsByName("theme");
		for (const themeChoice of themeChoices) {
			const theme = themeChoice.value;
			themeChoice.addEventListener("click", () => {
				document.documentElement.setAttribute("data-theme", theme);
			});
		}
	});

	window.addEventListener("load", function () {
		// Allow bulk-setting of factions
		if (!window.sfClusterGenTest) return;

		const setAllButtons = document.getElementsByClassName("set-all");
		for (const setAllButton of setAllButtons) {
			setAllButton.onclick = function (e) {
				e.preventDefault();

				const enableClass = setAllButton.getAttribute("data-enable-class");
				const factionChoices = document.getElementsByClassName("faction-choice");
				for (const factionChoice of factionChoices) {
					factionChoice.checked = factionChoice.classList.contains(enableClass);
				}
			};
		}

		const setSomeButtons = document.getElementsByClassName("set-all-by-faction");
		for (const setSomeButton of setSomeButtons) {
			setSomeButton.onclick = function (e) {
				e.preventDefault();

				const filterClass = setSomeButton.getAttribute("data-filter-class");
				const chosenClass = setSomeButton.getAttribute("data-enable-class");
				const factionChoices = document.getElementsByClassName("faction-choice");
				for (const factionChoice of factionChoices) {
					if (factionChoice.classList.contains(filterClass)) {
						factionChoice.checked = factionChoice.classList.contains(chosenClass);
					}
				}
			};
		}
	});
})();
