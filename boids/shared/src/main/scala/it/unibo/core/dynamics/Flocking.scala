package it.unibo.core.dynamics

import it.unibo.core.dynamics.Flocking.Weight
import it.unibo.core.geometry.Vector2D.{Vector2D, zero}
import it.unibo.core.{Boid, Dynamics, Environment}
import monocle.syntax.all._
class Flocking(weights: Weight, visionRange: Double, separationRange: Double) extends Dynamics:
  private val actuator = LinearVelocity()
  private val maxForce = 0.03
  private val maxSpeed = 2
  override def apply(environment: Environment): Environment =
    val updatedBoids = environment.replaceBoidsWith(
      environment.all
        .map(center => center -> environment.nearTo(center, visionRange))
        .map((center, neighborhood) => applyFlockingFactor(center, neighborhood))
    )
    actuator(updatedBoids)

  private def applyFlockingFactor(center: Boid, neighborhood: Seq[Boid]): Boid =
    val separationForce = separation(center, neighborhood) * maxForce
    val alignForce = align(center, neighborhood) * maxForce
    val cohesionForce = cohesion(center, neighborhood) * maxForce
    val forces = separationForce * weights.separation + alignForce * weights.align + cohesionForce * weights.cohesion
    center.focus(_.velocity).modify(velocity => (velocity + forces).limit(maxSpeed))

  private def separation(center: Boid, neighborhood: Seq[Boid]): Vector2D =
    val separationForces = for {
      other <- neighborhood
      position = other.position
      distance = center.position.distance(position)
      if distance < visionRange
      difference = center.position - position
      normalize = difference.normalize * maxSpeed
    } yield (normalize / distance)

    if (separationForces.isEmpty) { zero }
    else
      val combinedForces = separationForces.reduce(_ + _) / separationForces.size
      combinedForces.normalize - center.velocity
  private def align(center: Boid, neighborhood: Seq[Boid]): Vector2D =
    if (neighborhood.isEmpty) zero
    else
      val allVelocities = neighborhood.map(_.velocity).reduce(_ + _) / neighborhood.size
      val normalized = allVelocities.normalize * maxSpeed
      normalized - center.velocity
  private def cohesion(center: Boid, neighborhood: Seq[Boid]): Vector2D =
    if (neighborhood.isEmpty) zero
    else
      val seekPosition = neighborhood.map(_.position).reduce(_ + _) / neighborhood.size
      val direction = seekPosition - center.position
      val normalized = direction.normalize * maxSpeed
      normalized - center.velocity
object Flocking:
  case class Weight(separation: Double, align: Double, cohesion: Double)
