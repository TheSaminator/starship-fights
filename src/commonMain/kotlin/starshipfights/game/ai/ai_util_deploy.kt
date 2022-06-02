package starshipfights.game.ai

import starshipfights.data.Id
import starshipfights.game.*
import kotlin.math.sign

val deployEscortFocus by instinct(1.0..5.0)
val deployCruiserFocus by instinct(1.0..5.0)
val deployBattleshipFocus by instinct(1.0..5.0)

val ShipWeightClass.focus: Instinct
	get() = when (this) {
		ShipWeightClass.ESCORT -> deployEscortFocus
		ShipWeightClass.DESTROYER -> deployCruiserFocus
		ShipWeightClass.CRUISER -> deployCruiserFocus
		ShipWeightClass.BATTLECRUISER -> deployCruiserFocus
		ShipWeightClass.BATTLESHIP -> deployBattleshipFocus
		
		ShipWeightClass.BATTLE_BARGE -> deployBattleshipFocus
		
		ShipWeightClass.GRAND_CRUISER -> deployBattleshipFocus
		ShipWeightClass.COLOSSUS -> deployBattleshipFocus
		
		ShipWeightClass.FF_ESCORT -> deployEscortFocus
		ShipWeightClass.FF_DESTROYER -> deployCruiserFocus
		ShipWeightClass.FF_CRUISER -> deployCruiserFocus
		ShipWeightClass.FF_BATTLECRUISER -> deployCruiserFocus
		ShipWeightClass.FF_BATTLESHIP -> deployBattleshipFocus
		
		ShipWeightClass.AUXILIARY_SHIP -> deployEscortFocus
		ShipWeightClass.LIGHT_CRUISER -> deployEscortFocus
		ShipWeightClass.MEDIUM_CRUISER -> deployCruiserFocus
		ShipWeightClass.HEAVY_CRUISER -> deployBattleshipFocus
		
		ShipWeightClass.FRIGATE -> deployEscortFocus
		ShipWeightClass.LINE_SHIP -> deployCruiserFocus
		ShipWeightClass.DREADNOUGHT -> deployBattleshipFocus
	}

private val ShipWeightClass.rowIndex: Int
	get() = when (this) {
		ShipWeightClass.ESCORT -> 3
		ShipWeightClass.DESTROYER -> 2
		ShipWeightClass.CRUISER -> 2
		ShipWeightClass.BATTLECRUISER -> 1
		ShipWeightClass.BATTLESHIP -> 0
		
		ShipWeightClass.BATTLE_BARGE -> 0
		
		ShipWeightClass.GRAND_CRUISER -> 1
		ShipWeightClass.COLOSSUS -> 0
		
		ShipWeightClass.FF_ESCORT -> 3
		ShipWeightClass.FF_DESTROYER -> 2
		ShipWeightClass.FF_CRUISER -> 1
		ShipWeightClass.FF_BATTLECRUISER -> 1
		ShipWeightClass.FF_BATTLESHIP -> 0
		
		ShipWeightClass.AUXILIARY_SHIP -> 3
		ShipWeightClass.LIGHT_CRUISER -> 2
		ShipWeightClass.MEDIUM_CRUISER -> 1
		ShipWeightClass.HEAVY_CRUISER -> 0
		
		ShipWeightClass.FRIGATE -> 2
		ShipWeightClass.LINE_SHIP -> 1
		ShipWeightClass.DREADNOUGHT -> 0
	}

fun placeShips(ships: Set<Ship>, deployRectangle: PickBoundary.Rectangle): Map<Id<ShipInstance>, Position> {
	val fieldBoundSign = deployRectangle.center.vector.y.sign
	val fieldBoundary = deployRectangle.center.vector.y + (deployRectangle.length2 * fieldBoundSign)
	val rows = listOf(125.0, 625.0, 1125.0, 1625.0).map {
		fieldBoundary - (it * fieldBoundSign)
	}
	
	val shipsByRow = ships.groupBy { it.shipType.weightClass.rowIndex }
	return buildMap {
		for ((rowIndex, rowShips) in shipsByRow) {
			val row = rows[rowIndex]
			val rowMax = rowShips.size - 1
			for ((shipIndex, ship) in rowShips.withIndex()) {
				val col = (shipIndex * 500.0) - (rowMax * 250.0)
				put(ship.id.reinterpret(), Position(Vec2(col, row)))
			}
		}
	}
}
