package it.unibo.core.dynamics

import cats.effect.IO
import it.unibo.boundary.ConfigurationStore
import it.unibo.core.geometry.Vector2D.Vector2D
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.{Environment, Environments}

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

  test("three steps match the recorded snapshot"):
    run(3).map { world =>
      assertClose(world.all.map(_.position).reduce(_ + _), Vector2D(5069.44492026864, 5031.47111951853))
      assertClose(world.all.map(_.velocity).reduce(_ + _), Vector2D(9.12860044298517, 6.0932085004327465))
    }
