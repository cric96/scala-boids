package it.unibo.core.dynamics

import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.{Boid, Dynamics, Environment}
import cats.effect.IO
import monocle.syntax.all._
class BorderForce(bound: Rectangle2D, maxSpeed: Double = 2) extends Dynamics:
  override def apply(environment: Environment): IO[Environment] =
    IO(environment.replaceBoidsWith(environment.all.map(avoidWall)))

  def avoidWall(boid: Boid): Boid =
    val position = boid.position
    val forceLeft = Vector2D(forceFromDistance(position.distance(Vector2D(bound.bottomLeft.x, position.y))), 0)
    val forceRight = Vector2D(-forceFromDistance(position.distance(Vector2D(bound.topRight.x, position.y))), 0)
    val forceTop = Vector2D(0, -forceFromDistance(position.distance(Vector2D(position.x, bound.topRight.y))))
    val forceBottom = Vector2D(0, forceFromDistance(position.distance(Vector2D(position.x, bound.bottomLeft.y))))
    boid.focus(_.velocity).modify(v => (v + forceLeft + forceRight + forceTop + forceBottom).limit(maxSpeed))

  private def forceFromDistance(distance: Double): Double =
    1 / math.sqrt(distance + 1) * 2
