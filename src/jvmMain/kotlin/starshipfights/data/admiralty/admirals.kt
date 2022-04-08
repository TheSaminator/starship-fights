package starshipfights.data.admiralty

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.conversions.Bson
import org.litote.kmongo.*
import starshipfights.data.DataDocument
import starshipfights.data.DocumentTable
import starshipfights.data.Id
import starshipfights.data.auth.User
import starshipfights.data.invoke
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
	val acumen: Int,
	val money: Int,
) : DataDocument<Admiral> {
	val rank: AdmiralRank
		get() = AdmiralRank.fromAcumen(acumen)
	
	val fullName: String
		get() = "${rank.getDisplayName(faction)} $name"
	
	companion object Table : DocumentTable<Admiral> by DocumentTable.create({
		index(Admiral::owningUser)
	})
}

infix fun AdmiralRank.Companion.eq(rank: AdmiralRank): Bson = when (rank.ordinal) {
	0 -> Admiral::acumen lt AdmiralRank.values()[1].minAcumen
	AdmiralRank.values().size - 1 -> Admiral::acumen gte rank.minAcumen
	else -> and(
		Admiral::acumen gte rank.minAcumen,
		Admiral::acumen lt AdmiralRank.values()[rank.ordinal + 1].minAcumen
	)
}

@Serializable
data class ShipInDrydock(
	@SerialName("_id")
	override val id: Id<ShipInDrydock> = Id(),
	val name: String,
	val shipType: ShipType,
	val readyAt: @Contextual Instant,
	val owningAdmiral: Id<Admiral>
) : DataDocument<ShipInDrydock> {
	val shipData: Ship
		get() = Ship(id.reinterpret(), name, shipType)
	
	val fullName: String
		get() = shipData.fullName
	
	companion object Table : DocumentTable<ShipInDrydock> by DocumentTable.create({
		index(ShipInDrydock::owningAdmiral)
	})
}

suspend fun getAllInGameAdmirals(user: User) = Admiral.filter(Admiral::owningUser eq user.id).map { admiral ->
	InGameAdmiral(
		admiral.id.reinterpret(),
		InGameUser(user.id.reinterpret(), user.profileName),
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
			InGameUser(user.id.reinterpret(), user.profileName),
			admiral.name,
			admiral.isFemale,
			admiral.faction,
			admiral.rank
		)
	}
}

suspend fun getAdmiralsShips(admiralId: Id<Admiral>): Map<Id<Ship>, Ship> {
	val now = Instant.now()
	
	return ShipInDrydock
		.filter(and(ShipInDrydock::owningAdmiral eq admiralId, ShipInDrydock::readyAt lte now))
		.toList()
		.associate { it.shipData.id to it.shipData }
}

fun generateFleet(admiral: Admiral): List<ShipInDrydock> = ShipWeightClass.values()
	.flatMap { swc ->
		val shipTypes = ShipType.values().filter { st ->
			st.weightClass == swc && st.faction == admiral.faction
		}.shuffled()
		
		if (shipTypes.isEmpty())
			emptyList()
		else
			(0 until ((admiral.rank.maxShipWeightClass.rank - swc.rank + 1) * 2).coerceAtLeast(0)).map { i ->
				shipTypes[i % shipTypes.size]
			}
	}
	.let { shipTypes ->
		val now = Instant.now().minusMillis(100L)
		
		val shipNames = mutableSetOf<String>()
		shipTypes.mapNotNull { st ->
			newShipName(st.faction, st.weightClass, shipNames)?.let { name ->
				ShipInDrydock(
					id = Id(),
					name = name,
					shipType = st,
					readyAt = now,
					owningAdmiral = admiral.id
				)
			}
		}
	}
