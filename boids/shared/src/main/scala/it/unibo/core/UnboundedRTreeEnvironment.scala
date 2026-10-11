package it.unibo.core

import com.github.plokhotnyuk.rtree2d.core.EuclideanPlane.{distanceCalculator, entry}
import com.github.plokhotnyuk.rtree2d.core.{RTree, RTreeEntry}
import it.unibo.core.geometry.Vector2D.Vector2D

class UnboundedRTreeEnvironment(boids: Seq[Boid]) extends Environment:
  given Conversion[Double, Float] = _.toFloat
  private lazy val spatialIndex: RTree[Boid] =
    RTree(boids.map(boid => entry(boid.position.x, boid.position.y, boid)))
  def nearTo(center: Boid, range: Double, count: Int): Seq[Boid] =
    // one more, as the nearest is the center itself, dropped by identity: case-class equality boxes coordinates
    spatialIndex
      .nearestK(center.position.x, center.position.y, count + 1, range)
      .collect { case entry if entry.value ne center => entry.value }
      .take(count)
  def getAllIn(bottomLeft: Vector2D, topRight: Vector2D): Seq[Boid] =
    spatialIndex
      .searchAll(bottomLeft.x, bottomLeft.y, topRight.x, topRight.y)
      .map(_.value)
  def all: Seq[Boid] = boids
  def replaceBoidsWith(boids: Seq[Boid]): Environment = UnboundedRTreeEnvironment(boids)
