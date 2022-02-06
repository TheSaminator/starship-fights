package starshipfights.game

import externals.threejs.*
import kotlinx.coroutines.await

private val mtlLoader: MTLLoader
	get() = MTLLoader()
		.setPath("/static/game/meshes/")
		.setResourcePath("/static/game/meshes/")
		.unsafeCast<MTLLoader>()

private val meshLoader: OBJLoader
	get() = OBJLoader()
		.setPath("/static/game/meshes/")
		.setResourcePath("/static/game/meshes/")
		.unsafeCast<OBJLoader>()

suspend fun loadModel(name: String): Mesh {
	val mtl = mtlLoader.loadAsync("$name.mtl").await()
	mtl.preload()
	
	return meshLoader.setMaterials(mtl).loadAsync("$name.obj").await()
		.children.single { it.type == "Mesh" }.unsafeCast<Mesh>().apply { removeFromParent() }
}

private val textureLoader: TextureLoader
	get() = TextureLoader()
		.setPath("/static/game/textures/")
		.setResourcePath("/static/game/textures/")
		.unsafeCast<TextureLoader>()

suspend fun loadTexture(name: String): Texture = textureLoader.loadAsync("$name.png").await()
