package it.unibo.core.dynamics

import cats.effect.IO
import it.unibo.boundary.ConfigurationStore
import it.unibo.core.geometry.Vector2D.Vector2D
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.{Boid, Environment, Environments, UnboundedRTreeEnvironment}

import scala.util.Random

class FlockingSuite extends munit.CatsEffectSuite:
  private val bounds = Rectangle2D(Vector2D(0, 0), Vector2D(200, 200))
  private val dynamics = DynamicFactory.flocking(bounds)(ConfigurationStore.Config.from(_.default))
  private val start = Environments.setupBoids(bounds, 50)(using Random(0))

  private def run(steps: Int): IO[Environment] =
    (1 to steps).foldLeft(IO.pure(start))((world, _) => world.flatMap(dynamics))

  private def assertClose(actual: Vector2D, expected: Vector2D): Unit =
    assertEqualsDouble(actual.x, expected.x, 1e-6)
    assertEqualsDouble(actual.y, expected.y, 1e-6)

  test("a step moves every boid by its new velocity exactly once"):
    dynamics(start).map(world =>
      start.all.zip(world.all).foreach((before, after) => assertClose(after.position, before.position + after.velocity))
    )

  test("overlapping boids do not produce NaN"):
    val position = Vector2D(100, 100)
    val overlapping = UnboundedRTreeEnvironment(Seq(Boid(position, Vector2D(1, 0)), Boid(position, Vector2D(0, 1))))
    dynamics(overlapping).map(world =>
      world.all.foreach(boid => assert(!(boid.position.norm + boid.velocity.norm).isNaN, boid))
    )

  test("three steps match the recorded snapshot"):
    run(3).map { world =>
      assertClose(world.all.map(_.position).reduce(_ + _), Vector2D(5047.519176710308, 5016.583506946457))
      assertClose(world.all.map(_.velocity).reduce(_ + _), Vector2D(9.345647491290286, 6.1394733475105685))
    }
