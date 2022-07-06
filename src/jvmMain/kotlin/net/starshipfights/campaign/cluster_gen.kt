package net.starshipfights.campaign

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.starshipfights.data.Id
import net.starshipfights.data.invoke
import net.starshipfights.data.space.newStarName
import net.starshipfights.data.space.toRomanNumerals
import net.starshipfights.game.*
import net.starshipfights.game.ai.mean
import net.starshipfights.game.ai.random
import net.starshipfights.game.ai.weightedRandom
import kotlin.math.*
import kotlin.random.Random

class ClusterGenerator(val settings: ClusterGenerationSettings) {
	var throttle: suspend () -> Unit = ::`yield`
	
	suspend fun generateCluster(): StarClusterView {
		return withTimeoutOrNull(10_000L) {
			val positionsAsync = async {
				val rp = fixPositions(generatePositions().take(settings.size.maxStars).toList())
				val p = indexPositions(rp)
				p to fixWarpLanes(p, generateWarpLanes(p))
			}
			
			val starSystemsAsync = async {
				val systems = createStarSystems().filter {
					(0..it.numHabitableWorlds).random() > 0
				}.take(settings.size.maxStars).toList()
				
				val numCorruptSystems = settings.corruption.getNumCorruptedStars(settings.size)
				
				(systems.take(numCorruptSystems).map {
					corruptStarSystem(it)
				} + systems.drop(numCorruptSystems)).shuffled()
			}
			
			val (positions, warpLanes) = positionsAsync.await()
			val unplacedStarSystems = starSystemsAsync.await()
			
			val systems = (positions.toList() zip unplacedStarSystems).associate { (idAndPos, data) ->
				val (id, pos) = idAndPos
				id to data.place(pos)
			}
			
			StarClusterView(
				background = settings.background,
				systems = generateFleets(assignFactions(systems, warpLanes)),
				lanes = warpLanes,
			)
		} ?: generateCluster()
	}
	
	private fun generatePositions() = flow {
		val initial = Vec2(0.0, 0.0)
		val samples = mutableSetOf(initial)
		emit(initial)
		
		val activeSet = mutableSetOf(initial)
		while (activeSet.isNotEmpty()) {
			val aroundPoint = activeSet.random()
			val newPoints = (1..SYSTEM_K).map { randomPositionInAnnulus(SYSTEM_R, SYSTEM_R * 2) + aroundPoint }
			
			var isInactive = true
			for (newPoint in newPoints) {
				throttle()
				
				if (samples.any { (it - newPoint).magnitude < SYSTEM_R })
					continue
				
				samples.add(newPoint)
				emit(newPoint)
				
				activeSet.add(newPoint)
				isInactive = false
			}
			
			if (isInactive)
				activeSet -= aroundPoint
		}
	}
	
	private suspend fun fixPositions(positions: List<Vec2>): List<Vec2> {
		val average = positions.mean()
		
		return positions.map { pos ->
			throttle()
			pos - average
		}
	}
	
	private suspend fun indexPositions(positions: List<Vec2>): Map<Id<StarSystem>, Vec2> {
		return positions.associateBy {
			throttle()
			Id()
		}
	}
	
	private fun randomPositionInAnnulus(inner: Double, outer: Double): Vec2 {
		val theta = (0.0..(2 * PI)).random()
		val r = sqrt((inner.pow(2)..outer.pow(2)).random())
		return polarVector(r, theta)
	}
	
