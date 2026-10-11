package it.unibo.core

import it.unibo.boundary.Parameter
import it.unibo.core.geometry.Vector2D.Vector2D

/** Boids bucketed in square cells, built in linear time. Queries gather boids from the nearest cells outwards, ring by
  * ring, without sorting them: the neighbours are the nearest ones up to the size of a cell.
  */
class GridEnvironment(boids: Seq[Boid], cellSize: Double = GridEnvironment.CellSize) extends Environment:
  private type Cell = (Int, Int)
  private lazy val cells: Map[Cell, Seq[Boid]] = boids.groupBy(boid => cellOf(boid.position))

  def nearTo(center: Boid, range: Double, count: Int): Seq[Boid] =
    val home = cellOf(center.position)
    GridEnvironment.rings
      .take((range / cellSize).ceil.toInt + 1)
      .iterator
      .flatten
      .map((dx, dy) => (home._1 + dx, home._2 + dy))
      .flatMap(cells.getOrElse(_, Nil))
      .filter(other => (other ne center) && other.position.distance(center.position) < range)
      .take(count)
      .toVector

  def all: Seq[Boid] = boids
  def replaceBoidsWith(boids: Seq[Boid]): Environment = GridEnvironment(boids, cellSize)

  private def cellOf(position: Vector2D): Cell =
    ((position.x / cellSize).floor.toInt, (position.y / cellSize).floor.toInt)

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
