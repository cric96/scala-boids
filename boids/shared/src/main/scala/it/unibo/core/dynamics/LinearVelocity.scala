package it.unibo.core.dynamics

import it.unibo.core.{Dynamics, Environment}
import monocle.syntax.all._

class LinearVelocity extends Dynamics:
  override def apply(environment: Environment): Environment =
    environment.replaceBoidsWith(environment.all.map(boid => boid.focus(_.position).modify(_ + boid.velocity)))
