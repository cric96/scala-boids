package it.unibo.core.dynamics

import it.unibo.core.{Dynamics, Environment}
import monix.eval.Task
import monocle.syntax.all.*

class LinearVelocity extends Dynamics:
  override def apply(environment: Environment): Task[Environment] =
    Task(environment.replaceBoidsWith(environment.all.map(boid => boid.focus(_.position).modify(_ + boid.velocity))))
