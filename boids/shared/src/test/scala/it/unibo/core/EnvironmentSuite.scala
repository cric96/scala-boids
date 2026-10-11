package it.unibo.core

import it.unibo.Fixtures.{population, screen}
import it.unibo.boundary.Parameter
import it.unibo.core.dynamics.Flocking

import scala.util.Random

class EnvironmentSuite extends munit.CatsEffectSuite:
  private val environment = Environments.setupBoids(screen, population)(using Random(0))
  private val range = Parameter.VisionRange.default

  private def othersByDistance(center: Boid): Seq[Boid] =
    val distance = (other: Boid) => other.position.distance(center.position)
    environment.all.filter(other => (other ne center) && distance(other) < range).sortBy(distance)

  test("nearTo returns every other boid closer than range when the count allows it"):
    environment.all.foreach(center =>
      assertEquals(environment.nearTo(center, range, population).toSet, othersByDistance(center).toSet)
    )

  test("nearTo returns only the count boids closest to the center"):
    environment.all.foreach(center =>
      assertEquals(
        environment.nearTo(center, range, Flocking.Neighbours).toSet,
        othersByDistance(center).take(Flocking.Neighbours).toSet
      )
    )
