package it.unibo.core

import it.unibo.Fixtures.{population, screen}
import it.unibo.boundary.{Parameter, Settings}
import it.unibo.core.geometry.Vector2D

import scala.util.Random

class EnvironmentSuite extends munit.CatsEffectSuite:
  private given Random = Random(0)
  // a dense flock, where the count matters, and boids scattered up to the sides of the screen
  private val boids = Environments.setupBoids(screen, population).all ++
    Boid.generator(Vector2D.randomPositionIn(screen), Vector2D.randomUnitary).take(population)
  private val range = Parameter.VisionRange.default
  private val neighbours = Settings.constants.neighbours
  // cells much smaller and much larger than the range, and as tall as the screen, where the rings wrap onto themselves
  private val grids = Seq(Parameter.SeparationRange.min, Parameter.VisionRange.max, screen.height)
    .map(cellSize => cellSize -> GridEnvironment(boids, screen, cellSize))

  private def othersInRange(center: Boid): Seq[Boid] =
    boids.filter(other => (other ne center) && screen.offset(center.position, other.position).norm < range)

  test("nearTo returns every other boid closer than range on the torus when the count allows it"):
    for {
      (_, grid) <- grids
      center <- boids
    } do
      val found = grid.nearTo(center, range, boids.size)
      assertEquals(found.size, othersInRange(center).size)
      assertEquals(found.toSet, othersInRange(center).toSet)

  test("nearTo returns count of the boids closer than range when more are available"):
    for {
      (_, grid) <- grids
      center <- boids
    } do
      val found = grid.nearTo(center, range, neighbours)
      assertEquals(found.size, othersInRange(center).size.min(neighbours))
      assert(found.toSet.subsetOf(othersInRange(center).toSet))

  test("nearTo prefers the nearest boids, up to two cells"):
    for {
      (cellSize, grid) <- grids
      center <- boids
    } do
      def apart(other: Boid): Double =
        val offset = screen.offset(center.position, other.position)
        offset.x.abs.max(offset.y.abs)
      val found = grid.nearTo(center, range, neighbours)
      val leftOut = othersInRange(center).filterNot(found.contains)
      assert(found.map(apart).maxOption.forall(farthest => leftOut.forall(farthest < apart(_) + 2 * cellSize)), center)
