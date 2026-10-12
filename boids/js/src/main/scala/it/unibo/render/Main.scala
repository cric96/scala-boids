package it.unibo.render

import it.unibo.Slider
import it.unibo.boundary.Parameter
import it.unibo.boundary.Parameter.*
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.geometry.Vector2D.Vector2D
import it.unibo.core.{DensityMap, Environment, GridEnvironment}
import it.unibo.p5.{Element, P5, P5Logic}
import it.unibo.p5.api.*
import it.unibo.worker.Protocol

import scala.compiletime.uninitialized
import scalajs.js
import scalajs.js.typedarray.{Float64Array, Uint8ClampedArray}

/** Draws the boids that the simulation worker sends, as often as the browser repaints. */
object Main extends App with P5Logic:
  var sliders: Map[Parameter, Slider] = Map.empty
  var showBoids: Element = uninitialized
  var threeD: Element = uninitialized
  var reliefView: ReliefView = uninitialized
  var simulation: js.Dynamic = uninitialized

  /** The boids of the last step of the simulation, until they are drawn. */
  var latest: Option[Float64Array] = None

  override def setup(): Unit =
    // a block straight away: inline, the canvas overflows the page, whose scrollbars shrink the window for the relief
    createCanvas(windowWidth, windowHeight).style("display", "block")
    val (panelSliders, boidsSwitch, reliefSwitch) = createControlPanel()
    sliders = panelSliders
    showBoids = boidsSwitch
    threeD = reliefSwitch
    reliefView = ReliefView(createDiv().addClass("relief"))
    simulation = js.Dynamic.global.simulationWorker
    simulation.onmessage =
      (event => latest = Some(event.data.asInstanceOf[Float64Array])): js.Function1[js.Dynamic, Unit]

  override def windowResized(): Unit = resizeCanvas(windowWidth, windowHeight)

  override def draw(): Unit =
    reliefView.show(threeD.checked())
    simulation.postMessage(
      Protocol.encodeSettings(Protocol.Settings(sliders.view.mapValues(_.value).toMap, boundingBox))
    )
    latest.foreach { boids =>
      latest = None
      drawFrame(GridEnvironment(Protocol.decodeBoids(boids), boundingBox))
    }

  private def drawFrame(environment: Environment): Unit =
    val densityMap = DensityMap(boundingBox, sliders(BucketSize).value, sliders(BucketRange).value, environment)
    if (threeD.checked()) reliefView.update(Relief(densityMap)) else drawMap(environment, densityMap)

  private def drawMap(environment: Environment, densityMap: DensityMap): Unit =
    drawingContext.fillStyle = css(Palette.Background)
    drawingContext.fillRect(0, 0, width, height)
    drawDensityMap(densityMap)
    if (showBoids.checked())
      environment.all
        .grouped(GlyphsPerPath)
        .foreach(boids => fill(boids.map(Glyph.vertices(_).toList), Palette.BoidColor))
    fill(Blobs(densityMap, sliders(BlobThreshold).value), Palette.BlobColor, Palette.BlobOpacity)

  /** Canvas fills slow down faster than the paths grow: 10000 glyphs take 30 ms a hundred at a time, 200 ms at once. */
  private val GlyphsPerPath = 100

  /** All in one path filled once, straight on the canvas, so that the squares of a blob join without seams. */
  private def fill(polygons: Iterable[Seq[Vector2D]], rgb: Int, opacity: Double = 1): Unit =
    drawingContext.beginPath()
    polygons.foreach { polygon =>
      drawingContext.moveTo(polygon.head.x, polygon.head.y)
      polygon.tail.foreach(point => drawingContext.lineTo(point.x, point.y))
      drawingContext.closePath()
    }
    drawingContext.fillStyle = css(rgb)
    drawingContext.globalAlpha = opacity
    drawingContext.fill()
    drawingContext.globalAlpha = 1

  P5(this)

def boundingBox: Rectangle2D = Rectangle2D(Vector2D(0, 0), Vector2D(width, height))

/** Builds the floating control panel, returning its sliders and the "show boids" and "3D" switches. */
def createControlPanel(): (Map[Parameter, Slider], Element, Element) =
  val panel = createDiv().addClass("panel")
  val header = createDiv("<h1>Boids</h1>").addClass("panel-header").parent(panel)
  createButton(chevron)
    .addClass("collapse")
    .attribute("aria-label", "Toggle controls")
    .parent(header)
    .mousePressed(_ => panel.toggleClass("collapsed"))
  val content = createDiv().parent(createDiv().addClass("panel-body").parent(panel))
  val sliders = Parameter.sections.flatMap { (section, parameters) =>
    val group = createDiv(s"<h2>$section</h2>").addClass("section").parent(content)
    parameters.map(parameter => parameter -> Slider(parameter, group))
  }.toMap
  val footer = createDiv().addClass("panel-footer").parent(content)
  val switches = createDiv().addClass("switches").parent(footer)
  val showBoids = createCheckbox("Show boids").addClass("switch").parent(switches)
  val threeD = createCheckbox("3D").addClass("switch").parent(switches)
  createButton("Reset").parent(footer).mousePressed(_ => sliders.values.foreach(_.reset()))
  (sliders, showBoids, threeD)

private val chevron =
  """<svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">""" +
    """<path d="M4 10l4-4 4 4"/></svg>"""

/** One pixel per cell stretched over the canvas, which smooths it. */
def drawDensityMap(densityMap: DensityMap): Unit =
  val heatmap = document.createElement("canvas")
  heatmap.width = densityMap.columns
  heatmap.height = densityMap.rows
  val context = heatmap.getContext("2d")
  val image = context.createImageData(densityMap.columns, densityMap.rows)
  val pixels = image.data.asInstanceOf[Uint8ClampedArray]
  Palette.heatmap(densityMap).zipWithIndex.foreach { (rgb, pixel) =>
    val (red, green, blue) = Palette.channels(rgb)
    Seq(red, green, blue, Palette.ChannelMax).zipWithIndex
      .foreach((value, channel) => pixels(pixel * RgbaChannels + channel) = value.toShort)
  }
  context.putImageData(image, 0, 0)
  val (origin, cell) = (densityMap.origin, densityMap.cell)
  drawingContext.drawImage(heatmap, origin.x, origin.y, densityMap.columns * cell, densityMap.rows * cell)

/** Red, green, blue and alpha: the bytes of a pixel in a canvas image. */
private val RgbaChannels = 4

def css(rgb: Int): String = f"#$rgb%06x"
