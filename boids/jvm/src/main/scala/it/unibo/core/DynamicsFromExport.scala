package it.unibo.core
import monix.eval.Task

class DynamicsFromExport(val data: Seq[(Int, Seq[Boid])]) extends Dynamics:
  private var localData = data
  override def apply(environment: Environment): Task[Environment] =
    if (localData.isEmpty) {
      localData = data
    }
    val current = localData.head
    localData = localData.tail
    Task(UnboundedRTreeEnvironment(current._2))
