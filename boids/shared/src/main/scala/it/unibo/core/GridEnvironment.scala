package it.unibo.core

import it.unibo.boundary.Parameter
import it.unibo.core.geometry.Rectangle2D
import it.unibo.core.geometry.Vector2D.Vector2D

/** Boids on the torus of `bounds`, bucketed in cells built in linear time. Queries gather boids from the nearest cells
  * outwards, ring by ring and across the sides, without sorting them: the neighbours are the nearest ones up to a cell.
  */
class GridEnvironment(boids: Seq[Boid], bounds: Rectangle2D, cellSize: Double = GridEnvironment.CellSize)
    extends Environment:
  private type Cell = (Int, Int)
  private val columns = (bounds.width / cellSize).ceil.toInt.max(1)
  private val rows = (bounds.height / cellSize).ceil.toInt.max(1)
  // the cells tile the torus exactly: none is narrower than the others across a side
  private val (cellWidth, cellHeight) = (bounds.width / columns, bounds.height / rows)
  private lazy val cells: Map[Cell, Seq[Boid]] = boids.groupBy(boid => cellOf(boid.position))

  def nearTo(center: Boid, range: Double, count: Int): Seq[Boid] =
    val (column, row) = cellOf(center.position)
    GridEnvironment.rings
      .take((range / cellWidth.min(cellHeight)).ceil.toInt + 1)
      .iterator
      .flatten
      .map((dx, dy) => (Math.floorMod(column + dx, columns), Math.floorMod(row + dy, rows)))
      .distinct // on a small torus the outer rings wrap onto the inner ones
      .flatMap(cells.getOrElse(_, Nil))
      .filter(other => (other ne center) && bounds.offset(center.position, other.position).norm < range)
      .take(count)
      .toVector

  def all: Seq[Boid] = boids
  def replaceBoidsWith(boids: Seq[Boid]): Environment = GridEnvironment(boids, bounds, cellSize)

  private def cellOf(position: Vector2D): Cell =
    val offset = position - bounds.bottomLeft
    (
      Math.floorMod((offset.x / cellWidth).floor.toInt, columns),
      Math.floorMod((offset.y / cellHeight).floor.toInt, rows)
    )

object GridEnvironment:
  /** One separation range: the boids to keep away from are in a boid's own cell or in the first ring around it. */
  val CellSize: Double = Parameter.SeparationRange.default

  /** The offsets of the cells whose Chebyshev distance from a cell is the index of the ring. */
  private val rings: LazyList[Seq[(Int, Int)]] = LazyList
    .from(0)
    .map(ring =>
      for {
        dx <- -ring to ring
        dy <- -ring to ring
        if dx.abs == ring || dy.abs == ring
      } yield (dx, dy)
    )
