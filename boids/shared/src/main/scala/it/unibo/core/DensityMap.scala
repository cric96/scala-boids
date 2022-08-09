package it.unibo.core

import it.unibo.core.geometry.Vector2D.Vector2D
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

object DensityMap:
  def apply(
      boundingBox: Rectangle2D,
      precision: Double,
      visionRange: Double,
      environment: Environment
  ): Map[Vector2D, Double] =
    val startX = boundingBox.bottomLeft.x
    val startY = boundingBox.bottomLeft.y
    def iterateUntil(start: Double, end: Double): LazyList[Double] =
      LazyList.iterate(start)(_ + precision).takeWhile(_ < end)
    val allCoords = for {
      x <- iterateUntil(startX, boundingBox.topRight.x)
      y <- iterateUntil(startY, boundingBox.topRight.y)
    } yield (Vector2D(x, y))
    val densityMap = allCoords.map { coord =>
      val bottomLeft = coord - (Vector2D(visionRange / 2, visionRange / 2))
      val topRight = coord + (Vector2D(visionRange / 2, visionRange / 2))
      val density = environment.getAllIn(bottomLeft, topRight).size
      coord -> density
    }
    val maxDensityValue = densityMap.maxBy(_._2)._2.toDouble
    densityMap.map((coord, density) => coord -> (density / maxDensityValue)).toMap
