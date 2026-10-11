package it.unibo

import cats.effect.unsafe.implicits.global
import it.unibo.boundary.{ConfigurationStore, Parameter}
import it.unibo.core.dynamics.DynamicFactory
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.{DensityMap, Environment, Environments}

import scala.util.Random

/** Rough timings of the simulation hot paths: `sbt "boids/Test/runMain it.unibo.benchmark 2000 200"`. */
@main def benchmark(boids: Int, steps: Int): Unit =
  val bounds = Rectangle2D(Vector2D(0, 0), Vector2D(1920, 1080))
  val dynamics = DynamicFactory.flocking(bounds)(ConfigurationStore.Config.from(_.default))
  def run(environment: Environment): Environment =
    Iterator.iterate(environment)(dynamics(_).unsafeRunSync()).drop(steps).next()
  def millisPer(repetitions: Int)(task: => Any): Double =
    val begin = System.nanoTime()
    (1 to repetitions).foreach(_ => task)
    (System.nanoTime() - begin) / 1e6 / repetitions
  val warm = run(Environments.setupBoids(bounds, boids)(using Random(0)))
  val step = millisPer(1)(run(warm)) / steps
  val densities = Seq(20, 5).map(cell => cell -> millisPer(10)(DensityMap(bounds, cell, 100, warm)))
  val neighbours = warm.all.map(warm.nearTo(_, Parameter.VisionRange.default).size).sum / boids
  println(
    f"boids=$boids%5d  neighbours=$neighbours%4d  step=$step%7.2f ms  " +
      densities.map((cell, ms) => f"density@$cell=$ms%7.2f ms").mkString("  ")
  )
