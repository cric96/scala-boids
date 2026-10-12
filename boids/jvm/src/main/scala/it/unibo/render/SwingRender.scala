package it.unibo.render

import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.boundary.Parameter.*
import it.unibo.core.{Boid, DensityMap, Environment}
import it.unibo.core.dynamics.Flocking
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.geometry.Vector2D.Vector2D

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
    val densityMap = DensityMap(bounds, precision, renderingFrame.parametersOf(BucketRange), environment)
    drawDensityMap(graphics, densityMap)
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    if (renderingFrame.boidsEnabled)
      fill(graphics, environment.all.view.map(Glyph.vertices(_).toList), Color(Palette.BoidColor))
    val (red, green, blue) = Palette.channels(Palette.BlobColor)
    val blobColor = Color(red, green, blue, (Palette.BlobOpacity * Palette.ChannelMax).round.toInt)
    fill(graphics, Blobs(densityMap, renderingFrame.parametersOf(BlobThreshold)), blobColor)
    renderingFrame.backgroundImage = Some(image)
    frame.validate()
    frame.repaint()

  /** All in one path filled once, so that the squares of a blob join without seams. */
  private def fill(graphics: Graphics2D, polygons: Iterable[Seq[Vector2D]], color: Color): Unit =
    val path = Path2D.Double()
    polygons.foreach { polygon =>
      path.moveTo(polygon.head.x, polygon.head.y)
      polygon.tail.foreach(point => path.lineTo(point.x, point.y))
      path.closePath()
    }
    graphics.setColor(color)
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
