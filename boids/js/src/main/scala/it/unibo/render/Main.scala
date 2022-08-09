package it.unibo.render

import it.unibo.Slider
import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.{Boid, DensityEvaluation, DensityMap, Dynamics, Environment, Environments, RTreeEnvironment}
import it.unibo.core.dynamics.{Flocking, LinearVelocity, BorderForce}
import it.unibo.core.simulation.Simulation
import it.unibo.p5.{P5, P5Logic}
import it.unibo.p5.api.*
import monix.execution.Cancelable
import concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.scalajs.js.annotation.{JSExportTopLevel, JSGlobal, JSGlobalScope, JSName}
import scala.util.Random
import scalajs.js
import scala.math.atan
import monix.execution.Scheduler.Implicits.global
import org.scalajs.dom

import java.net.URL
object Main extends App with P5Logic with Renderer with ConfigurationStore:
  // Import part
  typings.p5.p5Require
  // InitialValues
  var boidsCount = 500
  var boidHeight = 10
  var environment: Environment = _
  var simulation: Cancelable = _
  var sliders: Map[String, Slider] = Map.empty
  var showBoids = false
  // Context
  given Random = Random(0)

  override def setup(): Unit =
    // gui setup
    val canvas = createCanvas(windowWidth, windowHeight)
    val bounds = boundingBox
    sliders = createSliders()
    environment = Environments.setupBoids(bounds, boidsCount)
    simulation = Simulation(this, environment, this, flockingFactory, 33 millisecond)
      .loop()
      .runAsync { _ => }

    val hideBoids = createButton("show boids");
    hideBoids.position(10, 10);
    hideBoids.mousePressed(_ => showBoids = !showBoids)
    canvas.style("display", "block")
    noLoop()

  override def draw(): Unit =
    if (sliders("boidsCount").value.toInt != boidsCount)
      boidsCount = sliders("boidsCount").value.toInt
      simulation.cancel()
      environment = Environments.setupBoids(boundingBox, boidsCount)
      simulation = Simulation(this, environment, this, flockingFactory, 33 millisecond)
        .loop()
        .runAsync { _ => }
    background(255)
    colorMode(HSB)
    drawDensityMap(boundingBox, sliders("bucketSize").value, sliders("bucketRange").value, environment)
    sliders.values.foreach(_.render())
    if (showBoids)
      environment.all.foreach(boid =>
        val delta = boidHeight / 2
        val (x, y) = (boid.position.x, boid.position.y)
        val angle = math.atan2(boid.velocity.y, boid.velocity.x)
        push()
        translate(x, y)
        rotate(angle + HALF_PI)
        triangle(0, 0 - boidHeight, 0 + delta, 0 + delta, 0 - delta, 0 + delta)
        pop()
      )

  override def getCurrentConfig(): ConfigurationStore.Config =
    val flocks = Flocking.Weight(sliders("separation").value, sliders("align").value, sliders("cohesion").value)
    ConfigurationStore.Config(flocks, sliders("visionRange").value, sliders("separationRange").value)

  override def render(environment: Environment): Unit =
    this.environment = environment
    redraw()
  P5(this)

  def flockingFactory: ConfigurationStore.Config => Dynamics =
    config =>
      Dynamics.combine(
        Flocking(config.flockingWeights, config.visionRange, config.separationRange, sliders("force").value),
        BorderForce(boundingBox)
      )

def boundingBox: Rectangle2D = Rectangle2D(Vector2D(0, 0), Vector2D(width, height))

def createSliders(): Map[String, Slider] =
  Map(
    "separation" -> Slider("separation", 0, 3, 2, (10, 30)),
    "align" -> Slider("align", 0, 3, 1, (10, 50)),
    "cohesion" -> Slider("cohesion", 0, 3, 1, (10, 70)),
    "visionRange" -> Slider("visionRange", 20, 200, 50, (10, 90)),
    "separationRange" -> Slider("separationRange", 5, 100, 10, (10, 110)),
    "bucketSize" -> Slider("bucketDefinition", 10, 40, 20, (10, 130), false),
    "bucketRange" -> Slider("bucketRange", 20, 200, 100, (10, 150)),
    "boidsCount" -> Slider("boids", 100, 1000, 400, (10, 170), false),
    "force" -> Slider("maxForce", 0.01, 0.1, 0.03, (10, 190))
  )

def drawDensityMap(
    boundingBox: Rectangle2D,
    precision: Double,
    visionRange: Double,
    environment: Environment
): Unit =
  val densityMap = DensityMap(boundingBox, precision, visionRange, environment)
  push()
  noStroke()
  densityMap.foreach { (coord, density) =>
    colorFromDensity(density)
    rect(coord.x, coord.y, precision, precision)
  }
  pop()

def colorFromDensity(density: Double): Unit =
  fill(((1.0f - density) * 0.85f - 0.15f) * 360, 127, 255, 1)
