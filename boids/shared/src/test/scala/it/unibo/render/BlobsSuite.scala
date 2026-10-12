package it.unibo.render

import it.unibo.Fixtures.{population, screen, tolerance}
import it.unibo.boundary.Parameter
import it.unibo.core.geometry.Vector2D
import it.unibo.core.geometry.Vector2D.Vector2D
import it.unibo.core.{Boid, DensityMap, Environments, GridEnvironment}

import scala.util.Random

class BlobsSuite extends munit.CatsEffectSuite:
  private given Random = Random(0)
  private val cell = Parameter.BucketSize.default
  private val (columns, rows) = ((screen.width / cell).toInt, (screen.height / cell).toInt)
  private val threshold = Parameter.BlobThreshold.default
  private def mapOf(values: Int => Double): DensityMap =
    DensityMap(screen.bottomLeft, cell, columns, rows, IndexedSeq.tabulate(columns * rows)(values))

  /** The area enclosed by the blobs, each polygon by the shoelace formula. */
  private def area(blobs: Seq[Seq[Vector2D]]): Double = blobs.map { polygon =>
    polygon.zip(polygon.tail :+ polygon.head).map((from, to) => from.x * to.y - to.x * from.y).sum.abs / 2
  }.sum

  test("boids spread evenly make no blob"):
    assertEquals(Blobs(mapOf(_ => 1), threshold), Seq.empty)

  test("a threshold every cell reaches covers the whole screen, across its sides too"):
    assert(area(Blobs(mapOf(_ => 1), Parameter.BlobThreshold.min)) >= screen.width * screen.height)

  test("a lone crowded cell makes a diamond whose corners are where the crowding falls to the threshold"):
    val crowded = mapOf(index => if (index == (rows / 2) * columns + columns / 2) 1 else 0)
    val peak = crowded.crowding(columns / 2, rows / 2)
    // the crowding falls linearly from the peak to zero at the centre of the next cell
    val reach = cell * (peak - threshold) / peak
    assertEqualsDouble(area(Blobs(crowded, threshold)), 2 * reach * reach, tolerance)

  test("raising the threshold never grows the blobs of a flock"):
    val boids = Environments.setupBoids(screen, population).all ++
      Boid.generator(Vector2D.randomPositionIn(screen), Vector2D.zero).take(population)
    val flock = DensityMap(screen, cell, Parameter.BucketRange.default, GridEnvironment(boids, screen))
    val thresholds = LazyList
      .iterate(Parameter.BlobThreshold.min)(_ + Parameter.BlobThreshold.step)
      .takeWhile(_ <= Parameter.BlobThreshold.max)
    val areas = thresholds.map(threshold => area(Blobs(flock, threshold)))
    assertEquals(areas.toList, areas.toList.sorted.reverse)
    assert(areas.head > areas.last, areas)
