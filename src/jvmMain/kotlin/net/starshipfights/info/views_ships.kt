package net.starshipfights.info

import io.ktor.application.*
import kotlinx.html.*
import net.starshipfights.game.*
import kotlin.math.PI
import kotlin.math.roundToInt

private val shipsPageSidebar: PageNavSidebar
	get() = PageNavSidebar(
		listOf<NavItem>(NavHead("Jump to Faction")) + Faction.values().map { faction ->
			NavLink("#${faction.toUrlSlug()}", faction.polityName)
		}
	)

suspend fun ApplicationCall.shipsPage(): HTML.() -> Unit = page("Strategema Nauticum", standardNavBar(), shipsPageSidebar) {
	section {
		h1 {
			foreign("la") { +"Strategema Nauticum" }
		}
		p {
			+"Here you will find an index of all ship classes in Starship Fights, with links to pages that show ship stats and appearances."
		}
	}
	for ((faction, factionShipTypes) in ShipType.values().groupBy { it.faction }.toSortedMap()) {
		section {
			id = faction.toUrlSlug()
			
			h2 { +faction.polityName }
			
			p {
				style = "text-align:center"
				img(src = faction.flagUrl, alt = "Flag of ${faction.getDefiniteShortName()}") {
					style = "width:40%"
				}
			}
			
			faction.blurbDesc(consumer)
			
			for ((weightClass, weightedShipTypes) in factionShipTypes.groupBy { it.weightClass }.toSortedMap(Comparator.comparingInt(ShipWeightClass::tier))) {
				h3 { +weightClass.displayName }
				ul {
					for (shipType in weightedShipTypes) {
						li {
							a(href = "/info/${shipType.toUrlSlug()}") {
								+shipType.fullDisplayName
								+" (${shipType.pointCost} points)"
							}
						}
					}
				}
			}
		}
	}
}

suspend fun ApplicationCall.shipPage(shipType: ShipType): HTML.() -> Unit = page(
	shipType.fullerDisplayName,
	standardNavBar(),
	ShipViewSidebar(shipType)
) {
	section {
		h1 { +shipType.fullDisplayName }
		
		p { +Entities.nbsp }
		
		table {
			tr {
				th { +"Weight Class" }
				th { +"Hull Integrity" }
				th { +"Defense Turrets" }
			}
			tr {
				td {
					+shipType.weightClass.displayName
					br
					+"(${shipType.pointCost} points to deploy)"
				}
				td {
					+"${shipType.weightClass.durability.maxHullPoints} impacts"
					br
					+"${shipType.weightClass.durability.troopsDefense} troops"
				}
				td {
					when (val durability = shipType.weightClass.durability) {
						is StandardShipDurability -> +"${durability.turretDefense.toPercent()} fighter-wing equivalent"
						is FelinaeShipDurability -> {
							span {
								style = "font-style:italic"
								+"Felinae Felices ships do not use turrets"
							}
							br
							br
							+"Disruption Pulse can wipe out strike craft up to ${durability.disruptionPulseRange} meters away up to ${durability.disruptionPulseShots} times"
						}
					}
				}
			}
			tr {
				th { +"Max Movement" }
				th { +"Reactor Power" }
				th { +"Energy Flow" }
			}
			tr {
				when (val movement = shipType.weightClass.movement) {
					is StandardShipMovement -> td {
						+"Accelerate ${movement.moveSpeed.roundToInt()} meters/turn"
						br
						+"Rotate ${(movement.turnAngle * 180.0 / PI).roundToInt()} degrees/turn"
					}
					is FelinaeShipMovement -> td {
						+"Accelerate ${movement.moveSpeed.roundToInt()} meters/turn"
						br
						+"Rotate ${(movement.turnAngle * 180.0 / PI).roundToInt()} degrees/turn"
						br
						br
						+"Inertialess Drive can jump up to ${movement.inertialessDriveRange} meters up to ${movement.inertialessDriveShots} times"
					}
				}
				
				when (val reactor = shipType.weightClass.reactor) {
					is StandardShipReactor -> {
						td {
							+reactor.powerOutput.toString()
							br
							+"(${reactor.subsystemAmount} per subsystem)"
						}
						td {
							+reactor.gridEfficiency.toString()
						}
					}
					FelinaeShipReactor -> {
						td {
							colSpan = "2"
							style = "font-style:italic"
							+"Felinae Felices ships use hyper-technologically-advanced super-reactors that need not concern themselves with \"power output\" or \"grid efficiency\"."
						}
					}
				}
			}
			tr {
				th { +"Base Crit Chance" }
				th { +"Cannon Targeting" }
				th { +"Lance Efficiency" }
			}
			tr {
				td {
					+shipType.weightClass.firepower.criticalChance.toPercent()
				}
				td {
					+shipType.weightClass.firepower.cannonAccuracy.toPercent()
				}
				td {
					if (shipType.weightClass.firepower.lanceCharging < 0.0)
						+"N/A"
					else
						+shipType.weightClass.firepower.lanceCharging.toPercent()
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
			
			for ((label, weapons) in shipType.armaments.values.groupBy { it.groupLabel }) {
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
						val weaponRangeMult = when (weapon) {
							is ShipWeapon.Cannon -> shipType.weightClass.firepower.rangeMultiplier
							is ShipWeapon.Lance -> shipType.weightClass.firepower.rangeMultiplier
							is ShipWeapon.ParticleClawLauncher -> shipType.weightClass.firepower.rangeMultiplier
							is ShipWeapon.LightningYarn -> shipType.weightClass.firepower.rangeMultiplier
							else -> 1.0
						}
						
						weapon.minRange.takeIf { it != SHIP_BASE_SIZE }?.let { +"${it.roundToInt()}-" }
						+"${(weapon.maxRange * weaponRangeMult).roundToInt()} meters"
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
							is ShipWeapon.ParticleClawLauncher -> "$numShots particle claw launcher" + (if (numShots == 1) "" else "s")
							is ShipWeapon.LightningYarn -> "$numShots lightning yarn launcher" + (if (numShots == 1) "" else "s")
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
