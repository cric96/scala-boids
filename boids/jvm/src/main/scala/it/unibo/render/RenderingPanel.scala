package it.unibo.render

import java.awt.{BorderLayout, Color, Graphics}
import java.awt.image.BufferedImage
import javax.swing.{BoxLayout, JButton, JPanel}
import scala.collection.immutable.ListMap

class RenderingPanel(var backgroundImage: Option[BufferedImage]) extends JPanel:
  private val contentPanel = JPanel()
  private val buttonPanel = JPanel()
  private var showBoids = false
  buttonPanel.setBackground(new Color(0, 0, 0, 0))
  contentPanel.setLayout(BoxLayout(contentPanel, BoxLayout.Y_AXIS))
  contentPanel.setBackground(Color(0, 0, 0, 0))
  this.setLayout(new BorderLayout())
  val showBoidsButton = JButton("show boids")
  buttonPanel.add(showBoidsButton)
  private val sliders: Map[Parameters, Slider] = slidersSpec.map { case (name: Parameters, (min, max, value)) =>
    name -> Slider(name, min, max, value)
  }
  sliders.map(_._2.render).foreach(contentPanel.add)
  this.add(contentPanel, BorderLayout.WEST)
  this.add(buttonPanel, BorderLayout.NORTH)
  showBoidsButton.addActionListener(_ => showBoids = !showBoids)
  def parametersOf(param: Parameters): Double = sliders(param).value
  def boidsEnabled: Boolean = showBoids
  override def paintComponent(g: Graphics): Unit =
    super.paintComponent(g)
    sliders.foreach(_._2.update)
    backgroundImage.foreach(backgroundImage => g.drawImage(backgroundImage, 0, 0, this))

def slidersSpec: Map[Parameters, (Double, Double, Double)] = ListMap(
  "separation" -> (1, 3, 2.36),
  "align" -> (1, 3, 1),
  "cohesion" -> (1, 3, 1),
  "visionRange" -> (20, 200, 50),
  "separationRange" -> (5, 100, 21),
  "bucketSize" -> (5, 40, 10),
  "bucketRange" -> (20, 200, 50),
  "boidsCount" -> (500, 4000, 1000),
  "force" -> (0.01, 0.1, 0.03)
)
