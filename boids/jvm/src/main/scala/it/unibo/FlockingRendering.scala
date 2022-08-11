package it.unibo

import it.unibo.boundary.ConfigurationStore
import it.unibo.core.{Boid, Dynamics, Environment, Environments, BoundedRTreeEnvironment}
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.simulation.Simulation
import it.unibo.core.dynamics.{BorderForce, DynamicFactory, Flocking}
import it.unibo.render.SwingRender
import monix.execution.Cancelable

import concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random
import monix.execution.Scheduler.Implicits.global

import javax.swing.SwingUtilities

object FlockingRendering extends App:
  given Random = Random(0)
  var simulation: Cancelable = _
  var lastCount = 1000
  val render = SwingRender()
  render.boidsCountStrategy = boidsCount =>
    if (boidsCount != lastCount) {
      simulation.cancel()
      lastCount = boidsCount
      simulation = createSimulation
    }
  SwingUtilities.invokeLater(() => simulation = createSimulation)

  def createSimulation = Simulation(
    render,
    Environments.setupBoids(render.bounds, lastCount),
    render,
    DynamicFactory.flocking(render.bounds),
    16 milliseconds
  ).loop().runAsync(_ => ())
