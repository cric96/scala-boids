package it.unibo.worker

import it.unibo.boundary.{ConfigurationStore, Parameter, Renderer}
import it.unibo.core.{Environment, Environments, GridEnvironment}
import it.unibo.core.geometry.Rectangle2D
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
  private var latest: Option[Environment] = None

  def main(args: Array[String]): Unit =
    js.Dynamic.global.onmessage =
      (event => receive(Protocol.decodeSettings(event.data.asInstanceOf[Float64Array]))): js.Function1[js.Dynamic, Unit]

  /** A new population restarts the simulation, a new canvas carries the flock over to it, anything else steers it. */
  private def receive(next: Protocol.Settings): Unit =
    val previous = settings
    settings = Some(next)
    val count = next.values(Parameter.BoidsCount)
    lazy val newFlock = Environments.setupBoids(next.bounds, count.toInt)
    if (!previous.exists(_.values(Parameter.BoidsCount) == count)) restart(newFlock, next.bounds)
    else if (!previous.exists(_.bounds == next.bounds))
      restart(latest.fold(newFlock)(flock => GridEnvironment(flock.all, next.bounds)), next.bounds)

  private def restart(environment: Environment, bounds: Rectangle2D): Unit =
    cancel()
    cancel = Simulation(renderer, environment, store, DynamicFactory.flocking(bounds), FramePeriod)
      .loop()
      .unsafeRunCancelable()

  private val renderer: Renderer = environment =>
    latest = Some(environment)
    val boids = Protocol.encodeBoids(environment.all)
    js.Dynamic.global.postMessage(boids, js.Array(boids.buffer))

  private val store: ConfigurationStore = () =>
    ConfigurationStore.Config.from(settings.fold((parameter: Parameter) => parameter.default)(_.values))
