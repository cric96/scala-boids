package it.unibo.render

import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.core.Environment
import it.unibo.core.dynamics.Flocking
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

import java.awt.{Color, Frame, Graphics, GraphicsEnvironment}
import java.awt.image.BufferedImage
import javax.swing.{JButton, JFrame, JPanel, WindowConstants}

/** */
class SwingRender extends Renderer with ConfigurationStore:
  private val config = GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.getDefaultConfiguration
  private val frame = new JFrame()
  frame.setExtendedState(Frame.MAXIMIZED_BOTH)
  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  frame.setVisible(true)
  val image = config.createCompatibleImage(10, 10)
  private val renderingFrame = RenderingPanel(image)
  frame.setContentPane(renderingFrame)
  def bounds: Rectangle2D =
    Rectangle2D(Vector2D(0, 0), Vector2D(frame.getWidth, frame.getHeight))
  def render(environment: Environment): Unit =
    val image = config.createCompatibleImage(frame.getWidth, frame.getHeight)
    val renderingContext = image.getGraphics
    renderingContext.setColor(Color.white)
    renderingContext.fillRect(0, 0, frame.getWidth, frame.getHeight)
    renderingFrame.backgroundImage = image
    val precision = 10
    def iterateUntil(start: Double, end: Double): LazyList[Double] =
      LazyList.iterate(start)(_ + precision).takeWhile(_ < end)
    val allCoords = for {
      x <- iterateUntil(0, frame.getWidth)
      y <- iterateUntil(0, frame.getWidth)
    } yield (Vector2D(x, y))
    val densityMap = allCoords.map { coord =>
      val bottomLeft = coord - (Vector2D(50 / 2, 50 / 2))
      val topRight = coord + (Vector2D(50 / 2, 50 / 2))
      val density = environment.getAllIn(bottomLeft, topRight).size
      coord -> density
    }
    val maxDensityValue = densityMap.maxBy(_._2)._2
    densityMap.foreach { (coord, density) =>
      val result = (1.0f - (density.toFloat / maxDensityValue)) * 0.85f - 0.15f
      renderingContext.setColor(new Color(Color.HSBtoRGB(result, 1, 0.5)))
      renderingContext.fillRect(coord.x.toInt, coord.y.toInt, precision, precision)
    }
    environment.all.foreach(boid => renderingContext.drawRect(boid.position.x.toInt, boid.position.y.toInt, 2, 2))
    frame.validate()
    frame.repaint()
  private class RenderingPanel(var backgroundImage: BufferedImage) extends JPanel:
    this.add(new JButton("bibo"))
    override def paintComponent(g: Graphics): Unit =
      super.paintComponent(g)
      g.drawImage(backgroundImage, 0, 0, this)

  override def getCurrentConfig(): ConfigurationStore.Config =
    ConfigurationStore.Config(Flocking.Weight(3, 1, 1), 100, 20)
