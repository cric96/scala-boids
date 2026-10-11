package it.unibo.core

import it.unibo.boundary.Parameter
import it.unibo.core.dynamics.Flocking
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

import scala.util.Random

class EnvironmentSuite extends munit.CatsEffectSuite:
  private val bounds = Rectangle2D(Vector2D(0, 0), Vector2D(1000, 1000))
  private val environment = Environments.setupBoids(bounds, Parameter.BoidsCount.default.toInt)(using Random(0))

  test("nearTo returns the count other boids closest to the center, closer than range"):
    val range = Parameter.VisionRange.default
    for {
      count <- Seq(Flocking.Neighbours, environment.all.size)
      center <- environment.all
    } do
      val distance = (other: Boid) => other.position.distance(center.position)
      val expected = environment.all.filter(other => (other ne center) && distance(other) < range).sortBy(distance)
      assertEquals(environment.nearTo(center, range, count).toSet, expected.take(count).toSet)
