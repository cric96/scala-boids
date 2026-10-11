package it.unibo.core.dynamics

import it.unibo.core.geometry.Rectangle2D
import it.unibo.core.{Dynamics, Environment}
import cats.effect.IO
import monocle.syntax.all.*

/** Moves every boid by its velocity, within `bound` as on a torus: leaving from one side enters from the opposite. */
class LinearVelocity(bound: Rectangle2D) extends Dynamics:
  override def apply(environment: Environment): IO[Environment] =
    IO(
      environment.replaceBoidsWith(
        environment.all.map(boid => boid.focus(_.position).modify(position => bound.wrap(position + boid.velocity)))
      )
    )
