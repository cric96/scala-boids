package it.unibo.core.geometry
import it.unibo.core.geometry.Vector2D.*

case class Rectangle2D(bottomLeft: Vector2D, topRight: Vector2D):
  lazy val width: Double = topRight.x - bottomLeft.x
  lazy val height: Double = topRight.y - bottomLeft.y
  def contains(point: Vector2D): Boolean =
    point.x > bottomLeft.x && point.x < topRight.x && point.y > bottomLeft.y && point.y < topRight.y
  lazy val center: Vector2D = Vector2D(bottomLeft.x + width / 2, bottomLeft.y + height / 2)

  /** The point brought inside as on a torus: leaving from one side enters from the opposite one. */
  def wrap(point: Vector2D): Vector2D =
    Vector2D(Rectangle2D.wrap(point.x, bottomLeft.x, width), Rectangle2D.wrap(point.y, bottomLeft.y, height))
object Rectangle2D:
  private def wrap(value: Double, start: Double, size: Double): Double =
    val offset = (value - start) % size
    start + (if (offset < 0) offset + size else offset)