	private suspend fun generateWarpLanes(positions: Map<Id<StarSystem>, Vec2>): Set<WarpLane> {
		val allSystems = positions.keys
		
		val maxDistance = positions.values.maxOf { it.magnitude } / settings.size.maxHyperlaneDistanceFactor
		
		val warpLanes = mutableMapOf<WarpLane, LineSegment>()
		val activeSystems = mutableSetOf(allSystems.random())
		val detachedSystems = (allSystems - activeSystems).toMutableSet()
		while (detachedSystems.isNotEmpty()) {
			val hubSystem = activeSystems.randomOrNull() ?: detachedSystems.random()
			val newLanes = allSystems.mapNotNull { nextSystem ->
				throttle()
				
				if (hubSystem == nextSystem)
					return@mapNotNull null
				
				val warpLane = WarpLane(hubSystem, nextSystem)
				if (warpLane in warpLanes)
					return@mapNotNull null
				
				val segment = LineSegment(positions.getValue(hubSystem), positions.getValue(nextSystem))
				if (segment.length > maxDistance && (1..50).random() != 1)
					return@mapNotNull null
				if (segment.length > maxDistance * 2)
					return@mapNotNull null
				if (warpLanes.any { (l, s) -> warpLane !in l && s in segment })
					return@mapNotNull null
				if (allSystems.any { it !in warpLane && segment.intersectsCircle(positions.getValue(it), MAX_SYSTEM_SIZE) })
					return@mapNotNull null
				
				warpLane to segment
			}.toMap()
			
			warpLanes += newLanes.filter { Random.nextBoolean() }
			
			val newActiveSystems = warpLanes.keys.map { (_, it) -> it }.toSet()
			activeSystems += newActiveSystems
			detachedSystems -= newActiveSystems
			
			if (warpLanes.any { (it, _) -> hubSystem in it })
				activeSystems -= hubSystem
		}
		
		val allLanes = warpLanes.keys
		val removedLanes = mutableSetOf<WarpLane>()
		for (lane in allLanes) {
			throttle()
			
			val remainingLanes = allLanes - removedLanes
			if (remainingLanes.count { it in lane } < 5)
				continue
			
			if (Random.nextDouble() < settings.laneDensity.chanceToRemove)
				removedLanes += lane
		}
		return allLanes - removedLanes
	}
	
	private suspend fun fixWarpLanes(positions: Map<Id<StarSystem>, Vec2>, warpLanes: Set<WarpLane>): Set<WarpLane> {
		val nodeStack = mutableListOf(positions.keys.random())
		val discoveredSystems = mutableSetOf<Id<StarSystem>>()
		
		while (nodeStack.isNotEmpty()) {
			throttle()
			
			val node = nodeStack.removeAt(0)
			if (node !in discoveredSystems) {
				discoveredSystems += node
				for (lane in warpLanes) {
					throttle()
					
					if (node == lane.systemA)
						nodeStack.add(0, lane.systemB)
					if (node == lane.systemB)
						nodeStack.add(0, lane.systemA)
				}
			}
		}
		
		val detachedSystems = positions.keys - discoveredSystems
		if (detachedSystems.isEmpty())
			return warpLanes
		
		val lanesWithSegments = warpLanes.map { lane ->
			val (aId, bId) = lane
			lane to LineSegment(positions.getValue(aId), positions.getValue(bId))
		}
		
		val possibleLanes = discoveredSystems.map { a ->
			val aPos = positions.getValue(a)
			val (b, bPos) = detachedSystems.map { b ->
				throttle()
				
				b to positions.getValue(b)
			}.minByOrNull { (_, bPos) ->
				(aPos - bPos).magnitude
			}!!
			WarpLane(a, b) to LineSegment(aPos, bPos)
		}.filter { (warpLane, segment) ->
			lanesWithSegments.none { (l, s) ->
				warpLane !in l && s in segment
			} && positions.none { (id, pos) ->
				id !in warpLane && segment.intersectsCircle(pos, MAX_SYSTEM_SIZE)
			}
		}.sortedBy { (_, it) -> it.length }.map { (it, _) -> it }
		
		return fixWarpLanes(positions, warpLanes + possibleLanes.take(1))
	}
	
	private fun createStarSystems() = flow {
		val usedNames = mutableSetOf<String>()
		
		while (currentCoroutineContext().isActive) {
			val name = newStarName(usedNames) ?: break
			
			val unnamedCelestialObjects = createCelestialObjects()
				.takeWhile { it.position.vector.magnitude + it.size + SYSTEM_MARGIN < MAX_SYSTEM_SIZE }
				.toList()
			
			val celestialObjects = UnnamedCelestialObject.giveNamesTo(name, unnamedCelestialObjects)
			val radius = celestialObjects.maxOf { it.position.vector.magnitude + it.size } + SYSTEM_MARGIN
			
			emit(
				UnplacedStarSystem(
					name = name,
					radius = radius,
					bodies = celestialObjects
				)
			)
		}
	}
	
