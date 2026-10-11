package it.unibo.core
import cats.effect.IO

class DynamicsFromExport(val data: Seq[(Int, Seq[Boid])]) extends Dynamics:
  private var localData = data
  override def apply(environment: Environment): IO[Environment] =
    if (localData.isEmpty) {
      localData = data
    }
    val current = localData.head
    localData = localData.tail
    IO(environment.replaceBoidsWith(current._2))
