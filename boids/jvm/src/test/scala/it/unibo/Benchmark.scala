package it.unibo

import cats.effect.unsafe.implicits.global
import it.unibo.boundary.{ConfigurationStore, Parameter}
import it.unibo.core.dynamics.DynamicFactory
import it.unibo.core.{DensityMap, Environment, Environments}

import java.util.concurrent.TimeUnit.MILLISECONDS
import scala.concurrent.duration.DurationLong
import scala.util.Random

/** Rough timings of the simulation hot paths: `sbt "boids/Test/runMain it.unibo.benchmark 2000 200"`. */
@main def benchmark(boids: Int, steps: Int): Unit =
  val bounds = Fixtures.screen
  val densityRepetitions = 10
  val dynamics = DynamicFactory.flocking(bounds)(ConfigurationStore.Config.from(_.default))
  def run(environment: Environment): Environment =
    Iterator.iterate(environment)(dynamics(_).unsafeRunSync()).drop(steps).next()
  def millisPer(repetitions: Int)(task: => Any): Double =
    val begin = System.nanoTime()
    (1 to repetitions).foreach(_ => task)
    (System.nanoTime() - begin).nanos.toUnit(MILLISECONDS) / repetitions
  val warm = run(Environments.setupBoids(bounds, boids)(using Random(0)))
  val step = millisPer(1)(run(warm)) / steps
  val densities = Seq(Parameter.BucketSize.default, Parameter.BucketSize.min).map(cell =>
    cell -> millisPer(densityRepetitions)(DensityMap(bounds, cell, Parameter.BucketRange.default, warm))
  )
  // boids within the vision range of each boid, before flocking caps them
  val neighbours =
    warm.all.map(warm.nearTo(_, Parameter.VisionRange.default, boids).size).sum / boids
  println(
    f"boids=$boids%5d  neighbours=$neighbours%4d  step=$step%7.2f ms  " +
      densities.map((cell, ms) => f"density@$cell%.0f=$ms%7.2f ms").mkString("  ")
  )
