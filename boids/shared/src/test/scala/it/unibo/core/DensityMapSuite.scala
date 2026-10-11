package it.unibo.core

import it.unibo.core.geometry.{Rectangle2D, Vector2D}

import scala.util.Random

class DensityMapSuite extends munit.CatsEffectSuite:
  private val bounds = Rectangle2D(Vector2D(0, 0), Vector2D(100, 80))
  private val boids = Boid.generator(Vector2D.randomPositionIn(bounds)(using Random(0)), Vector2D.zero).take(300)

  test("each cell counts the boids in the window centered on its corner, relative to the densest cell"):
    val (precision, range) = (10, 40.0)
    val counts = for {
      x <- 0 until 100 by precision
      y <- 0 until 80 by precision
    } yield Vector2D(x, y) -> boids.count(boid =>
      (boid.position.x - x).abs < range / 2 && (boid.position.y - y).abs < range / 2
    )
    val expected = counts.map((coord, count) => coord -> count.toDouble / counts.map(_._2).max).toMap
    assertEquals(DensityMap(bounds, precision, range, UnboundedRTreeEnvironment(boids)).toMap, expected)

  test("an empty environment has zero density everywhere"):
    val densities = DensityMap(bounds, 10, 40, UnboundedRTreeEnvironment(Seq.empty)).map(_._2)
    assertEquals(densities.toSet, Set(0.0))
