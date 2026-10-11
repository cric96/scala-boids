package it.unibo.core

import com.github.plokhotnyuk.rtree2d.core.EuclideanPlane.entry
import com.github.plokhotnyuk.rtree2d.core.{RTree, RTreeEntry}
import it.unibo.core.geometry.Vector2D.Vector2D

class UnboundedRTreeEnvironment(boids: Seq[Boid]) extends Environment:
  given Conversion[Double, Float] = _.toFloat
  private lazy val spatialIndex: RTree[Boid] =
    RTree(boids.map(boid => entry(boid.position.x, boid.position.y, boid)))
  def nearTo(center: Boid, range: Double): Seq[Boid] =
    val centerPosition = center.position
    val (x, y) = (centerPosition.x, centerPosition.y)
    // identity rather than equality: the case-class equality boxes the coordinates of every neighbour
    spatialIndex
      .searchAll(x - range, y - range, x + range, y + range)
      .collect { case entry if entry.value ne center => entry.value }
  def getAllIn(bottomLeft: Vector2D, topRight: Vector2D): Seq[Boid] =
    spatialIndex
      .searchAll(bottomLeft.x, bottomLeft.y, topRight.x, topRight.y)
      .map(_.value)
  def all: Seq[Boid] = boids
  def replaceBoidsWith(boids: Seq[Boid]): Environment = UnboundedRTreeEnvironment(boids)
