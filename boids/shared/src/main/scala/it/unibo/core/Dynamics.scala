package it.unibo.core

import cats.effect.IO

trait Dynamics extends (Environment => IO[Environment])

object Dynamics:
  def combine(dynamics: Dynamics*): Dynamics =
    (environment: Environment) => dynamics.foldLeft(IO.pure(environment))((env, dynamic) => env.flatMap(dynamic))
