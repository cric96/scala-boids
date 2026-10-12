package it.unibo.core

import it.unibo.Fixtures.{population, screen}
import it.unibo.boundary.Parameter
import it.unibo.core.geometry.Vector2D

import scala.util.Random

class DensityMapSuite extends munit.CatsEffectSuite:
  private given Random = Random(0)
  private val precision = Parameter.BucketSize.default
  // snapped to whole cells, where counting by cells is exact
  private val range = 2 * precision * (Parameter.BucketRange.default / (2 * precision)).round
  // a dense flock, so that the counts differ, and boids scattered up to the sides of the screen
  private val boids = Environments.setupBoids(screen, population).all ++
    Boid.generator(Vector2D.randomPositionIn(screen), Vector2D.zero).take(population)
  private val densityMap = DensityMap(screen, precision, range, GridEnvironment(boids, screen))

  private def corners(from: Double, to: Double): Seq[Double] =
    LazyList.iterate(from)(_ + precision).takeWhile(_ < to)

  test("the map has a cell for every corner of the grid over the screen"):
    val expected = for {
      x <- corners(screen.bottomLeft.x, screen.topRight.x)
      y <- corners(screen.bottomLeft.y, screen.topRight.y)
    } yield Vector2D(x, y)
    assertEquals(densityMap.cells.map(densityMap.corner).toSet, expected.toSet)

  test("each cell counts the boids in the window centered on its corner on the torus"):
    densityMap.cells.foreach { (column, row) =>
      val corner = densityMap.corner(column, row)
      val inWindow = boids.count { boid =>
        val offset = screen.offset(corner, boid.position)
        offset.x.abs < range / 2 && offset.y.abs < range / 2
      }
      assertEquals(densityMap.count(column, row), inWindow, corner)
    }

  test("the densest cell has density one and denser cells never have a lower density"):
    val byCount = densityMap.cells.sortBy(densityMap.count)
    val densities = byCount.map(densityMap.density)
    assertEquals(densities.last, 1.0)
    assertEquals(densities, densities.sorted)

  test("an empty environment has zero density everywhere"):
    val empty = DensityMap(screen, precision, range, GridEnvironment(Seq.empty, screen))
    assertEquals(empty.cells.map(empty.density).toSet, Set(0.0))
