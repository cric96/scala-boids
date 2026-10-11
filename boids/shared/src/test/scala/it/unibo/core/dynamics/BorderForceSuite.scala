package it.unibo.core.dynamics

import it.unibo.core.Boid
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

class BorderForceSuite extends munit.CatsEffectSuite:
  private val border = BorderForce(Rectangle2D(Vector2D(0, 0), Vector2D(1000, 1000)))
  private val velocity = Vector2D(1, 0)

  test("boids far from the walls keep their velocity"):
    assertEquals(border.avoidWall(Boid(Vector2D(500, 300), velocity)).velocity, velocity)

  test("boids close to a wall are pushed away from it"):
    assert(border.avoidWall(Boid(Vector2D(500, 10), velocity)).velocity.y > 0)
