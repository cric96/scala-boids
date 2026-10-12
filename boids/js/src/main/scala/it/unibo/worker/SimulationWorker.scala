package it.unibo.worker

import it.unibo.boundary.{ConfigurationStore, Parameter, Renderer}
import it.unibo.core.Environments
import it.unibo.core.dynamics.DynamicFactory
import it.unibo.core.simulation.Simulation
import cats.effect.unsafe.implicits.global

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float64Array
import scala.util.Random

/** Steps the simulation off the thread of the page: it receives the settings and answers with the boids of each step. */
object SimulationWorker:
  /** The pace the boids have always moved at on the web. */
  private val FramePeriod = 33.milliseconds
  private given Random = Random(0)
  private var settings: Option[Protocol.Settings] = None
  private var cancel: () => Future[Unit] = () => Future.unit

  def main(args: Array[String]): Unit =
    js.Dynamic.global.onmessage =
      (event => receive(Protocol.decodeSettings(event.data.asInstanceOf[Float64Array]))): js.Function1[js.Dynamic, Unit]

  /** A new population or canvas restarts the simulation, any other change only steers it. */
  private def receive(next: Protocol.Settings): Unit =
    val restart = !settings.exists(current =>
      current.bounds == next.bounds && current.values(Parameter.BoidsCount) == next.values(Parameter.BoidsCount)
    )
    settings = Some(next)
    if (restart)
      cancel()
      cancel = Simulation(
        renderer,
        Environments.setupBoids(next.bounds, next.values(Parameter.BoidsCount).toInt),
        store,
        DynamicFactory.flocking(next.bounds),
        FramePeriod
      ).loop().unsafeRunCancelable()

  private val renderer: Renderer = environment =>
    val boids = Protocol.encodeBoids(environment.all)
    js.Dynamic.global.postMessage(boids, js.Array(boids.buffer))

  private val store: ConfigurationStore = () =>
    ConfigurationStore.Config.from(settings.fold((parameter: Parameter) => parameter.default)(_.values))
