package it.unibo

import it.unibo.boundary.ConfigurationStore
import it.unibo.core.{Boid, Environment}
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.simulation.Simulation
import it.unibo.core.RTreeEnvironment
import it.unibo.core.dynamics.Flocking
import it.unibo.render.SwingRender

import concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random
import monix.execution.Scheduler.Implicits.global

import javax.swing.SwingUtilities

object FlockingRendering extends App:
  given Random = Random(0)
  val render = new SwingRender()
  SwingUtilities.invokeLater(() =>
    val simulation = Simulation(
      render,
      setupBoids(render.bounds, 5000),
      render,
      flockingFactory,
      33 milliseconds
    ).loop().runAsyncAndForget
  )

def setupBoids(boundingBox: Rectangle2D, boidsCount: Int)(using Random): Environment =
  val centeringFactor = boundingBox.width / 10
  val center = boundingBox.center
  val delta = Vector2D(centeringFactor, centeringFactor)
  val generatorBox = Rectangle2D(center - delta, center + delta)
  val boids = Boid
    .generator(Vector2D.randomPositionIn(generatorBox), Vector2D.randomUnitary)
    .take(boidsCount)
  RTreeEnvironment(boids, boundingBox)

def flockingFactory: ConfigurationStore.Config => Flocking =
  config => Flocking(config.flockingWeights, config.visionRange, config.separationRange)
