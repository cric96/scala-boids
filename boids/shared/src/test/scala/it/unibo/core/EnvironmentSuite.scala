package it.unibo.core

import it.unibo.core.geometry.{Rectangle2D, Vector2D}

import scala.util.Random

class EnvironmentSuite extends munit.CatsEffectSuite:
  private val environment =
    Environments.setupBoids(Rectangle2D(Vector2D(0, 0), Vector2D(100, 100)), 200)(using Random(0))

  test("nearTo returns the other boids inside the square window around the center"):
    val range = 5.0
    environment.all.foreach { center =>
      val expected = environment.all.filter(other =>
        (other ne center) &&
          (other.position.x - center.position.x).abs <= range &&
          (other.position.y - center.position.y).abs <= range
      )
      assertEquals(environment.nearTo(center, range).toSet, expected.toSet)
    }
