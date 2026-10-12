package it.unibo.render

import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.boundary.Parameter.*
import it.unibo.core.{Boid, DensityMap, Environment}
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
  RenderingHints,
  geom
}
import java.awt.image.BufferedImage
import javax.swing.{BoxLayout, JButton, JComponent, JFrame, JLabel, JPanel, JSlider, WindowConstants}
import java.awt.geom.{AffineTransform, Path2D}
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
    val graphics = renderingContext.asInstanceOf[Graphics2D]
    graphics.setColor(Color(Palette.Background))
    graphics.fillRect(0, 0, frame.getWidth, frame.getHeight)
    boidsCountStrategy(renderingFrame.parametersOf(BoidsCount).toInt)
    val precision = renderingFrame.parametersOf(BucketSize)
    drawDensityMap(graphics, DensityMap(bounds, precision, renderingFrame.parametersOf(BucketRange), environment))
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    if (renderingFrame.boidsEnabled) drawBoids(graphics, environment.all)
    renderingFrame.backgroundImage = Some(image)
    frame.validate()
    frame.repaint()

  /** All in one path filled once. */
  private def drawBoids(graphics: Graphics2D, boids: Seq[Boid]): Unit =
    val path = Path2D.Double()
    boids.foreach { boid =>
      val (tip, left, right) = Glyph.vertices(boid)
      path.moveTo(tip.x, tip.y)
      path.lineTo(left.x, left.y)
      path.lineTo(right.x, right.y)
      path.closePath()
    }
    graphics.setColor(Color(Palette.BoidColor))
    graphics.fill(path)

  /** One pixel per cell stretched over the frame, which smooths it. */
  private def drawDensityMap(graphics: Graphics2D, densityMap: DensityMap): Unit =
    val heatmap = BufferedImage(densityMap.columns, densityMap.rows, BufferedImage.TYPE_INT_RGB)
    heatmap.setRGB(
      0,
      0,
      densityMap.columns,
      densityMap.rows,
      Palette.heatmap(densityMap).toArray,
      0,
      densityMap.columns
    )
    val stretch = AffineTransform.getTranslateInstance(densityMap.origin.x, densityMap.origin.y)
    stretch.scale(densityMap.cell, densityMap.cell)
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    graphics.drawImage(heatmap, stretch, null)

  override def getCurrentConfig(): ConfigurationStore.Config =
    ConfigurationStore.Config.from(renderingFrame.parametersOf)
