package starshipfights.info

import io.ktor.application.*
import kotlinx.html.*
import starshipfights.game.*
import kotlin.math.PI
import kotlin.math.roundToInt

private val shipsPageSidebar: PageNavSidebar
	get() = PageNavSidebar(
		listOf<NavItem>(NavHead("Jump to Faction")) + Faction.values().map { faction ->
			NavLink("#${faction.toUrlSlug()}", faction.shortName)
		}
	)

suspend fun ApplicationCall.shipsPage(): HTML.() -> Unit = page("Game Manual", standardNavBar(), shipsPageSidebar) {
	section {
		h1 { +"The Enclosed Instruction Book" }
		p {
			+"Here you will find an explanation of the game's mechanics and structure, as well as an index of all ship classes in Starship Fights, with links to pages that show ship stats and appearances."
		}
		h2 { +"Game Mechanics" }
		h3 { +"Types of Weapons" }
		p {
			+"The four main types of weapons in Starship Fights are cannons, lances, torpedoes, and strike craft. Cannons fire bolts of massive particles that have a chance to miss their target; this chance increases with distance and relative velocity. Lances fire a beam of massless particles that strike their target instantly, however lances also need to be charged by spending Weapons Power. Torpedoes are strong against unshielded hulls, guaranteed to deal two impacts, but are weak against shields, with only a 50% chance to hit if the target has its shields up. Strike craft come in two flavors: fighters and bombers. Fighters are used to defend your ships from bombers, while bombers are used to attack hostile ships."
		}
		p {
			+"There are also three types of special weapons: the Mechyrdians' Mega Giga Cannon, the Masra Draetsen Revelation Gun, and the Isarnareyksk EMP Emitter. The Mega Giga Cannon fires a long-range projectile that deals severe damage to enemy ships, but has a limited number of shots. The Revelation Gun instantly vaporizes an enemy ship, but can only be used once in a battle. The EMP Antenna depletes by a random amount the targeted ships' subsystem powers."
		}
		h3 { +"Subsystem Powering" }
		p {
			+"Ships have two particular attributes that are closely related: Reactor Power and Energy Flow. Reactor Power is how much power the ship's generators generate, and starts off as being split evenly between the ship's three subsystems: Weapons, Shields, and Engines. Weapons Power is expended when firing Cannons or charging Lances; Shields Power is expended whenever the ship's shields are impacted by enemy fire; finally, Engines Power modifies the speed and turn rate of the ship. The ship's Energy Flow statistic determines how many transfers can be made between subsystems during the Power Distribution phase of a turn."
		}
		h3 { +"Turn Structure" }
		p {
			+"Games start with a pre-battle deployment phase, and continue with three-phase turns. During the deploy phase, both players simultaneously deploy their fleets in the area that they are allowed to deploy ships within. Once both players are done deploying, the battle begins: enemy ships are revealed as Signals (not yet identified as ships), and the first Turn starts. The first phase of a turn is the Power Distribution phase: ships distribute power between their various subsystems. Both players take this phase simultaneously."
		}
		p {
			+"The next phase is the Ship Movement phase: ships turn and then either accelerate or decelerate. The velocity of a ship is the vector distance from the ship's previous position and its current position. If a ship isn't moved manually, it is considered drifting, and will move in accordance with its current velocity. If a ship is moved manually, first it decides a direction and angle to turn towards. Then, it moves along a line centered on what its position would be if it were left to drift; the direction of this line is halfway between the angle that the ship is facing, and the direction of its current velocity. Once both players are done moving their ships, the third and final phase begins."
		}
		p {
			+"At the last phase of a turn, ships fire weapons at each other. Again, both players take this phase simultaneously. Ships fire weapons at enemy ships, as far as they can and as much as they can. Damage from weapons is inflicted on targeted ships instantly, so players are encouraged to click fast when attacking enemy ships. Note that this does not apply to strike craft; they are deployed to ships, with percentages on the ship labels indicating the total strength of all strike wings surrounding a ship, and the damage done by bombers is calculated at the end of the phase, before the next turn begins."
		}
	}
	ShipType.values().groupBy { it.faction }.toSortedMap().forEach { (faction, factionShipTypes) ->
		section {
			id = faction.toUrlSlug()
			
			h2 { +faction.shortName }
			
			p {
				style = "text-align:center"
				img(src = faction.flagUrl, alt = "Flag of ${faction.getDefiniteShortName()}") {
					style = "width:40%"
				}
			}
			
			faction.blurbDesc(consumer)
			
			factionShipTypes.groupBy { it.weightClass }.toSortedMap(Comparator.comparingInt(ShipWeightClass::rank)).forEach { (weightClass, weightedShipTypes) ->
				h3 { +weightClass.displayName }
				ul {
					weightedShipTypes.forEach { shipType ->
						li {
							a(href = "/info/${shipType.toUrlSlug()}") { +shipType.fullDisplayName }
						}
					}
				}
			}
		}
	}
}

