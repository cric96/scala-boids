package it.unibo.core.geometry

import it.unibo.Fixtures.{samples, screen, tolerance}
import it.unibo.core.geometry.Vector2D.*

import scala.util.Random

class Rectangle2DSuite extends munit.CatsEffectSuite:
  private given Random = Random(0)
  // points up to a whole screen away from it, on every side
  private val around = Rectangle2D(screen.bottomLeft - screen.topRight, screen.topRight * 2)
  private val points = List.fill(samples)(Vector2D.randomPositionIn(around))

  private val pairs = List.fill(samples)((Vector2D.randomPositionIn(screen), Vector2D.randomPositionIn(screen)))

  private def isWhole(turns: Double): Boolean = (turns - turns.round).abs < tolerance

  private def assertClose(actual: Vector2D, expected: Vector2D): Unit =
    assertEqualsDouble(actual.x, expected.x, tolerance)
    assertEqualsDouble(actual.y, expected.y, tolerance)

  test("wrap leaves the points inside the rectangle where they are"):
    List.fill(samples)(Vector2D.randomPositionIn(screen)).foreach(point => assertEquals(screen.wrap(point), point))

  test("wrap brings every point inside the rectangle"):
    points.map(screen.wrap).foreach { wrapped =>
      assert(screen.bottomLeft.x <= wrapped.x && wrapped.x < screen.topRight.x, wrapped)
      assert(screen.bottomLeft.y <= wrapped.y && wrapped.y < screen.topRight.y, wrapped)
    }

  test("wrap moves points by whole widths and heights, as on a torus"):
    points.foreach { point =>
      val shift = point - screen.wrap(point)
      assert(isWhole(shift.x / screen.width) && isWhole(shift.y / screen.height), point)
    }

  test("offset is the plain difference between points closer than half the rectangle"):
    pairs
      .filter((from, to) => (to - from).x.abs < screen.width / 2 && (to - from).y.abs < screen.height / 2)
      .foreach((from, to) => assertClose(screen.offset(from, to), to - from))

  test("offset never spans more than half the rectangle"):
    pairs.map(screen.offset).foreach { offset =>
      assert(offset.x.abs <= screen.width / 2 && offset.y.abs <= screen.height / 2, offset)
    }

  test("offset leads from a point to the other on the torus"):
    pairs.foreach((from, to) => assertClose(screen.wrap(from + screen.offset(from, to)), to))
