package starshipfights.game.ai

import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import starshipfights.game.BattleSize
import starshipfights.game.Faction
import java.io.File
import javax.swing.JOptionPane
import javax.swing.UIManager

object AITesting {
	@JvmStatic
	fun main(args: Array<String>) {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
		
		val instinctVectorCounts = listOf(5, 11, 17)
		val instinctVectorOptions = instinctVectorCounts.map { it.toString() }.toTypedArray()
		
		val instinctVectorIndex = JOptionPane.showOptionDialog(
			null, "Please select the number of Instinct vectors to generate",
			"Generate Instinct Vectors", JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE, null,
			instinctVectorOptions, instinctVectorOptions[0]
		)
		
		if (instinctVectorIndex == JOptionPane.CLOSED_OPTION) return
		
		val instinctVectors = genInstinctCandidates(instinctVectorCounts[instinctVectorIndex])
		
		val numTrialCounts = listOf(3, 5, 7, 10)
		val numTrialOptions = numTrialCounts.map { it.toString() }.toTypedArray()
		
		val numTrialIndex = JOptionPane.showOptionDialog(
			null, "Please select the number of trials to execute per instinct pairing",
			"Number of Trials", JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE, null,
			numTrialOptions, numTrialOptions[0]
		)
		
		if (numTrialIndex == JOptionPane.CLOSED_OPTION) return
		
		val numTrials = numTrialCounts[numTrialIndex]
		
		val allowedBattleSizeChoices = BattleSize.values().map { setOf(it) } + listOf(BattleSize.values().toSet())
		val allowedBattleSizeOptions = allowedBattleSizeChoices.map { it.singleOrNull()?.displayName ?: "Allow Any" }.toTypedArray()
		
		val allowedBattleSizeIndex = JOptionPane.showOptionDialog(
			null, "Please select the allowed sizes of battle",
			"Allowed Battle Sizes", JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE, null,
			allowedBattleSizeOptions, allowedBattleSizeOptions[0]
		)
		
		if (allowedBattleSizeIndex == JOptionPane.CLOSED_OPTION) return
		
		val allowedBattleSizes = allowedBattleSizeChoices[allowedBattleSizeIndex]
		
		val allowedFactionChoices = Faction.values().map { setOf(it) } + listOf(Faction.values().toSet())
		val allowedFactionOptions = allowedFactionChoices.map { it.singleOrNull()?.shortName ?: "Allow Any" }.toTypedArray()
		
		val allowedFactionIndex = JOptionPane.showOptionDialog(
			null, "Please select the allowed factions in battle",
			"Allowed Factions", JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE, null,
			allowedFactionOptions, allowedFactionOptions[0]
		)
		
		if (allowedFactionIndex == JOptionPane.CLOSED_OPTION) return
		
		val allowedFactions = allowedFactionChoices[allowedFactionIndex]
		
		val instinctSuccessRate = runBlocking {
			performTrials(numTrials, instinctVectors, allowedBattleSizes, allowedFactions)
		}
		
		val indexedInstincts = instinctSuccessRate
			.toList()
			.sortedBy { (_, v) -> v }
			.mapIndexed { i, p -> (i + 1) to p }
		
		val results = createHTML(prettyPrint = false, xhtmlCompatible = true).html {
			head {
				title { +"Test Results" }
			}
			body {
				style = "font-family:sans-serif"
				
				h1 { +"Test Results" }
				p { +"These are the results of testing AI instinct parameters in Starship Fights" }
				h2 { +"Test Parameters" }
				p { +"Number of Instincts Generated: ${instinctVectors.size}" }
				p { +"Number of Trials Per Instinct Pairing: $numTrials" }
				p { +"Battle Sizes Allowed: ${allowedBattleSizes.singleOrNull()?.displayName ?: "All"}" }
				p { +"Factions Allowed: ${allowedFactions.singleOrNull()?.polityName ?: "All"}" }
				h2 { +"Instincts Vectors and Battle Results" }
				table {
					val cellStyle = "border: 1px solid rgba(0, 0, 0, 0.6)"
					thead {
						tr {
							th(scope = ThScope.row) {
								style = cellStyle
								+"Vector Values"
							}
							th(scope = ThScope.col) {
								style = cellStyle
								+"Battles Won as Host"
							}
							allInstincts.forEach {
								th(scope = ThScope.col) {
									style = cellStyle
									+it.key
								}
							}
						}
					}
					tbody {
						indexedInstincts.forEach { (i, pair) ->
							val (instincts, successRate) = pair
							tr {
								th(scope = ThScope.row) {
									style = cellStyle
									+"Instincts $i"
								}
								td {
									style = cellStyle
									+"$successRate"
								}
								allInstincts.forEach { key ->
									td {
										style = cellStyle
										+"${instincts[key]}"
									}
								}
							}
						}
					}
				}
			}
		}
		
		File("test_results.html").writeText(results)
	}
}
