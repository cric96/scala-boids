package it.unibo.core.simulation

import it.unibo.core.{Boid, Dynamics, Environment}
import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.core.dynamics.Flocking
import monix.eval.Task

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

case class Simulation(
    render: Renderer,
    environment: Environment,
    store: ConfigurationStore,
    dynamicsFactory: ConfigurationStore.Config => Dynamics,
    deltaTime: FiniteDuration
):
  def loop(): Task[Unit] = for {
    newWorld <- this.step()
    _ <- Task.sleep(deltaTime)
    _ <- this.copy(environment = newWorld).loop()
  } yield ()

  private def step(): Task[Environment] = Task {
    val flockConfig = store.getCurrentConfig()
    val flockingBehaviour = dynamicsFactory(flockConfig)
    val newWorld = flockingBehaviour(environment)
    render.render(newWorld)
    newWorld
  }
