package it.unibo.core
import it.unibo.core.geometry.Vector2D.*

case class Boid(position: Vector2D, velocity: Vector2D)

object Boid:
  def generator(
      positionGenerator: => Vector2D,
      velocityGenerator: => Vector2D
  ): LazyList[Boid] = LazyList.continually(Boid(positionGenerator, velocityGenerator))
