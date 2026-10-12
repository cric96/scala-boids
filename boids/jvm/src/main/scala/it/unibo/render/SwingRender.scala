package it.unibo.render

import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.boundary.Parameter.*
import it.unibo.core.{DensityMap, Environment}
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
  Polygon,
  Rectangle,
  RenderingHints,
  geom
}
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
  // Same triangle as the web version (`Main.draw`): tip ahead of the boid, white with a black outline
  private val boidShape = Polygon(Array(0, 5, -5), Array(-10, 5, 5), 3)

  def bounds: Rectangle2D =
    Rectangle2D(Vector2D(0, 0), Vector2D(frame.getWidth, frame.getHeight))

  def render(environment: Environment): Unit =
    val image = config.createCompatibleImage(frame.getWidth, frame.getHeight)
    val renderingContext = image.getGraphics
    renderingContext.setColor(Color.white)
    renderingContext.fillRect(0, 0, frame.getWidth, frame.getHeight)
    boidsCountStrategy(renderingFrame.parametersOf(BoidsCount).toInt)
    val precision = renderingFrame.parametersOf(BucketSize)
    val densityMap = DensityMap(bounds, precision.toInt, renderingFrame.parametersOf(BucketRange), environment)
    densityMap.cells.foreach { (column, row) =>
      val centre = densityMap.centre(column, row)
      val result = (1.0f - densityMap.density(column, row)) * 0.85f - 0.15f
      renderingContext.setColor(new Color(Color.HSBtoRGB(result.toFloat, 1, 0.5)))
      renderingContext.fillRect(
        (centre.x - precision / 2).toInt,
        (centre.y - precision / 2).toInt,
        precision.toInt,
        precision.toInt
      )
    }
    val graphics = renderingContext.asInstanceOf[Graphics2D]
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    if (renderingFrame.boidsEnabled)
      environment.all.foreach(boid =>
        val transform = AffineTransform.getTranslateInstance(boid.position.x, boid.position.y)
        transform.rotate(math.atan2(boid.velocity.y, boid.velocity.x) + Math.PI / 2)
        val triangle = transform.createTransformedShape(boidShape)
        graphics.setColor(Color.white)
        graphics.fill(triangle)
        graphics.setColor(Color.black)
        graphics.draw(triangle)
      )
    renderingFrame.backgroundImage = Some(image)
    frame.validate()
    frame.repaint()

  override def getCurrentConfig(): ConfigurationStore.Config =
    ConfigurationStore.Config.from(renderingFrame.parametersOf)
