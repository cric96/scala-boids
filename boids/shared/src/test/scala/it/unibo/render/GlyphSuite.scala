package it.unibo.render

import it.unibo.Fixtures.{samples, screen, tolerance}
import it.unibo.boundary.Settings
import it.unibo.core.Boid
import it.unibo.core.geometry.Vector2D

import scala.util.Random

class GlyphSuite extends munit.CatsEffectSuite:
  private given random: Random = Random(0)
  private val boids = Boid
    .generator(
      Vector2D.randomPositionIn(screen),
      Vector2D.randomUnitary * (random.nextDouble() * Settings.constants.maxSpeed)
    )
    .take(samples)

  test("a glyph points its tip along the velocity of the boid"):
    boids.foreach { boid =>
      val (tip, _, _) = Glyph.vertices(boid)
      val (heading, direction) = ((tip - boid.position).normalize, boid.velocity.normalize)
      assertEqualsDouble(heading.x, direction.x, tolerance)
      assertEqualsDouble(heading.y, direction.y, tolerance)
    }

  test("a glyph is symmetric about the heading of the boid"):
    boids.foreach { boid =>
      val (tip, left, right) = Glyph.vertices(boid)
      assertEqualsDouble(tip.distance(left), tip.distance(right), tolerance)
      assertEqualsDouble(boid.position.distance(left), boid.position.distance(right), tolerance)
    }
