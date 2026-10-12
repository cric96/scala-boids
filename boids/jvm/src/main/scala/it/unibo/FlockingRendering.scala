package it.unibo

import it.unibo.boundary.{ConfigurationStore, Parameter, Renderer}
import it.unibo.core.{Boid, Dynamics, Environment, Environments, GridEnvironment}
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.simulation.Simulation
import it.unibo.core.dynamics.{DynamicFactory, Flocking}
import it.unibo.render.SwingRender
import cats.effect.unsafe.implicits.global

import concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.concurrent.Future
import scala.util.Random

import javax.swing.SwingUtilities

object FlockingRendering extends App:
  given Random = Random(0)
  var cancelSimulation: () => Future[Unit] = () => Future.unit
  var lastCount = Parameter.BoidsCount.default.toInt
  val render = SwingRender()
  var lastBounds = render.bounds
  var latest: Option[Environment] = None

  /** A new population restarts the simulation, a resized window carries the flock over to it. */
  render.boidsCountStrategy = boidsCount =>
    if (boidsCount != lastCount)
      lastCount = boidsCount
      restart(Environments.setupBoids(render.bounds, boidsCount))
    else if (render.bounds != lastBounds) latest.foreach(flock => restart(GridEnvironment(flock.all, render.bounds)))
  SwingUtilities.invokeLater(() => restart(Environments.setupBoids(render.bounds, lastCount)))

  def restart(environment: Environment): Unit =
    cancelSimulation()
    lastBounds = render.bounds
    val recorded: Renderer = frame =>
      latest = Some(frame)
      render.render(frame)
    cancelSimulation = Simulation(recorded, environment, render, DynamicFactory.flocking(lastBounds), 16 milliseconds)
      .loop()
      .unsafeRunCancelable()