suspend fun ApplicationCall.shipPage(shipType: ShipType): HTML.() -> Unit = page(shipType.fullerDisplayName, standardNavBar(), ShipViewSidebar(shipType)) {
	section {
		h1 { +shipType.fullDisplayName }
		
		p { +Entities.nbsp }
		
		table {
			tr {
				th {
					+"Weight Class"
					br
					+"(Point Cost)"
				}
				th {
					+"Hull Integrity"
				}
				th { +"Max Acceleration" }
				th { +"Max Rotation" }
				th {
					+"Reactor Power"
					br
					+"(Per Subsystem)"
				}
				th { +"Energy Flow" }
			}
			tr {
				td {
					+shipType.weightClass.displayName
					br
					+"(${shipType.weightClass.basePointCost})"
				}
				td {
					+"${shipType.weightClass.durability.maxHullPoints} impacts"
				}
				td {
					+"${shipType.weightClass.movement.moveSpeed.roundToInt()} meters/turn"
				}
				td {
					+"${(shipType.weightClass.movement.turnAngle * 180.0 / PI).roundToInt()} degrees/turn"
				}
				td {
					+shipType.weightClass.reactor.powerOutput.toString()
					br
					+"(${shipType.weightClass.reactor.subsystemAmount})"
				}
				td {
					+shipType.weightClass.reactor.gridEfficiency.toString()
				}
			}
		}
		table {
			tr {
				th { +"Armament" }
				th { +"Firing Arcs" }
				th { +"Range" }
				th { +"Firepower" }
			}
			
			val groupedWeapons = shipType.armaments.weapons.values.groupBy { it.groupLabel }
			groupedWeapons.forEach { (label, weapons) ->
				val weapon = weapons.distinct().single()
				val numShots = weapons.sumOf { it.numShots }
				
				tr {
					td { +label }
					td {
						+if (weapon is AreaWeapon && weapon.isLine) {
							"Linear (Fore-firing)"
						} else if (weapon is ShipWeapon.Hangar) {
							"(Omnidirectional)"
						} else {
							weapon.firingArcs.joinToString { arc -> arc.displayName }
						}
					}
					td {
						weapon.minRange.takeIf { it != SHIP_BASE_SIZE }?.let { +"${it.roundToInt()}-" }
						+"${weapon.maxRange.roundToInt()} meters"
						if (weapon is AreaWeapon) {
							br
							+"${weapon.areaRadius.roundToInt()} meter impact radius"
						}
					}
					td {
						+when (weapon) {
							is ShipWeapon.Cannon -> "$numShots cannon" + (if (numShots == 1) "" else "s")
							is ShipWeapon.Lance -> "$numShots lance" + (if (numShots == 1) "" else "s")
							is ShipWeapon.Torpedo -> "$numShots launcher" + (if (numShots == 1) "" else "s")
							is ShipWeapon.Hangar -> "$numShots strike wing" + (if (numShots == 1) "" else "s")
							ShipWeapon.MegaCannon -> "Severely damages targets"
							ShipWeapon.RevelationGun -> "Vaporizes target"
							ShipWeapon.EmpAntenna -> "Randomly depletes targets' subsystems"
						}
					}
				}
			}
		}
		
		p { +Entities.nbsp }
		
		canvas {
			style = "width:100%;height:25em"
			attributes["data-model"] = shipType.meshName
		}
		
		script {
			unsafe { +"window.sfShipMeshViewer = true;" }
		}
	}
}
