package it.unibo.core.simulation

import it.unibo.core.{Boid, Dynamics, Environment}
import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.core.dynamics.Flocking
import monix.eval.Task

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

import concurrent.duration.DurationInt

case class Simulation(
    render: Renderer,
    environment: Environment,
    store: ConfigurationStore,
    dynamicsFactory: ConfigurationStore.Config => Dynamics,
    deltaTime: FiniteDuration = 0 milliseconds
):
  def loop(): Task[Unit] = for {
    newWorld <- this.step()
    _ <-
      if (deltaTime > (0 milliseconds)) { Task.sleep(deltaTime) }
      else { Task {} }
    _ <- this.copy(environment = newWorld).loop()
  } yield ()
  private def step(): Task[Environment] = Task
    .defer {
      val flockConfig = store.getCurrentConfig()
      val flockingBehaviour = dynamicsFactory(flockConfig)
      flockingBehaviour(environment)
    }
    .map(newWorld =>
      render.render(newWorld)
      newWorld
    )
  def loopFor(steps: Int): Task[Unit] =
    if (steps == 0) {
      Task {}
    } else {
      this.step().flatMap(newEnv => this.copy(environment = newEnv).loopFor(steps - 1))
    }
