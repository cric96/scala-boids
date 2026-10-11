package it.unibo.core.simulation

import it.unibo.core.{Boid, Dynamics, Environment}
import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.core.dynamics.Flocking
import cats.effect.IO

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.postfixOps

import concurrent.duration.DurationInt

case class Simulation(
    render: Renderer,
    environment: Environment,
    store: ConfigurationStore,
    dynamicsFactory: ConfigurationStore.Config => Dynamics,
    framePeriod: FiniteDuration = 0 milliseconds
):
  /** Steps and renders forever, sleeping only what each step leaves of the frame period. */
  def loop(): IO[Unit] = for {
    (elapsed, newWorld) <- this.step().timed
    _ <- IO.sleep((framePeriod - elapsed).max(Duration.Zero))
    _ <- this.copy(environment = newWorld).loop()
  } yield ()
  private def step(): IO[Environment] = IO
    .defer {
      val flockConfig = store.getCurrentConfig()
      val flockingBehaviour = dynamicsFactory(flockConfig)
      flockingBehaviour(environment)
    }
    .map(newWorld =>
      render.render(newWorld)
      newWorld
    )
  def loopFor(steps: Int): IO[Unit] =
    if (steps == 0) {
      IO.unit
    } else {
      this.step().flatMap(newEnv => this.copy(environment = newEnv).loopFor(steps - 1))
    }
