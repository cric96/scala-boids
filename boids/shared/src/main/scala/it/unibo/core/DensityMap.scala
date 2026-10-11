package it.unibo.core

import it.unibo.core.geometry.Vector2D.Vector2D
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

/** Density of boids around each cell corner on the torus, counted in O(boids + cells) through a summed-area table. */
object DensityMap:
  def apply(
      boundingBox: Rectangle2D,
      precision: Double,
      visionRange: Double,
      environment: Environment
  ): Seq[(Vector2D, Double)] =
    val columns = (boundingBox.width / precision).ceil.toInt.max(1)
    val rows = (boundingBox.height / precision).ceil.toInt.max(1)
    // ponytail: the window snaps to whole cells, off by half a cell when visionRange / (2 * precision) is fractional
    val reach = (visionRange / (2 * precision)).round.toInt
    val counts = cellCounts(boundingBox, precision, environment, columns, rows)
    // padded by reach cells on every side, which repeat the cells across the opposite side of the torus
    val table = summedAreaTable(columns + 2 * reach, rows + 2 * reach)((column, row) =>
      counts.getOrElse((Math.floorMod(column - reach, columns), Math.floorMod(row - reach, rows)), 0)
    )
    def windowCount(column: Int, row: Int): Int =
      val (right, top) = (column + 2 * reach, row + 2 * reach)
      table(right)(top) - table(column)(top) - table(right)(row) + table(column)(row)
    val densities = for {
      column <- 0 until columns
      row <- 0 until rows
    } yield Vector2D(boundingBox.bottomLeft.x + column * precision, boundingBox.bottomLeft.y + row * precision) ->
      windowCount(column, row)
    val maxCount = densities.map(_._2).maxOption.getOrElse(0).max(1).toDouble
    densities.map((coord, count) => coord -> count / maxCount)

  private def cellCounts(
      boundingBox: Rectangle2D,
      precision: Double,
      environment: Environment,
      columns: Int,
      rows: Int
  ): Map[(Int, Int), Int] =
    environment.all.groupMapReduce { boid =>
      val offset = boid.position - boundingBox.bottomLeft
      (
        Math.floorMod((offset.x / precision).floor.toInt, columns),
        Math.floorMod((offset.y / precision).floor.toInt, rows)
      )
    }(_ => 1)(_ + _)

  /** `table(c)(r)` is the number of boids in the cells with column < c and row < r. */
  private def summedAreaTable(columns: Int, rows: Int)(count: (Int, Int) => Int): Vector[Vector[Int]] =
    (0 until columns)
      .scanLeft(Vector.fill(rows + 1)(0)) { (previous, column) =>
        val columnPrefix = (0 until rows).scanLeft(0)((sum, row) => sum + count(column, row))
        previous.lazyZip(columnPrefix).map(_ + _)
      }
      .toVector
