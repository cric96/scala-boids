package it.unibo.core.dynamics

import it.unibo.boundary.{Parameter, Settings}
import it.unibo.core.dynamics.Flocking.{Mate, Weight}
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
    val mates = environment
      .nearTo(center, visionRange.max(separationRange), neighbours)
      .map(other => Mate(environment.offset(center.position, other.position), other.velocity))
    val closerThan = (range: Double) => mates.filter(_.offset.norm < range)
    applyFlockingFactor(center, closerThan(visionRange), closerThan(separationRange))

  private def applyFlockingFactor(center: Boid, mates: Seq[Mate], separationMates: Seq[Mate]): Boid =
    val separationForce = separation(center, separationMates) * maxForce
    val alignForce = align(center, mates) * maxForce
    val cohesionForce = cohesion(center, mates) * maxForce
    val forces = separationForce * weights.separation + alignForce * weights.align + cohesionForce * weights.cohesion
    center.focus(_.velocity).modify(velocity => (velocity + forces).limit(maxSpeed))

  private def separation(center: Boid, mates: Seq[Mate]): Vector2D =
    val separationForces = mates
      .map(mate => zero - mate.offset)
      .collect { case away if away.norm > 0 => away.normalize * maxSpeed / away.norm }

    if (separationForces.isEmpty) { zero }
    else
      val combinedForces = separationForces.reduce(_ + _) / separationForces.size
      combinedForces.normalize - center.velocity
  private def align(center: Boid, mates: Seq[Mate]): Vector2D =
    if (mates.isEmpty) zero
    else
      val allVelocities = mates.map(_.velocity).reduce(_ + _) / mates.size
      val normalized = allVelocities.normalize * maxSpeed
      normalized - center.velocity
  private def cohesion(center: Boid, mates: Seq[Mate]): Vector2D =
    if (mates.isEmpty) zero
    else
      val direction = mates.map(_.offset).reduce(_ + _) / mates.size
      val normalized = direction.normalize * maxSpeed
      normalized - center.velocity
object Flocking:
  case class Weight(separation: Double, align: Double, cohesion: Double)

  /** A mate as a boid sees it: where it is relative to the boid, on the shortest way, and how it moves. */
  private case class Mate(offset: Vector2D, velocity: Vector2D)
