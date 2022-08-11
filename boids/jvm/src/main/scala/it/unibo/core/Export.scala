package it.unibo.core

import it.unibo.boundary.{ConfigurationStore, Renderer}
import it.unibo.core.dynamics.Flocking
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.simulation.Simulation
import org.rogach.scallop.*
import it.unibo.core.dynamics.DynamicFactory
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random
import upickle.default.*

class Configuration(arguments: Seq[String]) extends ScallopConf(arguments):
  val boids: ScallopOption[Int] = opt[Int](required = true)
  val maxForce: ScallopOption[Double] = opt[Double](default = Some(0.03))
  val separation: ScallopOption[Double] = opt[Double](required = true)
  val align: ScallopOption[Double] = opt[Double](required = true)
  val cohesion: ScallopOption[Double] = opt[Double](required = true)
  val separationRange: ScallopOption[Double] = opt[Double](required = true)
  val vision: ScallopOption[Double] = opt[Double](required = true)
  val steps: ScallopOption[Int] = opt[Int](default = Some(1000))
  val width: ScallopOption[Int] = opt[Int](required = true)
  val height: ScallopOption[Int] = opt[Int](required = true)
  val seeds: ScallopOption[Int] = opt[Int](required = true)
  verify()
  def extractSimulationConfiguration: ConfigurationStore.Config = ConfigurationStore.Config(
    Flocking.Weight(separation(), align(), cohesion()),
    vision(),
    separationRange(),
    maxForce()
  )
class Export(seed: Int) extends Renderer:
  var frameCount = 0
  val allFrame: mutable.ListBuffer[(Int, Seq[Boid])] = ListBuffer.empty
  override def render(environment: Environment): Unit =
    allFrame.addOne(frameCount -> environment.all)
    frameCount += 1
  def close(): Unit =
    os.write(os.pwd / "res" / seed.toString, writeBinary(allFrame))

class SingleStore(config: Configuration) extends ConfigurationStore:
  import config._
  override def getCurrentConfig(): ConfigurationStore.Config = extractSimulationConfiguration
@main def main(args: String*): Unit =
  os.makeDir.all(os.pwd / "res")
  val configuration = Configuration(args)
  import configuration._
  val bounds = Rectangle2D(Vector2D(0, 0), Vector2D(width(), height()))
  import configuration._
  val simulations = (0 to seeds())
    .map { seed =>
      val render = Export(seed)
      val environment =
        Environments.setupBoids(bounds, boids())(using Random(seed))
      render -> simulation.Simulation(render, environment, SingleStore(configuration), DynamicFactory.flocking(bounds))
    }
    .map { case (render, simulation) => simulation.loopFor(steps()).map(_ => render.close()) }

  val parallelExecution = Task.parSequence(simulations)
  val timeBefore = System.currentTimeMillis()
  parallelExecution.runSyncUnsafe()
