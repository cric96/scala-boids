package it.unibo

import it.unibo.boundary.{ConfigurationStore, Parameter}
import it.unibo.core.{Boid, Dynamics, Environment, Environments}
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.simulation.Simulation
import it.unibo.core.dynamics.{BorderForce, DynamicFactory, Flocking}
import it.unibo.render.SwingRender
import cats.effect.unsafe.implicits.global

import concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.concurrent.Future
import scala.util.Random

import javax.swing.SwingUtilities

object FlockingRendering extends App:
  given Random = Random(0)
  var cancelSimulation: () => Future[Unit] = _
  var lastCount = Parameter.BoidsCount.default.toInt
  val render = SwingRender()
  render.boidsCountStrategy = boidsCount =>
    if (boidsCount != lastCount) {
      cancelSimulation()
      lastCount = boidsCount
      cancelSimulation = createSimulation
    }
  SwingUtilities.invokeLater(() => cancelSimulation = createSimulation)

  def createSimulation = Simulation(
    render,
    Environments.setupBoids(render.bounds, lastCount),
    render,
    DynamicFactory.flocking(render.bounds),
    16 milliseconds
  ).loop().unsafeRunCancelable()
