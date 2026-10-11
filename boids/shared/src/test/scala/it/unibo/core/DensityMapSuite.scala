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
  private val boids = Boid.generator(Vector2D.randomPositionIn(screen), Vector2D.zero).take(population).toVector
  private val densityMap = DensityMap(screen, precision, range, GridEnvironment(boids, screen))

  private def corners(from: Double, to: Double): Seq[Double] =
    LazyList.iterate(from)(_ + precision).takeWhile(_ < to)

  test("the map has a value for the corner of every cell"):
    val expected = for {
      x <- corners(screen.bottomLeft.x, screen.topRight.x)
      y <- corners(screen.bottomLeft.y, screen.topRight.y)
    } yield Vector2D(x, y)
    assertEquals(densityMap.map(_._1).toSet, expected.toSet)

  test("each cell counts the boids in the window centered on its corner on the torus, relative to the densest cell"):
    val counts = densityMap.map((corner, _) =>
      corner -> boids.count(boid =>
        val offset = screen.offset(corner, boid.position)
        offset.x.abs < range / 2 && offset.y.abs < range / 2
      )
    )
    val densest = counts.map(_._2).max.toDouble
    assertEquals(densityMap, counts.map((corner, count) => corner -> count / densest))

  test("an empty environment has zero density everywhere"):
    val densities = DensityMap(screen, precision, range, GridEnvironment(Seq.empty, screen)).map(_._2)
    assertEquals(densities.toSet, Set(0.0))
