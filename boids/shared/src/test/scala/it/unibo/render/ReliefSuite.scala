package it.unibo.render

import it.unibo.Fixtures.{population, screen, tolerance}
import it.unibo.boundary.Parameter
import it.unibo.core.geometry.Vector2D
import it.unibo.core.{Boid, DensityMap, Environments, GridEnvironment}

import scala.util.Random

class ReliefSuite extends munit.CatsEffectSuite:
  private given Random = Random(0)
  // a dense flock and boids scattered up to the sides, leaving empty cells in between
  private val boids = Environments.setupBoids(screen, population).all ++
    Boid.generator(Vector2D.randomPositionIn(screen), Vector2D.zero).take(population / 2)
  private val densityMap = DensityMap(
    screen,
    Parameter.BucketSize.default,
    Parameter.BucketRange.default,
    GridEnvironment(boids, screen)
  )
  private val relief = Relief(densityMap)
  private val vertices = relief.points.toVector.grouped(Relief.CoordinatesPerPoint).toVector
  private val triangles = relief.faces.toVector.grouped(Relief.CornersPerFace).toVector

  test("the relief has a vertex above the centre of every cell"):
    val above = vertices.map(point => Vector2D(point(0), point(1))).toSet
    val centres = densityMap.cells.map(densityMap.centre).map(centre => Vector2D(centre.x.toFloat, centre.y.toFloat))
    assertEquals(above, centres.toSet)

  test("two triangles join every four neighbouring centres"):
    assertEquals(triangles.size, 2 * (densityMap.columns - 1) * (densityMap.rows - 1))

  test("empty cells lie flat at zero and the densest cell is the highest, with density one"):
    val empty = densityMap.cells.zipWithIndex.collect {
      case ((column, row), vertex) if densityMap.value(column, row) == 0 =>
        vertex
    }
    assert(empty.nonEmpty)
    empty.foreach(vertex => assertEquals(vertices(vertex)(2), 0f))
    val highest = vertices.indices.maxBy(vertices(_)(2))
    assertEqualsDouble(relief.densities(highest), 1, tolerance)
    assertEqualsDouble(vertices(highest)(2), Relief.HeightRatio * screen.width.min(screen.height), tolerance)

  test("every triangle faces up, away from the ground"):
    triangles.foreach { case Seq(a, b, c) =>
      val (u, v) = (vertices(b).zip(vertices(a)).map(_ - _), vertices(c).zip(vertices(a)).map(_ - _))
      assert(u(0) * v(1) - u(1) * v(0) > 0, (a, b, c))
    }
