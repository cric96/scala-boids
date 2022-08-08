package it.unibo.render

import it.unibo.Slider
import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.{Boid, DensityEvaluation, Environment, RTreeEnvironment}
import it.unibo.core.dynamics.{Flocking, LinearVelocity}
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
  // InitialValues
  var boidsCount = 500
  var boidHeight = 10
  var environment: Environment = _
  var simulation: Cancelable = _
  var sliders: Map[String, Slider] = Map.empty
  // Context
  given Random = Random(0)

  override def setup(): Unit =
    // gui setup
    val canvas = createCanvas(windowWidth, windowHeight)
    val bounds = boundingBox
    sliders = createSliders()
    environment = setupBoids(bounds, boidsCount)
    simulation = Simulation(this, environment, this, flockingFactory, 33 millisecond)
      .loop()
      .runAsync { _ => }
    canvas.style("display", "block")
    noLoop()

  override def draw(): Unit =
    if (sliders("boidsCount").value.toInt != boidsCount)
      boidsCount = sliders("boidsCount").value.toInt
      simulation.cancel()
      environment = setupBoids(boundingBox, boidsCount)
      simulation = Simulation(this, environment, this, flockingFactory, 33 millisecond)
        .loop()
        .runAsync { _ => }
    background(255)
    colorMode(HSB)
    val densityMap = DensityEvaluation.fromEnvironment(environment, sliders("precision").value, boundingBox)
    drawDensityMap(boundingBox, sliders("precision").value, densityMap)
    sliders.values.foreach(_.render())
    environment.all.foreach(boid =>
      val delta = boidHeight / 2
      val (x, y) = (boid.position.x, boid.position.y)
      val angle = math.atan2(boid.velocity.y, boid.velocity.x)
      colorFromDensity(densityMap(boid.position))
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

def flockingFactory: ConfigurationStore.Config => Flocking =
  config => Flocking(config.flockingWeights, config.visionRange, config.separationRange)

def setupBoids(boundingBox: Rectangle2D, boidsCount: Int)(using Random): Environment =
  val centeringFactor = boundingBox.width / 10
  val center = boundingBox.center
  val delta = Vector2D(centeringFactor, centeringFactor)
  val generatorBox = Rectangle2D(center - delta, center + delta)
  val boids = Boid
    .generator(Vector2D.randomPositionIn(generatorBox), Vector2D.randomUnitary)
    .take(boidsCount)
  RTreeEnvironment(boids, boundingBox)

def boundingBox: Rectangle2D = Rectangle2D(Vector2D(0, 0), Vector2D(width, height))

def createSliders(): Map[String, Slider] =
  Map(
    "separation" -> Slider("separation", 0, 3, 2, (10, 10)),
    "align" -> Slider("align", 0, 3, 1, (10, 30)),
    "cohesion" -> Slider("cohesion", 0, 3, 1, (10, 50)),
    "visionRange" -> Slider("visionRange", 20, 200, 50, (10, 70)),
    "separationRange" -> Slider("separationRange", 5, 100, 10, (10, 90)),
    "precision" -> Slider("precision", 20, 400, 40, (10, 110)),
    "boidsCount" -> Slider("boids", 100, 1000, 400, (10, 130), false)
  )

def drawDensityMap(boundingBox: Rectangle2D, precision: Double, densityEvaluation: DensityEvaluation): Unit =
  val startX = boundingBox.bottomLeft.x
  val startY = boundingBox.bottomLeft.y
  def iterateUntil(start: Double, end: Double): LazyList[Double] =
    LazyList.iterate(start)(_ + precision).takeWhile(_ < end)
  val allCoords = for {
    x <- iterateUntil(startX, boundingBox.topRight.x)
    y <- iterateUntil(startY, boundingBox.topRight.y)
  } yield (Vector2D(x, y))
  push()
  noStroke()
  allCoords.foreach { bottom =>
    val density = densityEvaluation(Vector2D(bottom.x + precision / 2, bottom.y + precision / 2))
    colorFromDensity(density)
    rect(bottom.x, bottom.y, precision, precision)
  }
  pop()

def colorFromDensity(density: Double): Unit =
  fill(0, 127, density, 0.5)