	private fun createCelestialObjects() = flow {
		val usedPositions = mutableSetOf<Vec2>()
		
		val stars = if ((1..3).random() == 1) {
			// Unary star system
			
			val starVec2 = Vec2(0.0, 0.0)
			usedPositions += starVec2
			
			val starType = randomStarType()
			val starSize = starType.sizeRange.random()
			val star = UnnamedCelestialObject.Star(
				position = Position(starVec2),
				size = starSize,
				rotationSpeed = rotationSpeed(),
				type = starType,
			)
			emit(star)
			
			setOf(star.withoutName() as CelestialObject.Star)
		} else {
			// Binary star system
			
			val theta = (0.0..2 * PI).random()
			val starAPos = polarVector(36.0, theta)
			val starBPos = -starAPos
			
			usedPositions += starAPos
			usedPositions += starBPos
			
			val starAType = randomStarType()
			val starASize = starAType.sizeRange.random()
			val starA = UnnamedCelestialObject.Star(
				position = Position(starAPos),
				size = starASize,
				rotationSpeed = rotationSpeed(),
				type = starAType,
			)
			
			val starBType = randomStarType()
			val starBSize = starBType.sizeRange.random()
			val starB = UnnamedCelestialObject.Star(
				position = Position(starBPos),
				size = starBSize,
				rotationSpeed = rotationSpeed(),
				type = starBType,
			)
			
			emit(starA)
			emit(starB)
			
			setOf(
				starA.withoutName() as CelestialObject.Star,
				starB.withoutName() as CelestialObject.Star,
			)
		}
		
		val activePositions = mutableSetOf<Vec2>()
		activePositions += usedPositions
		while (activePositions.isNotEmpty()) {
			val aroundPoint = activePositions.random()
			val newPoints = (1..CELESTIAL_OBJECT_K).map { randomPositionInAnnulus(CELESTIAL_OBJECT_R, CELESTIAL_OBJECT_R * 2) + aroundPoint }
			
			var isInactive = true
			for (newPoint in newPoints) {
				throttle()
				
				if (usedPositions.any { (it - newPoint).magnitude < CELESTIAL_OBJECT_R })
					continue
				
				usedPositions.add(newPoint)
				activePositions.add(newPoint)
				isInactive = false
				
				val planetPos = Position(newPoint)
				
				val habitability = habitabilityAt(stars, planetPos)
				val planetType = habitability.randomPlanetType()
				val planetSize = planetType.sizeRange.random()
				
				if (planetType == PlanetType.TERRESTRIAL || Random.nextDouble() < settings.planetDensity.chanceToAdd)
					emit(
						UnnamedCelestialObject.Planet(
							position = planetPos,
							size = planetSize,
							rotationSpeed = rotationSpeed(),
							type = planetType,
						)
					)
			}
			
			if (isInactive)
				activePositions -= aroundPoint
		}
	}
	
	private fun rotationSpeed() = (0.125..0.625).random() * (if ((1..7).random() == 1) -1 else 1)
	
	private fun randomStarType() = (StarType.values().toSet() - StarType.X).random()
	
	private suspend fun corruptStarSystem(starSystem: UnplacedStarSystem) = starSystem.copy(bodies = starSystem.bodies.map { obj ->
		throttle()
		when (obj) {
			is CelestialObject.Planet -> if (obj.type.isGiant)
				obj.copy(type = PlanetType.VEILED_GIANT)
			else
				obj.copy(type = PlanetType.VEILED)
			is CelestialObject.Star -> obj.copy(type = StarType.X)
		}
	}.toSet())
	
	private suspend fun assignFactions(starSystems: Map<Id<StarSystem>, StarSystem>, warpLanes: Set<WarpLane>): Map<Id<StarSystem>, StarSystem> {
		val systemControllers = (starSystems.keys.shuffled() zip settings.factions.asGenerationSequence().take(starSystems.size * 2 / 5).toList()).toMap().toMutableMap()
		
		val uncontrolledSystems = (starSystems.keys - systemControllers.keys).toMutableSet()
		
		while (uncontrolledSystems.size > starSystems.size / 5) {
			val controlledSystems = systemControllers.keys.shuffled()
			
			var shouldKeepLooping = false
			for (systemId in controlledSystems) {
				val borderingSystems = mutableSetOf<Id<StarSystem>>()
				
				for (lane in warpLanes) {
					throttle()
					
					if (systemId == lane.systemA)
						borderingSystems += lane.systemB
					if (systemId == lane.systemB)
						borderingSystems += lane.systemA
				}
				
				val degree = borderingSystems.size
				borderingSystems.retainAll(uncontrolledSystems)
				
				for (borderId in borderingSystems) {
					throttle()
					
					uncontrolledSystems -= borderId
					
					if (Random.nextDouble() < settings.contention.controlSpreadChance / degree)
						systemControllers[borderId] = systemControllers.getValue(systemId).let { faction ->
							if (Random.nextBoolean())
								faction
							else
								settings.factions.getRelatedFaction(faction)
						}
					
					shouldKeepLooping = true
				}
			}
			
			if (!shouldKeepLooping)
				break
		}
		
		return starSystems.mapValues { (id, system) ->
			system.copy(holder = systemControllers[id])
		}
	}
	
