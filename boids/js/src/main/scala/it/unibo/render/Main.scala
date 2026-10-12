package it.unibo.render

import it.unibo.Slider
import it.unibo.boundary.{ConfigurationStore, Parameter, Renderer}
import it.unibo.boundary.Parameter.*
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.{Boid, DensityMap, Dynamics, Environment, Environments}
import it.unibo.core.dynamics.{DynamicFactory, Flocking}
import it.unibo.core.simulation.Simulation
import it.unibo.p5.{Element, P5, P5Logic}
import it.unibo.p5.api.*
import cats.effect.unsafe.implicits.global

import concurrent.duration.DurationInt
import scala.concurrent.Future
import scala.language.postfixOps
import scala.scalajs.js.annotation.{JSExportTopLevel, JSGlobal, JSGlobalScope, JSName}
import scala.util.Random
import scalajs.js
import scalajs.js.typedarray.Uint8ClampedArray
import scala.math.atan

import java.net.URL
object Main extends App with P5Logic with Renderer with ConfigurationStore:
  // InitialValues
  var boidsCount = BoidsCount.default.toInt
  var environment: Environment = _
  var cancelSimulation: () => Future[Unit] = _
  var sliders: Map[Parameter, Slider] = Map.empty
  var showBoids: Element = _
  // Context
  given Random = Random(0)

  override def setup(): Unit =
    // gui setup
    val canvas = createCanvas(windowWidth, windowHeight)
    val bounds = boundingBox
    val (panelSliders, boidsSwitch) = createControlPanel()
    sliders = panelSliders
    showBoids = boidsSwitch
    environment = Environments.setupBoids(bounds, boidsCount)
    cancelSimulation = Simulation(this, environment, this, DynamicFactory.flocking(bounds), 33 millisecond)
      .loop()
      .unsafeRunCancelable()

    canvas.style("display", "block")
    noLoop()

  override def draw(): Unit =
    if (sliders(BoidsCount).value.toInt != boidsCount)
      boidsCount = sliders(BoidsCount).value.toInt
      cancelSimulation()
      environment = Environments.setupBoids(boundingBox, boidsCount)
      cancelSimulation = Simulation(this, environment, this, DynamicFactory.flocking(boundingBox), 33 millisecond)
        .loop()
        .unsafeRunCancelable()
    drawingContext.fillStyle = css(Palette.Background)
    drawingContext.fillRect(0, 0, width, height)
    drawDensityMap(DensityMap(boundingBox, sliders(BucketSize).value, sliders(BucketRange).value, environment))
    if (showBoids.checked()) drawBoids(environment.all)

  /** All in one path filled once, straight on the canvas: p5 calls and a fill per boid would dominate the frame. */
  private def drawBoids(boids: Seq[Boid]): Unit =
    drawingContext.beginPath()
    boids.foreach { boid =>
      val (tip, left, right) = Glyph.vertices(boid)
      drawingContext.moveTo(tip.x, tip.y)
      drawingContext.lineTo(left.x, left.y)
      drawingContext.lineTo(right.x, right.y)
      drawingContext.closePath()
    }
    drawingContext.fillStyle = css(Palette.BoidColor)
    drawingContext.fill()

  override def getCurrentConfig(): ConfigurationStore.Config = ConfigurationStore.Config.from(sliders(_).value)

  override def render(environment: Environment): Unit =
    this.environment = environment
    redraw()
  P5(this)

def boundingBox: Rectangle2D = Rectangle2D(Vector2D(0, 0), Vector2D(width, height))

/** Builds the floating control panel, returning its sliders and the "show boids" switch. */
def createControlPanel(): (Map[Parameter, Slider], Element) =
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
  val showBoids = createCheckbox("Show boids").addClass("switch").parent(footer)
  createButton("Reset").parent(footer).mousePressed(_ => sliders.values.foreach(_.reset()))
  (sliders, showBoids)

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
