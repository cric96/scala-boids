package it.unibo.core

import it.unibo.Fixtures.{population, screen, tolerance}
import it.unibo.boundary.Parameter
import it.unibo.core.geometry.Vector2D

import scala.util.Random

class DensityMapSuite extends munit.CatsEffectSuite:
  private given Random = Random(0)
  private val precision = Parameter.BucketSize.default
  private val range = Parameter.BucketRange.default
  // a dense flock, so that the densities differ, and boids scattered up to the sides of the screen
  private val boids = Environments.setupBoids(screen, population).all ++
    Boid.generator(Vector2D.randomPositionIn(screen), Vector2D.zero).take(population)
  private def mapOf(boids: Seq[Boid]): DensityMap = DensityMap(screen, precision, range, GridEnvironment(boids, screen))
  private val densityMap = mapOf(boids)

  private def centres(from: Double, to: Double): Seq[Double] =
    LazyList.iterate(from + precision / 2)(_ + precision).takeWhile(_ < to)

  test("the map has a value at the centre of every cell of the grid over the screen"):
    val expected = for {
      x <- centres(screen.bottomLeft.x, screen.topRight.x)
      y <- centres(screen.bottomLeft.y, screen.topRight.y)
    } yield Vector2D(x, y)
    assertEquals(densityMap.cells.map(densityMap.centre).toSet, expected.toSet)

  test("smoothing spreads the boids over the cells without losing or adding any"):
    assertEqualsDouble(densityMap.cells.map(densityMap.value).sum, boids.size.toDouble, tolerance * boids.size)

  test("a lone boid is densest in its own cell and fades evenly on every side"):
    val empty = mapOf(Seq.empty)
    val (column, row) = (empty.columns / 2, empty.rows / 2)
    val lone = mapOf(Seq(Boid(empty.centre(column, row), Vector2D.zero)))
    def side(dx: Int, dy: Int): Seq[Double] =
      (0 until lone.rows / 2).map(d => lone.value(column + d * dx, row + d * dy))
    val right = side(1, 0)
    Seq(side(-1, 0), side(0, 1), side(0, -1)).foreach(_.zip(right).foreach(assertEqualsDouble(_, _, tolerance)))
    assertEquals(right, right.sorted.reverse)
    assert(right.head > 0, right)
    assertEqualsDouble(right.last, 0, tolerance)

  test("moving the whole flock across the sides of the torus moves its map along"):
    val shift = densityMap.columns / 2
    val shifted =
      mapOf(boids.map(boid => boid.copy(position = screen.wrap(boid.position + Vector2D(shift * precision, 0)))))
    densityMap.cells.foreach((column, row) =>
      assertEqualsDouble(
        shifted.value((column + shift) % densityMap.columns, row),
        densityMap.value(column, row),
        tolerance
      )
    )

  test("the densest cell has density one and denser cells never have a lower density"):
    val byValue = densityMap.cells.sortBy(densityMap.value)
    val densities = byValue.map(densityMap.density)
    assertEquals(densities.last, 1.0)
    assertEquals(densities, densities.sorted)

  test("a boid at the centre of every cell makes every cell as crowded as the average"):
    val empty = mapOf(Seq.empty)
    val even = mapOf(empty.cells.map(cell => Boid(empty.centre.tupled(cell), Vector2D.zero)))
    even.cells.foreach(cell => assertEqualsDouble(even.crowding.tupled(cell), 1, tolerance))

  test("an empty environment has zero density and zero crowding everywhere"):
    val empty = mapOf(Seq.empty)
    assertEquals(
      empty.cells.flatMap(cell => Seq(empty.density.tupled(cell), empty.crowding.tupled(cell))).toSet,
      Set(0.0)
    )