	private suspend fun generateFleets(starSystems: Map<Id<StarSystem>, StarSystem>): Map<Id<StarSystem>, StarSystem> {
		return starSystems.mapValues { (_, system) ->
			throttle()
			system.holder?.let { owner ->
				system.copy(
					fleets = generateFleetPresences(owner, settings.contention.maxFleets, settings.contention.fleetStrengthMult)
				)
			} ?: system
		}
	}
	
	companion object {
		const val SYSTEM_R = 1024.0
		const val SYSTEM_K = 4
		
		const val MAX_SYSTEM_SIZE = 384.0
		const val SYSTEM_MARGIN = 32.0
		
		const val CELESTIAL_OBJECT_R = 72.0
		const val CELESTIAL_OBJECT_K = 3
	}
}

private data class UnplacedStarSystem(
	val name: String,
	val radius: Double,
	val bodies: Set<CelestialObject>
) {
	fun place(position: Vec2) = StarSystem(
		name = name,
		holder = null,
		fleets = emptyMap(),
		position = Position(position),
		radius = radius,
		bodies = bodies.associateBy { Id() }
	)
}

private val UnplacedStarSystem.numHabitableWorlds: Int
	get() = bodies.count { it is CelestialObject.Planet && it.type == PlanetType.TERRESTRIAL }

@JvmInline
private value class UnnamedCelestialObject private constructor(private val celestialObject: CelestialObject) {
	val position: Position
		get() = celestialObject.position
	
	val size: Int
		get() = celestialObject.size
	
	fun withoutName() = celestialObject
	
	fun withName(name: String) = when (celestialObject) {
		is CelestialObject.Star -> celestialObject.copy(name = name)
		is CelestialObject.Planet -> celestialObject.copy(name = name)
	}
	
	companion object {
		fun Star(position: Position, size: Int, rotationSpeed: Double, type: StarType) = UnnamedCelestialObject(
			CelestialObject.Star("<unnamed>", position, size, rotationSpeed, type)
		)
		
		fun Planet(position: Position, size: Int, rotationSpeed: Double, type: PlanetType) = UnnamedCelestialObject(
			CelestialObject.Planet("<unnamed>", position, size, rotationSpeed, type)
		)
		
		fun giveNamesTo(systemName: String, unnamedCelestialObjects: List<UnnamedCelestialObject>): Set<CelestialObject> = buildSet {
			val stars = unnamedCelestialObjects.filter { it.celestialObject is CelestialObject.Star }
			
			when (stars.size) {
				1 -> add(stars.single().withName(systemName))
				2 -> {
					val (starA, starB) = stars.sortedByDescending { it.size }
					
					add(starA.withName("$systemName A"))
					add(starB.withName("$systemName B"))
				}
				else -> error("Invalid number of stars in system ${stars.size}")
			}
			
			val planets = unnamedCelestialObjects.filter { it.celestialObject is CelestialObject.Planet }
			
			val planetsIndexed = planets
				.sortedBy { it.position.vector.magnitude }
				.withIndex()
			
			for ((i, planet) in planetsIndexed)
				add(planet.withName("$systemName ${(i + 1).toRomanNumerals()}"))
		}
	}
}

private fun Vec2.Companion.orientation(p: Vec2, q: Vec2, r: Vec2): Int {
	val orientation = ((q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y))
	return if (orientation.absoluteValue < EPSILON)
		0
	else orientation.sign.toInt()
}

private operator fun WarpLane.contains(other: WarpLane) = (setOf(systemA, systemB) intersect setOf(other.systemA, other.systemB)).isNotEmpty()

