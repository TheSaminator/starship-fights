package starshipfights.data.admiralty

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.eq
import starshipfights.data.*
import starshipfights.data.auth.User
import starshipfights.game.*
import java.time.Instant

@Serializable
data class Admiral(
	@SerialName("_id")
	override val id: Id<Admiral> = Id(),
	
	val owningUser: Id<User>,
	
	val name: String,
	val isFemale: Boolean,
	
	val faction: Faction,
	val rank: AdmiralRank,
) : DataDocument<Admiral> {
	val fullName: String
		get() = "${rank.getDisplayName(faction)} $name"
	
	companion object Table : DocumentTable<Admiral> by DocumentTable.create({
		index(Admiral::owningUser)
	})
}

@Serializable
sealed class DrydockStatus {
	@Serializable
	object Ready : DrydockStatus()
	
	@Serializable
	data class InRepair(val until: @Contextual Instant) : DrydockStatus()
}

@Serializable
data class ShipInDrydock(
	@SerialName("_id")
	override val id: Id<ShipInDrydock> = Id(),
	val name: String,
	val shipType: ShipType,
	val status: DrydockStatus,
	val owningAdmiral: Id<Admiral>
) : DataDocument<ShipInDrydock> {
	val isReady: Boolean
		get() = status == DrydockStatus.Ready
	
	val shipData: Ship
		get() = Ship(id.reinterpret(), name, shipType)
	
	companion object Table : DocumentTable<ShipInDrydock> by DocumentTable.create({
		index(ShipInDrydock::owningAdmiral)
	})
}

suspend fun getAllInGameAdmirals(user: User) = Admiral.select(Admiral::owningUser eq user.id).map { admiral ->
	InGameAdmiral(
		admiral.id.reinterpret(),
		InGameUser(user.id.reinterpret(), user.username),
		admiral.name,
		admiral.isFemale,
		admiral.faction,
		admiral.rank
	)
}.toList()

suspend fun getInGameAdmiral(admiralId: Id<InGameAdmiral>) = Admiral.get(admiralId.reinterpret())?.let { admiral ->
	User.get(admiral.owningUser)?.let { user ->
		InGameAdmiral(
			admiralId,
			InGameUser(user.id.reinterpret(), user.username),
			admiral.name,
			admiral.isFemale,
			admiral.faction,
			admiral.rank
		)
	}
}

suspend fun getAdmiralsShips(admiralId: Id<Admiral>) = ShipInDrydock
	.select(ShipInDrydock::owningAdmiral eq admiralId)
	.toList()
	.filter { it.isReady }
	.associate { it.shipData.id to it.shipData }

fun generateFleet(admiral: Admiral): List<ShipInDrydock> = ShipWeightClass.values()
	.flatMap {
		val shipTypes = ShipType.values().filter { st ->
			st.weightClass == it && st.faction == admiral.faction
		}.shuffled()
		
		if (shipTypes.isEmpty())
			emptyList()
		else
			(0..((admiral.rank.maxShipWeightClass.rank - it.rank) * 2 + 1).coerceAtLeast(0)).map { i ->
				shipTypes[i % shipTypes.size]
			}
	}
	.let { shipTypes ->
		val shipNames = mutableSetOf<String>()
		shipTypes.map {
			ShipInDrydock(
				id = Id(),
				name = newShipName(it.faction, it.weightClass, shipNames),
				shipType = it,
				status = DrydockStatus.Ready,
				owningAdmiral = admiral.id
			)
		}
	}
