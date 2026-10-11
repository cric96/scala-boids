package it.unibo.core.dynamics

import it.unibo.core.{Dynamics, Environment}
import cats.effect.IO
import monocle.syntax.all.*

class LinearVelocity extends Dynamics:
  override def apply(environment: Environment): IO[Environment] =
    IO(environment.replaceBoidsWith(environment.all.map(boid => boid.focus(_.position).modify(_ + boid.velocity))))
