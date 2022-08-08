package it.unibo.core

import monix.eval.Task

trait Dynamics extends (Environment => Task[Environment])
