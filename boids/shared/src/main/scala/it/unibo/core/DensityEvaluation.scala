package it.unibo.core

import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.geometry.Vector2D.Vector2D

trait DensityEvaluation extends (Vector2D => Int)

object DensityEvaluation:
  def fromEnvironment(environment: Environment, bucketSize: Double, boundingBox: Rectangle2D): DensityEvaluation =
    (position: Vector2D) =>
      if (!boundingBox.contains(position)) {
        0
      } else {
        val bottomLeft = position - (Vector2D(bucketSize / 2, bucketSize / 2))
        val topRight = position + (Vector2D(bucketSize / 2, bucketSize / 2))
        environment.getAllIn(bottomLeft, topRight).size
      }
