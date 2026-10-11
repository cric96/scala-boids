package it.unibo.render

import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.boundary.Parameter.*
import it.unibo.core.{DensityEvaluation, DensityMap, Environment}
import it.unibo.core.dynamics.Flocking
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

import java.awt.{
  BorderLayout,
  Color,
  Dimension,
  FlowLayout,
  Frame,
  Graphics,
  Graphics2D,
  GraphicsEnvironment,
  GridLayout,
  Rectangle,
  geom
}
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import javax.swing.{BoxLayout, JButton, JComponent, JFrame, JLabel, JPanel, JSlider, WindowConstants}
import java.awt.geom.AffineTransform
import scala.collection.immutable.{ListMap, TreeMap}

/** */
class SwingRender(control: Boolean = true, var boidsCountStrategy: Int => Unit = _ => ())
    extends Renderer
    with ConfigurationStore:
  Theme.install()
  private val config = GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.getDefaultConfiguration
  private val frame = new JFrame()
  frame.setExtendedState(Frame.MAXIMIZED_BOTH)
  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  frame.setVisible(true)
  private val renderingFrame = RenderingPanel(control, None)
  frame.setContentPane(renderingFrame)

  def bounds: Rectangle2D =
    Rectangle2D(Vector2D(0, 0), Vector2D(frame.getWidth, frame.getHeight))

  def render(environment: Environment): Unit =
    val image = config.createCompatibleImage(frame.getWidth, frame.getHeight)
    val renderingContext = image.getGraphics
    renderingContext.setColor(Color.white)
    renderingContext.fillRect(0, 0, frame.getWidth, frame.getHeight)
    boidsCountStrategy(renderingFrame.parametersOf(BoidsCount).toInt)
    val precision = renderingFrame.parametersOf(BucketSize)
    renderingFrame.backgroundImage = Some(image)
    val densityMap = DensityMap(bounds, precision.toInt, renderingFrame.parametersOf(BucketRange), environment)
    densityMap.foreach { (coord, density) =>
      val result = (1.0f - density) * 0.85f - 0.15f
      renderingContext.setColor(new Color(Color.HSBtoRGB(result.toFloat, 1, 0.5)))
      renderingContext.fillRect(coord.x.toInt, coord.y.toInt, precision.toInt, precision.toInt)
    }
    environment.all.foreach(boid =>
      renderingContext.setColor(new Color(1, 1, 1, 0.5f))
      val transform = AffineTransform.getTranslateInstance(boid.position.x, boid.position.y)
      transform.rotate(math.atan2(boid.velocity.y, boid.velocity.x) + Math.PI / 2)
      val shape = Ellipse2D.Double(0, 0, 5, 10)
      if (renderingFrame.boidsEnabled)
        renderingContext.asInstanceOf[Graphics2D].fill(transform.createTransformedShape(shape))
    )
    frame.validate()
    frame.repaint()

  override def getCurrentConfig(): ConfigurationStore.Config =
    ConfigurationStore.Config.from(renderingFrame.parametersOf)
