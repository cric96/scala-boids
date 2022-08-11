package it.unibo.core

import it.unibo.core.geometry.Vector2D
import it.unibo.core.geometry.Rectangle2D
import com.github.plokhotnyuk.rtree2d.core.*
import EuclideanPlane.*
import it.unibo.core.geometry.Vector2D.Vector2D
import monocle.syntax.all.*

class BoundedRTreeEnvironment(boids: Seq[Boid], boundingBox: Rectangle2D) extends Environment:
  given Conversion[Double, Float] = _.toFloat
  private val adjustedBoids = boids
    .map(boid => boid.focus(_.position).modify(Rectangle2D.wrapCornerRectanglePosition(boundingBox, _)))

  private val spatialIndex: RTree[Boid] =
    RTree(adjustedBoids.map(boid => entry(boid.position.x, boid.position.y, boid)))
  def nearTo(center: Boid, range: Double): Seq[Boid] =
    val centerPosition = center.position
    val (x, y) = (centerPosition.x, centerPosition.y)
    val (bottomX, bottomY, topX, topY) =
      (boundingBox.bottomLeft.x, boundingBox.bottomLeft.y, boundingBox.topRight.x, boundingBox.topRight.y)
    def evaluateOrEmpty(condition: Boolean)(action: Seq[RTreeEntry[Boid]]): Seq[RTreeEntry[Boid]] = if (condition)
      action
    else
      Seq.empty
    val plainSearch = spatialIndex
      .searchAll(x - range, y - range, x + range, y + range)

    val bottomCornerCondition = x - range < bottomX && y - range < bottomY
    val topCornerCondition = x + range > topX && y + range > topY
    val bottomCorner =
      evaluateOrEmpty(bottomCornerCondition) {
        spatialIndex.searchAll(topX - (range - (x - bottomX)), topY - (range - (y - bottomY)), topX, topY)
      }
    val topCorner =
      evaluateOrEmpty(topCornerCondition) {
        spatialIndex.searchAll(bottomX, bottomY, bottomX + (range - (topX - x)), bottomY + (range - (topY - y)))
      }
    val leftCorner = evaluateOrEmpty(x - range < bottomX && !bottomCornerCondition) {
      spatialIndex.searchAll(topX - (range - (x - bottomX)), y - range, topX, y + range)
    }
    val rightCorner = evaluateOrEmpty(x + range > topX && !topCornerCondition) {
      spatialIndex.searchAll(bottomX, y - range, bottomX + (range - (topX - x)), y + range)
    }
    val downCorner = evaluateOrEmpty(y - range < bottomY && !bottomCornerCondition) {
      spatialIndex.searchAll(x - range, topY - (range - (y - bottomY)), x + range, topY)
    }
    val upCorner = evaluateOrEmpty(y + range > topY && !topCornerCondition) {
      spatialIndex.searchAll(x - range, bottomY, x + range, y + range)
    }

    val otherSearch = upCorner ++ downCorner ++ rightCorner ++ topCorner ++ bottomCorner ++ leftCorner

    plainSearch
      .map(_.value)
      .filter(_ != center)
  def getAllIn(bottomLeft: Vector2D, topRight: Vector2D): Seq[Boid] =
    spatialIndex
      .searchAll(bottomLeft.x, bottomLeft.y, topRight.x, topRight.y)
      .map(_.value)
  def all: Seq[Boid] = adjustedBoids
  def replaceBoidsWith(boids: Seq[Boid]): Environment = BoundedRTreeEnvironment(boids, boundingBox)
