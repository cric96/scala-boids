package it.unibo.core.geometry

import scala.annotation.targetName
import scala.util.Random
import upickle.default._
object Vector2D:
  opaque type Vector2D = (Double, Double)
  def apply(x: Double, y: Double): Vector2D = (x, y)
  extension (point: Vector2D)
    def x: Double = point._1
    def y: Double = point._2
    @targetName("plus")
    def +(other: Vector2D) = Vector2D(point.x + other.x, point.y + other.y)
    @targetName("minus")
    def -(other: Vector2D) = Vector2D(point.x - other.x, point.y - other.y)
    @targetName("multiply")
    def *(alpha: Double) = Vector2D(point.x * alpha, point.y * alpha)
    @targetName("divide")
    def /(alpha: Double) = point * (1.0 / alpha)
    def normalize: Vector2D = Vector2D(x / norm, y / norm)
    def norm: Double = math.sqrt(x * x + y * y)
    def limit(bound: Double): Vector2D = if (bound > norm)
      normalize * bound
    else
      point
    def distance(other: Vector2D): Double = math.sqrt(math.pow(x - other.x, 2) + math.pow(y - other.y, 2))
  def randomPositionIn(rectangle2D: Rectangle2D)(using random: Random): Vector2D =
    val minX = rectangle2D.bottomLeft.x
    val minY = rectangle2D.bottomLeft.y
    val randomX = random.nextDouble() * rectangle2D.width + minX
    val randomY = random.nextDouble() * rectangle2D.height + minY
    (randomX, randomY)
  val zero: Vector2D = (0.0, 0.0)

  def randomUnitary(using random: Random): Vector2D =
    val vector = (random.nextDouble() - 0.5, random.nextDouble() - 0.5)
    vector / vector.norm

  given vectorMacroRW: ReadWriter[Vector2D] = macroRW
