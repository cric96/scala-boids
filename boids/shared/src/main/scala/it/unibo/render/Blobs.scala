package it.unibo.render

import it.unibo.core.DensityMap
import it.unibo.core.geometry.Vector2D.Vector2D

/** Where the boids crowd at least `threshold` times the average, by marching squares between the cell centres: one
  * polygon per square, to be filled together. The squares go across the sides of the torus, so the blobs do too.
  */
object Blobs:
  def apply(densityMap: DensityMap, threshold: Double): Seq[Seq[Vector2D]] =
    def crowding(column: Int, row: Int): Double =
      densityMap.crowding(Math.floorMod(column, densityMap.columns), Math.floorMod(row, densityMap.rows))
    for {
      row <- -1 until densityMap.rows
      column <- -1 until densityMap.columns
      square = Seq((column, row), (column + 1, row), (column + 1, row + 1), (column, row + 1))
      values = square.map(crowding.tupled)
      if values.exists(_ >= threshold)
    } yield clip(square.map(densityMap.centre.tupled).zip(values), threshold)

  /** The crowded corners of a square and, between them, the points along the sides where the crowding, linear from
    * corner to corner, reaches the threshold.
    */
  private def clip(corners: Seq[(Vector2D, Double)], threshold: Double): Seq[Vector2D] =
    corners.zip(corners.tail :+ corners.head).flatMap { case ((from, start), (to, end)) =>
      val crossing = Option.when((start >= threshold) != (end >= threshold))(
        from + (to - from) * ((threshold - start) / (end - start))
      )
      Option.when(start >= threshold)(from) ++ crossing
    }
