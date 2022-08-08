package it.unibo.render

import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.core.Environment
import it.unibo.core.dynamics.Flocking
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

import java.awt.{Color, Frame, Graphics, GraphicsEnvironment}
import java.awt.image.BufferedImage
import javax.swing.{JFrame, JPanel}

/** */
class SwingRender extends Renderer with ConfigurationStore:
  private val config = GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.getDefaultConfiguration
  private val frame = new JFrame()
  frame.setExtendedState(Frame.MAXIMIZED_BOTH)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.setVisible(true)
  val image = config.createCompatibleImage(10, 10)
  private val renderingFrame = RenderingPanel(image)
  frame.setContentPane(renderingFrame)
  def bounds: Rectangle2D =
    Rectangle2D(Vector2D(0, 0), Vector2D(frame.getWidth, frame.getHeight))
  def render(environment: Environment): Unit =
    val image = config.createCompatibleImage(frame.getWidth, frame.getHeight)
    val renderingContext = image.getGraphics
    renderingContext.setColor(Color.CYAN)
    environment.all.foreach(boid => renderingContext.drawRect(boid.position.x.toInt, boid.position.y.toInt, 1, 1))
    renderingFrame.backgroundImage = image
    frame.validate()
    frame.repaint()
  //renderingFrame.invalidate()
  private class RenderingPanel(var backgroundImage: BufferedImage) extends JPanel:
    override def paintComponent(g: Graphics): Unit =
      super.paintComponent(g)
      g.drawImage(backgroundImage, 0, 0, this)

  override def getCurrentConfig(): ConfigurationStore.Config =
    ConfigurationStore.Config(Flocking.Weight(2, 1, 1), 50, 20)
