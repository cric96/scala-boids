package it.unibo.core

import monix.eval.Task

trait Dynamics extends (Environment => Task[Environment])

object Dynamics:
  def combine(dynamics: Dynamics*): Dynamics =
    (environment: Environment) => dynamics.foldLeft(Task(environment))((env, dynamic) => env.flatMap(dynamic))
