package it.unibo

import it.unibo.boundary.ConfigurationStore
import it.unibo.core.{Boid, Dynamics, Environment, Environments, RTreeEnvironment}
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.simulation.Simulation
import it.unibo.core.dynamics.Flocking
import it.unibo.core.dynamics.BorderForce
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
    Simulation(
      render,
      Environments.setupBoids(render.bounds, 1000),
      render,
      flockingFactory(render.bounds),
      33 milliseconds
    ).loop().runAsyncAndForget
  )

def flockingFactory(border: Rectangle2D): ConfigurationStore.Config => Dynamics =
  config =>
    Dynamics.combine(Flocking(config.flockingWeights, config.visionRange, config.separationRange), BorderForce(border))
