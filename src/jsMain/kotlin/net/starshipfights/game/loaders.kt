package net.starshipfights.game

import com.juul.indexeddb.Database
import com.juul.indexeddb.Key
import com.juul.indexeddb.KeyPath
import com.juul.indexeddb.openDatabase
import externals.threejs.*
import kotlinx.browser.window
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlin.js.Date

const val MESH_PATH = "/static/game/meshes/"

private val fileLoader: FileLoader
	get() = FileLoader()
		.setPath(MESH_PATH)
		.setResourcePath(MESH_PATH)
		.unsafeCast<FileLoader>()

private val mtlLoader: MTLLoader
	get() = MTLLoader()
		.setPath(MESH_PATH)
		.setResourcePath(MESH_PATH)
		.unsafeCast<MTLLoader>()

private val meshLoader: OBJLoader
	get() = OBJLoader()
		.setPath(MESH_PATH)
		.setResourcePath(MESH_PATH)
		.unsafeCast<OBJLoader>()

suspend fun loadModel(name: String): Mesh {
	val (mtlText, objText) = coroutineScope {
		val mtlAsync = async { loadCacheEntry("mtl", name, ::cacheMissHandler).content!! }
		val objAsync = async { loadCacheEntry("obj", name, ::cacheMissHandler).content!! }
		
		mtlAsync.await() to objAsync.await()
	}
	val mtl = mtlLoader.parse(mtlText, MESH_PATH)
	mtl.preload()
	
	return meshLoader
		.setMaterials(mtl)
		.parse(objText)
		.children
		.single { it.type == "Mesh" }
		.unsafeCast<Mesh>()
		.apply { removeFromParent() }
}

private val textureLoader: TextureLoader
	get() = TextureLoader()
		.setPath("/static/game/textures/")
		.setResourcePath("/static/game/textures/")
		.unsafeCast<TextureLoader>()

suspend fun loadTexture(name: String): Texture = textureLoader.loadAsync("$name.png").await()

private var database: Database? = null
private var cacheTime: Double = 0.0

private external interface CacheEntry {
	var name: String?
	var content: String?
	var updated: Double?
}

private suspend fun cacheMissHandler(collection: String, name: String) = configure<CacheEntry> {
	this.name = name
	this.content = fileLoader.loadAsync("$name.$collection").await().unsafeCast<String>()
	this.updated = Date.now()
}

private suspend fun loadCacheEntry(collection: String, name: String, onMiss: suspend (String, String) -> CacheEntry): CacheEntry {
	return database?.let { db ->
		db.transaction(collection) {
			objectStore(collection).getAll(Key(name)).singleOrNull()?.unsafeCast<CacheEntry?>()
		}?.takeIf { entry ->
			entry.updated?.let { updated -> updated >= cacheTime } ?: false
		} ?: onMiss(collection, name).also { entry ->
			db.writeTransaction(collection) {
				objectStore(collection).put(entry)
			}
		}
	} ?: onMiss(collection, name)
}

suspend fun initResCache() {
	cacheTime = window.fetch("/cache-time").await().text().await().toDouble()
	
	database = try {
		openDatabase("resource-cache", 1) { database, oldVersion, newVersion ->
			if (oldVersion < 1) {
				database.createObjectStore("obj", KeyPath("name"))
				database.createObjectStore("mtl", KeyPath("name"))
			}
		}
	} catch (ex: Throwable) {
		ex.printStackTrace()
		null
	} catch (ex: dynamic) {
		console.error(ex)
		null
	}
}
