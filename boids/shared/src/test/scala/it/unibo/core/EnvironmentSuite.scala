package it.unibo.core

import it.unibo.Fixtures.{population, screen}
import it.unibo.boundary.{Parameter, Settings}
import it.unibo.core.geometry.Vector2D.Vector2D

import scala.util.Random

class EnvironmentSuite extends munit.CatsEffectSuite:
  private val boids = Environments.setupBoids(screen, population)(using Random(0)).all
  private val range = Parameter.VisionRange.default
  // cells much smaller and much larger than the range: the guarantees must not depend on them
  private val grids =
    Seq(Parameter.SeparationRange.min, Parameter.VisionRange.max).map(cellSize =>
      cellSize -> GridEnvironment(boids, cellSize)
    )

  private def othersInRange(center: Boid): Seq[Boid] =
    boids.filter(other => (other ne center) && other.position.distance(center.position) < range)

  test("nearTo returns every other boid closer than range when the count allows it"):
    for {
      (_, grid) <- grids
      center <- boids
    } do assertEquals(grid.nearTo(center, range, population).toSet, othersInRange(center).toSet)

  test("nearTo returns count of the boids closer than range when more are available"):
    for {
      (_, grid) <- grids
      center <- boids
    } do
      val neighbours = grid.nearTo(center, range, Settings.constants.neighbours)
      assertEquals(neighbours.size, othersInRange(center).size.min(Settings.constants.neighbours))
      assert(neighbours.toSet.subsetOf(othersInRange(center).toSet))

  test("nearTo prefers the boids in the cells nearest to the center"):
    for {
      (cellSize, grid) <- grids
      center <- boids
    } do
      def cell(position: Vector2D): (Int, Int) =
        ((position.x / cellSize).floor.toInt, (position.y / cellSize).floor.toInt)
      def ring(other: Boid): Int =
        val ((x, y), (centerX, centerY)) = (cell(other.position), cell(center.position))
        (x - centerX).abs.max((y - centerY).abs)
      val neighbours = grid.nearTo(center, range, Settings.constants.neighbours)
      val leftOut = othersInRange(center).filterNot(neighbours.contains)
      assert(neighbours.map(ring).maxOption.forall(farthest => leftOut.forall(ring(_) >= farthest)), center)
