package it.unibo.core.geometry
import it.unibo.core.geometry.Vector2D.*

case class Rectangle2D(bottomLeft: Vector2D, topRight: Vector2D):
  lazy val width: Double = topRight.x - bottomLeft.x
  lazy val height: Double = topRight.y - bottomLeft.y
  def contains(point: Vector2D): Boolean =
    point.x > bottomLeft.x && point.x < topRight.x && point.y > bottomLeft.y && point.y < topRight.y
  lazy val center: Vector2D = Vector2D(bottomLeft.x + width / 2, bottomLeft.y + height / 2)
object Rectangle2D:
  def wrapCornerRectanglePosition(rectangle2D: Rectangle2D, position: Vector2D): Vector2D =
    val x =
      if (rectangle2D.bottomLeft.x > position.x)
        rectangle2D.topRight.x
      else if (rectangle2D.topRight.x < position.x)
        rectangle2D.bottomLeft.x
      else
        position.x

    val y =
      if (rectangle2D.bottomLeft.y > position.y)
        rectangle2D.topRight.y
      else if (rectangle2D.topRight.y < position.y)
        rectangle2D.bottomLeft.y
      else
        position.y

    Vector2D(x, y)
