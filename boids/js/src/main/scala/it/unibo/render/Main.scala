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
import scala.math.atan

import java.net.URL
object Main extends App with P5Logic with Renderer with ConfigurationStore:
  // InitialValues
  var boidsCount = BoidsCount.default.toInt
  var boidHeight = 10
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
    background(255)
    drawDensityMap(boundingBox, sliders(BucketSize).value, sliders(BucketRange).value, environment)
    if (showBoids.checked()) drawBoids(environment.all)

  /** Straight on the canvas with the corners computed here: five p5 calls per boid would dominate the frame. */
  private def drawBoids(boids: Seq[Boid]): Unit =
    val half = boidHeight / 2.0
    drawingContext.fillStyle = "white"
    drawingContext.strokeStyle = "black"
    boids.foreach { boid =>
      val forward = boid.velocity.normalize
      val side = Vector2D(-forward.y, forward.x)
      val base = boid.position - forward * half
      val corners = Seq(boid.position + forward * boidHeight, base + side * half, base - side * half)
      drawingContext.beginPath()
      drawingContext.moveTo(corners.head.x, corners.head.y)
      corners.tail.foreach(corner => drawingContext.lineTo(corner.x, corner.y))
      drawingContext.closePath()
      drawingContext.fill()
      drawingContext.stroke()
    }

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

def drawDensityMap(
    boundingBox: Rectangle2D,
    precision: Double,
    visionRange: Double,
    environment: Environment
): Unit =
  DensityMap(boundingBox, precision, visionRange, environment).foreach { (coord, density) =>
    drawingContext.fillStyle = colorFromDensity(density)
    drawingContext.fillRect(coord.x, coord.y, precision, precision)
  }

/** Straight on the canvas, as p5 calls cost more than the drawing; negative hues are clamped to red like p5 did. */
def colorFromDensity(density: Double): String =
  s"hsl(${(((1.0f - density) * 0.85f - 0.15f) * 360).max(0)}, 100%, 50%)"
