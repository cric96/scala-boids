package it.unibo.core.dynamics

import it.unibo.core.dynamics.Flocking.Weight
import it.unibo.core.geometry.Vector2D.{Vector2D, zero}
import it.unibo.core.{Boid, Dynamics, Environment}
import monix.eval.Task
import monocle.syntax.all.*
import monix.execution.Scheduler.Implicits.global
class Flocking(weights: Weight, visionRange: Double, separationRange: Double) extends Dynamics:
  private val actuator = LinearVelocity()
  private val maxForce = 0.03
  private val maxSpeed = 2
  override def apply(environment: Environment): Task[Environment] =
    val centers = environment.all
      .map(center =>
        Task(center, environment.nearTo(center, visionRange), environment.nearTo(center, separationRange))
          .map((center, neighborhood, separationNeighborhood) =>
            applyFlockingFactor(center, neighborhood, separationNeighborhood)
          )
      )

    Task.parSequence(centers).flatMap(evaluation => actuator(environment.replaceBoidsWith(evaluation)))

  private def applyFlockingFactor(center: Boid, neighborhood: Seq[Boid], separationNeighborhood: Seq[Boid]): Boid =
    val separationForce = separation(center, separationNeighborhood) * maxForce
    val alignForce = align(center, neighborhood) * maxForce
    val cohesionForce = cohesion(center, neighborhood) * maxForce
    val forces = separationForce * weights.separation + alignForce * weights.align + cohesionForce * weights.cohesion
    center.focus(_.velocity).modify(velocity => (velocity + forces).limit(maxSpeed))

  private def separation(center: Boid, neighborhood: Seq[Boid]): Vector2D =
    val separationForces = for {
      other <- neighborhood
      position = other.position
      distance = center.position.distance(position)
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