private class LineSegment(val p: Vec2, val q: Vec2) {
	private fun containsInBox(r: Vec2): Boolean {
		return r.x <= max(p.x, q.x) && r.x >= min(p.x, q.x) && r.y <= max(p.y, q.y) && r.y >= min(p.y, q.y)
	}
	
	val length: Double
		get() = (p - q).magnitude
	
	fun intersectsCircle(center: Vec2, radius: Double) = Position(center).distanceToLineSegment(Position(p), Position(q)) <= radius
	
	operator fun contains(other: LineSegment): Boolean {
		val o1 = Vec2.orientation(p, q, other.p)
		val o2 = Vec2.orientation(p, q, other.q)
		val o3 = Vec2.orientation(other.p, other.q, p)
		val o4 = Vec2.orientation(other.p, other.q, q)
		
		if (o1 != o2 && o3 != o4)
			return true
		
		if (o1 == 0 && containsInBox(other.p))
			return true
		if (o2 == 0 && containsInBox(other.q))
			return true
		if (o3 == 0 && other.containsInBox(p))
			return true
		if (o4 == 0 && other.containsInBox(q))
			return true
		
		return false
	}
}

private enum class Habitability {
	HOT, WARM, PLEASANT, COOL, COLD, ELDRITCH;
}

private val CelestialObject.Star.surfaceLuminosity: Double
	get() {
		val hue = type.lightColor.let { (r, _, b) -> b - r } / 255.0
		val gray = type.lightColor.let { (r, g, b) -> r + g + b } / 765.0
		val area = 4 * PI * size // radius is proportional to sqrt(size)
		
		// neutron stars look blue, but emit light as if they were gray
		val temperature = exp(if (type == StarType.NEUTRON_STAR) 0.0 else hue)
		
		// not-so-physically-accurate variant of Stefan-Boltzmann equation
		return gray * area * temperature
	}

private fun CelestialObject.Star.apparentMagnitude(distance: Double) = surfaceLuminosity / distance.pow(2)

private val perfectComfortMagnitude: Double by lazy {
	val sunlikeStar = CelestialObject.Star(
		"Sol",
		Position(Vec2(0.0, 0.0)),
		13,
		0.0,
		StarType.G
	)
	
	val earthDistance = 128.0
	
	sunlikeStar.apparentMagnitude(earthDistance)
}

private fun habitabilityAt(stars: Set<CelestialObject.Star>, position: Position): Habitability {
	if (stars.any { it.type.isEldritch })
		return Habitability.ELDRITCH
	
	val receivedLight = stars.sumOf { it.apparentMagnitude((it.position - position).length).pow(2) }.pow(0.5)
	
	return when {
		receivedLight < 0.25 * perfectComfortMagnitude -> Habitability.COLD
		receivedLight < 0.75 * perfectComfortMagnitude -> Habitability.COOL
		receivedLight < 1.25 * perfectComfortMagnitude -> Habitability.PLEASANT
		receivedLight < 1.75 * perfectComfortMagnitude -> Habitability.WARM
		else -> Habitability.HOT
	}
}

private fun Habitability.randomPlanetType() = when (this) {
	Habitability.HOT -> mapOf(
		PlanetType.BARREN to 2.5,
		PlanetType.GAS_GIANT to 1.0,
	)
	Habitability.WARM -> mapOf(
		PlanetType.BARREN to 3.0,
		PlanetType.TOXIC to 2.0,
		PlanetType.GAS_GIANT to 1.0
	)
	Habitability.PLEASANT -> mapOf(
		PlanetType.DUSTY to 2.5,
		PlanetType.TOXIC to 2.0,
		PlanetType.BARREN to 1.5,
		PlanetType.TERRESTRIAL to 1.0,
	)
	Habitability.COOL -> mapOf(
		PlanetType.BARREN to 3.0,
		PlanetType.DUSTY to 2.5,
		PlanetType.THOLIN to 2.0,
		PlanetType.ICE_SHELL to 1.5,
		PlanetType.GAS_GIANT to 1.0
	)
	Habitability.COLD -> mapOf(
		PlanetType.BARREN to 2.5,
		PlanetType.ICE_GIANT to 1.0,
	)
	Habitability.ELDRITCH -> mapOf(
		PlanetType.VEILED to 2.5,
		PlanetType.VEILED_GIANT to 1.0,
	)
}.weightedRandom()
