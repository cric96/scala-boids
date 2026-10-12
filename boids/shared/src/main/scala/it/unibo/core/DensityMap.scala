package it.unibo.core

import it.unibo.core.geometry.Vector2D.Vector2D
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

import scala.collection.immutable.ArraySeq

/** Boids around the centre of each cell of a grid of square cells, in boids per cell, row by row in `values`. */
case class DensityMap(origin: Vector2D, cell: Double, columns: Int, rows: Int, values: IndexedSeq[Double]):
  private lazy val densest = values.maxOption.getOrElse(0.0)
  private lazy val average = values.sum / values.size

  def cells: Seq[(Int, Int)] = for {
    row <- 0 until rows
    column <- 0 until columns
  } yield (column, row)
  def value(column: Int, row: Int): Double = values(row * columns + column)

  /** The value relative to the densest cell, from zero to one. */
  def density(column: Int, row: Int): Double = if (densest == 0) 0 else value(column, row) / densest

  /** How many times denser than the average the cell is. */
  def crowding(column: Int, row: Int): Double = if (average == 0) 0 else value(column, row) / average
  def centre(column: Int, row: Int): Vector2D = origin + Vector2D(column * cell + cell / 2, row * cell + cell / 2)

/** Counted on the torus in O(boids + cells), then smoothed so that every boid weighs on the cells around its own. */
object DensityMap:
  /** Box blurs in a row weigh the boids almost as a gaussian, smooth enough for round blobs and hills. */
  private val SmoothingPasses = 3

  def apply(boundingBox: Rectangle2D, precision: Double, radius: Double, environment: Environment): DensityMap =
    val columns = (boundingBox.width / precision).ceil.toInt.max(1)
    val rows = (boundingBox.height / precision).ceil.toInt.max(1)
    // the passes together reach about half the radius on each side of a cell
    val reach = (radius / (2 * precision * SmoothingPasses)).round.toInt
    val counts = cellCounts(boundingBox, precision, environment, columns, rows)
    val smoothed = Iterator.iterate(counts)(blur(_, columns, rows, reach)).drop(SmoothingPasses).next()
    DensityMap(boundingBox.bottomLeft, precision, columns, rows, ArraySeq.unsafeWrapArray(smoothed))

  private def cellCounts(
      boundingBox: Rectangle2D,
      precision: Double,
      environment: Environment,
      columns: Int,
      rows: Int
  ): Array[Double] =
    val counts = environment.all.groupMapReduce { boid =>
      val offset = boid.position - boundingBox.bottomLeft
      (
        Math.floorMod((offset.x / precision).floor.toInt, columns),
        Math.floorMod((offset.y / precision).floor.toInt, rows)
      )
    }(_ => 1)(_ + _)
    Array.tabulate(columns * rows)(index => counts.getOrElse((index % columns, index / columns), 0).toDouble)

  /** Every value averaged with the values up to `reach` cells away on the torus, along rows and then along columns. */
  private def blur(values: Array[Double], columns: Int, rows: Int, reach: Int): Array[Double] =
    val alongRows = Array.tabulate(rows)(row => average(values.slice(row * columns, (row + 1) * columns), reach))
    val alongColumns = Array.tabulate(columns)(column => average(alongRows.map(_(column)), reach))
    Array.tabulate(columns * rows)(index => alongColumns(index % columns)(index / columns))

  /** The average of the `2 * reach + 1` values centred on each one, wrapping around the ends of the line. */
  private def average(line: Array[Double], reach: Int): Array[Double] =
    val window = 2 * reach + 1
    val prefix =
      Array.tabulate(line.length + 2 * reach)(i => line(Math.floorMod(i - reach, line.length))).scanLeft(0.0)(_ + _)
    Array.tabulate(line.length)(i => (prefix(i + window) - prefix(i)) / window)
