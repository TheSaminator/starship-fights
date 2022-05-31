package starshipfights.data

import com.mongodb.client.model.BulkWriteOptions
import com.mongodb.client.model.ReplaceOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.replaceOne
import org.litote.kmongo.serialization.IdController
import org.litote.kmongo.util.KMongoUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface DataDocument<T : DataDocument<T>> {
	@SerialName("_id")
	val id: Id<T>
}

object DocumentIdController : IdController {
	override fun findIdProperty(type: KClass<*>): KProperty1<*, *> {
		return DataDocument<*>::id
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun <T, R> getIdValue(idProperty: KProperty1<T, R>, instance: T): R? {
		return (instance as DataDocument<*>).id as R
	}
	
	override fun <T, R> setIdValue(idProperty: KProperty1<T, R>, instance: T) {
		throw UnsupportedOperationException("Cannot set `id` property of `DataDocument<T>`!")
	}
}

interface DocumentTable<T : DataDocument<T>> {
	fun initialize()
	
	suspend fun index(vararg properties: KProperty1<T, *>)
	suspend fun unique(vararg properties: KProperty1<T, *>)
	
	suspend fun put(doc: T)
	suspend fun put(docs: Iterable<T>)
	suspend fun set(id: Id<T>, set: Bson): Boolean
	suspend fun get(id: Id<T>): T?
	suspend fun del(id: Id<T>)
	suspend fun all(): Flow<T>
	
	suspend fun filter(where: Bson): Flow<T>
	suspend fun sorted(order: Bson): Flow<T>
	suspend fun select(where: Bson, order: Bson): Flow<T>
	suspend fun number(where: Bson): Long
	suspend fun locate(where: Bson): T?
	suspend fun update(where: Bson, set: Bson)
	suspend fun remove(where: Bson)
	
	companion object : CoroutineScope {
		private val logger: Logger by lazy {
			LoggerFactory.getLogger(DocumentTable::class.java)
		}
		
		override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { ctx, ex ->
			val name = ctx[CoroutineName]?.name?.let { "table $it" } ?: "unnamed table"
			logger.error("Caught unhandled exception from initializing $name!", ex)
		}
		
		fun <T : DataDocument<T>> create(kclass: KClass<T>, initFunc: suspend DocumentTable<T>.() -> Unit = {}): DocumentTable<T> = DocumentTableImpl(kclass) {
			runBlocking {
				it.initFunc()
			}
		}
		
		inline fun <reified T : DataDocument<T>> create(noinline initFunc: suspend DocumentTable<T>.() -> Unit = {}) = create(T::class, initFunc)
	}
}

private class DocumentTableImpl<T : DataDocument<T>>(val kclass: KClass<T>, private val initFunc: (DocumentTable<T>) -> Unit) : DocumentTable<T> {
	suspend fun collection() = ConnectionHolder.getDatabase().database.getCollection(kclass.simpleName, kclass.java).coroutine
	
	override fun initialize() {
		initFunc(this)
	}
	
	override suspend fun index(vararg properties: KProperty1<T, *>) {
		collection().ensureIndex(*properties)
	}
	
	override suspend fun unique(vararg properties: KProperty1<T, *>) {
		collection().ensureUniqueIndex(*properties)
	}
	
	override suspend fun put(doc: T) {
		collection().replaceOneById(doc.id, doc, ReplaceOptions().upsert(true))
	}
	
	override suspend fun put(docs: Iterable<T>) {
		collection().bulkWrite(
			docs.map { doc ->
				replaceOne(KMongoUtil.idFilterQuery(doc.id), doc, ReplaceOptions().upsert(true))
			},
			BulkWriteOptions().ordered(false)
		)
	}
	
	override suspend fun set(id: Id<T>, set: Bson): Boolean {
		return collection().updateOneById(id, set).matchedCount != 0L
	}
	
	override suspend fun get(id: Id<T>): T? {
		return collection().findOneById(id)
	}
	
	override suspend fun del(id: Id<T>) {
		collection().deleteOneById(id)
	}
	
	override suspend fun all(): Flow<T> {
		return collection().find().toFlow()
	}
	
	override suspend fun filter(where: Bson): Flow<T> {
		return collection().find(where).toFlow()
	}
	
	override suspend fun sorted(order: Bson): Flow<T> {
		return collection().find().sort(order).toFlow()
	}
	
	override suspend fun select(where: Bson, order: Bson): Flow<T> {
		return collection().find(where).sort(order).toFlow()
	}
	
	override suspend fun number(where: Bson): Long {
		return collection().countDocuments(where)
	}
	
	override suspend fun locate(where: Bson): T? {
		return collection().findOne(where)
	}
	
	override suspend fun update(where: Bson, set: Bson) {
		collection().updateMany(where, set)
	}
	
	override suspend fun remove(where: Bson) {
		collection().deleteMany(where)
	}
}
