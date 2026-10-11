package it.unibo.core.dynamics

import it.unibo.Fixtures.{samples, screen}
import it.unibo.boundary.Settings
import it.unibo.core.Boid
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

import scala.util.Random

class BorderForceSuite extends munit.CatsEffectSuite:
  private given Random = Random(0)
  private val border = BorderForce(screen)
  private val margin = Vector2D(Settings.constants.borderMargin, Settings.constants.borderMargin)
  private val inner = Rectangle2D(screen.bottomLeft + margin, screen.topRight - margin)

  test("boids farther than the margin from every wall keep their velocity"):
    Boid
      .generator(Vector2D.randomPositionIn(inner), Vector2D.randomUnitary)
      .take(samples)
      .foreach(boid => assertEquals(border.avoidWall(boid).velocity, boid.velocity))

  test("boids at rest within the margin of a wall are pushed towards the inside"):
    Iterator
      .continually(Vector2D.randomPositionIn(screen))
      .filterNot(inner.contains)
      .take(samples)
      .foreach { position =>
        val push = border.avoidWall(Boid(position, Vector2D.zero)).velocity
        val inwards = screen.center - position
        assert(push.x * inwards.x + push.y * inwards.y > 0, position)
      }
