package net.starshipfights.campaign

import kotlinx.coroutines.runBlocking
import net.starshipfights.game.Vec2
import net.starshipfights.game.magnitude
import net.starshipfights.game.plus
import net.starshipfights.game.times
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JOptionPane
import javax.swing.UIManager
import kotlin.math.roundToInt

object ClusterGenTesting {
	@JvmStatic
	fun main(args: Array<String>) {
		UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
		
		val clusterSizes = ClusterSize.values().toList()
		val clusterSizeOptions = clusterSizes.map { it.toString() }.toTypedArray()
		
		val clusterSizeIndex = JOptionPane.showOptionDialog(
			null, "Please select the size of your star cluster",
			"Generate Star Cluster", JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE, null,
			clusterSizeOptions, clusterSizeOptions[0]
		)
		
		if (clusterSizeIndex == JOptionPane.CLOSED_OPTION) return
		
		val laneDensities = ClusterLaneDensity.values().toList()
		val laneDensityOptions = laneDensities.map { it.toString() }.toTypedArray()
		
		val laneDensityIndex = JOptionPane.showOptionDialog(
			null, "Please select the warp-lane density of your star cluster",
			"Generate Star Cluster", JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE, null,
			laneDensityOptions, laneDensityOptions[0]
		)
		
		if (laneDensityIndex == JOptionPane.CLOSED_OPTION) return
		
		val planetDensities = ClusterPlanetDensity.values().toList()
		val planetDensityOptions = planetDensities.map { it.toString() }.toTypedArray()
		
		val planetDensityIndex = JOptionPane.showOptionDialog(
			null, "Please select the planet density of your star cluster",
			"Generate Star Cluster", JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE, null,
			planetDensityOptions, planetDensityOptions[0]
		)
		
		if (planetDensityIndex == JOptionPane.CLOSED_OPTION) return
		
		val settings = ClusterGenerationSettings(
			StarClusterBackground.RED,
			clusterSizes[clusterSizeIndex],
			laneDensities[laneDensityIndex],
			planetDensities[planetDensityIndex],
			ClusterCorruption.MATERIAL
		)
		
		val starCluster = runBlocking {
			ClusterGenerator(settings).generateCluster()
		}
		
		val radius = (starCluster.systems.maxOf { (_, system) -> system.position.vector.magnitude } * SCALE_FACTOR)
		val radiusShift = Vec2(radius, radius)
		
		val imageSize = ((radius + IMAGE_MARGIN) * 2).roundToInt()
		val image = BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB)
		val g2d = image.createGraphics()
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
			
			g2d.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
			
			g2d.color = Color.white
			g2d.fillRect(0, 0, imageSize, imageSize)
			
			g2d.color = Color.black
			g2d.fillRect(1, 1, imageSize - 2, imageSize - 2)
			
			g2d.color = Color.decode("#3366CC")
			g2d.stroke = BasicStroke(2.5f)
			for ((aId, bId) in starCluster.lanes) {
				val aPos = starCluster.systems.getValue(aId).position.vector * SCALE_FACTOR + radiusShift
				val bPos = starCluster.systems.getValue(bId).position.vector * SCALE_FACTOR + radiusShift
				
				g2d.drawLine(
					(aPos.x + IMAGE_MARGIN).roundToInt(),
					(aPos.y + IMAGE_MARGIN).roundToInt(),
					(bPos.x + IMAGE_MARGIN).roundToInt(),
					(bPos.y + IMAGE_MARGIN).roundToInt(),
				)
			}
			
			g2d.color = Color.decode("#CC9933")
			for (system in starCluster.systems.values) {
				val pos = system.position.vector * SCALE_FACTOR + radiusShift
				val r = system.radius * SCALE_FACTOR
				
				g2d.fillOval(
					(pos.x + IMAGE_MARGIN - r).roundToInt(),
					(pos.y + IMAGE_MARGIN - r).roundToInt(),
					(r * 2).roundToInt(),
					(r * 2).roundToInt(),
				)
			}
			
			// Draw names
			g2d.color = Color.white
			for (system in starCluster.systems.values) {
				val pos = system.position.vector * SCALE_FACTOR + radiusShift
				
				val minusX = g2d.fontMetrics.stringWidth(system.name) * 0.5
				val minusY = g2d.fontMetrics.ascent * -0.5
				
				g2d.drawString(
					system.name,
					(pos.x + IMAGE_MARGIN - minusX).roundToInt(),
					(pos.y + IMAGE_MARGIN - minusY).roundToInt(),
				)
			}
		} finally {
			g2d.dispose()
		}
		
		ImageIO.write(image, "PNG", File("test_output/cluster_gen.png"))
	}
	
	private const val IMAGE_MARGIN = 51.2
	private const val SCALE_FACTOR = 0.32
}
