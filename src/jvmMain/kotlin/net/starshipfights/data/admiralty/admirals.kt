package net.starshipfights.data.admiralty

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.starshipfights.data.DataDocument
import net.starshipfights.data.DocumentTable
import net.starshipfights.data.Id
import net.starshipfights.data.auth.User
import net.starshipfights.data.invoke
import net.starshipfights.game.*
import org.bson.conversions.Bson
import org.litote.kmongo.*
import java.time.Instant
import kotlin.random.Random

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

fun genAIName(faction: Faction, isFemale: Boolean) = AdmiralNames.randomName(AdmiralNameFlavor.forFaction(faction).random(), isFemale)

fun genAI(faction: Faction, forBattleSize: BattleSize): Admiral {
	val isFemale = Random.nextBoolean()
	
	return Admiral(
		id = Id("advanced_robotical_admiral"),
		owningUser = Id("fake_player_actually_an_AI"),
		name = genAIName(faction, isFemale),
		isFemale = isFemale,
		faction = faction,
		acumen = AdmiralRank.values().first {
			it.maxBattleSize >= forBattleSize
		}.minAcumen + 500,
		money = 0
	)
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
	val shipFlavor: FactionFlavor = FactionFlavor.defaultForFaction(shipType.faction),
	val readyAt: @Contextual Instant,
	val owningAdmiral: Id<Admiral>
) : DataDocument<ShipInDrydock> {
	val shipData: Ship
		get() = Ship(id.reinterpret(), name, shipType, shipFlavor)
	
	val fullName: String
		get() = shipData.fullName
	
	companion object Table : DocumentTable<ShipInDrydock> by DocumentTable.create({
		index(ShipInDrydock::owningAdmiral)
	})
}

@Serializable
data class ShipMemorial(
	@SerialName("_id")
	override val id: Id<ShipMemorial> = Id(),
	val name: String,
	val shipType: ShipType,
	val destroyedAt: @Contextual Instant,
	val owningAdmiral: Id<Admiral>,
	val destroyedIn: Id<BattleRecord>,
) : DataDocument<ShipMemorial> {
	val fullName: String
		get() = "${shipType.faction.shipPrefix}$name"
	
	companion object Table : DocumentTable<ShipMemorial> by DocumentTable.create({
		index(ShipMemorial::owningAdmiral)
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

fun generateFleet(admiral: Admiral, flavor: FactionFlavor = FactionFlavor.defaultForFaction(admiral.faction)): List<ShipInDrydock> = ShipWeightClass.values()
	.flatMap { swc ->
		val shipTypes = ShipType.values().filter { st ->
			st.weightClass == swc && st.faction == admiral.faction
		}.shuffled()
		
		if (shipTypes.isEmpty())
			emptyList()
		else
			(0 until ((admiral.rank.maxShipTier.ordinal - swc.tier.ordinal + 1) * 2).coerceAtLeast(0)).map { i ->
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
					shipFlavor = flavor,
					readyAt = now,
					owningAdmiral = admiral.id
				)
			}
		}
	}
