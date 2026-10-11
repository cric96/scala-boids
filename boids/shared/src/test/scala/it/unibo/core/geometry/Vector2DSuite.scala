package it.unibo.core.geometry

import it.unibo.Fixtures.{samples, tolerance}
import it.unibo.core.geometry.Vector2D.*

import scala.util.Random

class Vector2DSuite extends munit.CatsEffectSuite:
  private given random: Random = Random(0)
  private val square = Rectangle2D(Vector2D(-1, -1), Vector2D(1, 1))
  private val vectorsAndBounds = List.fill(samples)(Vector2D.randomPositionIn(square) -> random.nextDouble())
  private val longer = vectorsAndBounds.filter(_.norm > _)

  test("limit caps longer vectors to the bound"):
    longer.foreach((vector, bound) => assertEqualsDouble(vector.limit(bound).norm, bound, tolerance))

  test("limit keeps the direction of longer vectors"):
    longer.foreach { (vector, bound) =>
      assertEqualsDouble(vector.limit(bound).normalize.x, vector.normalize.x, tolerance)
      assertEqualsDouble(vector.limit(bound).normalize.y, vector.normalize.y, tolerance)
    }

  test("limit leaves shorter vectors untouched"):
    vectorsAndBounds.filter(_.norm <= _).foreach((vector, bound) => assertEquals(vector.limit(bound), vector))

  test("normalizing the zero vector gives the zero vector"):
    assertEquals(zero.normalize, zero)
