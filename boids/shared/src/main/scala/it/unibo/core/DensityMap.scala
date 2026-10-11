package it.unibo.core

import it.unibo.core.geometry.Vector2D.Vector2D
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

/** Density of boids around each cell corner, counted in O(boids + cells) through a summed-area table. */
object DensityMap:
  def apply(
      boundingBox: Rectangle2D,
      precision: Double,
      visionRange: Double,
      environment: Environment
  ): Seq[(Vector2D, Double)] =
    val columns = (boundingBox.width / precision).ceil.toInt
    val rows = (boundingBox.height / precision).ceil.toInt
    // ponytail: the window snaps to whole cells, off by half a cell when visionRange / (2 * precision) is fractional
    val reach = (visionRange / (2 * precision)).round.toInt
    val table = summedAreaTable(columns, rows, cellCounts(boundingBox, precision, environment))
    def windowCount(column: Int, row: Int): Int =
      val (left, right) = ((column - reach).max(0), (column + reach).min(columns))
      val (bottom, top) = ((row - reach).max(0), (row + reach).min(rows))
      table(right)(top) - table(left)(top) - table(right)(bottom) + table(left)(bottom)
    val counts = for {
      column <- 0 until columns
      row <- 0 until rows
    } yield Vector2D(boundingBox.bottomLeft.x + column * precision, boundingBox.bottomLeft.y + row * precision) ->
      windowCount(column, row)
    val maxCount = counts.map(_._2).maxOption.getOrElse(0).max(1).toDouble
    counts.map((coord, count) => coord -> count / maxCount)

  private def cellCounts(boundingBox: Rectangle2D, precision: Double, environment: Environment): Map[(Int, Int), Int] =
    environment.all.groupMapReduce { boid =>
      val offset = boid.position - boundingBox.bottomLeft
      ((offset.x / precision).floor.toInt, (offset.y / precision).floor.toInt)
    }(_ => 1)(_ + _)

  /** `table(c)(r)` is the number of boids in the cells with column < c and row < r. */
  private def summedAreaTable(columns: Int, rows: Int, counts: Map[(Int, Int), Int]): Vector[Vector[Int]] =
    (0 until columns)
      .scanLeft(Vector.fill(rows + 1)(0)) { (previous, column) =>
        val columnPrefix = (0 until rows).scanLeft(0)((sum, row) => sum + counts.getOrElse((column, row), 0))
        previous.lazyZip(columnPrefix).map(_ + _)
      }
      .toVector
