package it.unibo.core.dynamics

import it.unibo.boundary.{Parameter, Settings}
import it.unibo.core.dynamics.Flocking.Weight
import it.unibo.core.geometry.Vector2D.{Vector2D, zero}
import it.unibo.core.{Boid, Dynamics, Environment}
import cats.effect.IO
import cats.syntax.all.*
import monocle.syntax.all.*
class Flocking(
    weights: Weight,
    visionRange: Double,
    separationRange: Double,
    maxForce: Double = Parameter.MaxForce.default,
    maxSpeed: Double = Settings.constants.maxSpeed,
    neighbours: Int = Settings.constants.neighbours
) extends Dynamics:
  override def apply(environment: Environment): IO[Environment] =
    environment.all
      .grouped(chunkSize(environment.all.size))
      .toVector
      .parTraverse(chunk => IO(chunk.map(steer(environment))))
      .map(evaluation => environment.replaceBoidsWith(evaluation.flatten))

  /** One chunk per core: a fiber per boid costs more than the boid itself (and Scala.js has a single core). */
  private def chunkSize(boids: Int): Int =
    math.ceil(boids.toDouble / Runtime.getRuntime.availableProcessors).toInt.max(1)

  private def steer(environment: Environment)(center: Boid): Boid =
    val neighbourhood = environment.nearTo(center, visionRange.max(separationRange), neighbours)
    val closerThan = (range: Double) => neighbourhood.filter(_.position.distance(center.position) < range)
    applyFlockingFactor(center, closerThan(visionRange), closerThan(separationRange))

  private def applyFlockingFactor(center: Boid, neighborhood: Seq[Boid], separationNeighborhood: Seq[Boid]): Boid =
    val separationForce = separation(center, separationNeighborhood) * maxForce
    val alignForce = align(center, neighborhood) * maxForce
    val cohesionForce = cohesion(center, neighborhood) * maxForce
    val forces = separationForce * weights.separation + alignForce * weights.align + cohesionForce * weights.cohesion
    center.focus(_.velocity).modify(velocity => (velocity + forces).limit(maxSpeed))

  private def separation(center: Boid, neighborhood: Seq[Boid]): Vector2D =
    val separationForces = neighborhood
      .map(other => center.position - other.position)
      .collect { case difference if difference.norm > 0 => difference.normalize * maxSpeed / difference.norm }

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
