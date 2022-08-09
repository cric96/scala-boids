package it.unibo.core.dynamics

import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.{Boid, Dynamics, Environment}
import monix.eval.Task
import monocle.syntax.all._
class BorderForce(bound: Rectangle2D) extends Dynamics:
  val actuator = LinearVelocity()
  override def apply(environment: Environment): Task[Environment] =
    val boidsBounded = environment.all.map(avoidWall)
    actuator(environment.replaceBoidsWith(boidsBounded))

  def avoidWall(boid: Boid): Boid =
    val position = boid.position
    val forceLeft = Vector2D(forceFromDistance(position.distance(Vector2D(bound.bottomLeft.x, position.y))), 0)
    val forceRight = Vector2D(-forceFromDistance(position.distance(Vector2D(bound.topRight.x, position.y))), 0)
    val forceTop = Vector2D(0, -forceFromDistance(position.distance(Vector2D(position.x, bound.topRight.y))))
    val forceBottom = Vector2D(0, forceFromDistance(position.distance(Vector2D(position.x, bound.bottomLeft.y))))
    boid.focus(_.velocity).modify(v => (v + forceLeft + forceRight + forceTop + forceBottom).limit(1))

  private def forceFromDistance(distance: Double): Double =
    1 / math.sqrt(distance + 1) * 1.7
