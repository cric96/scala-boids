package it.unibo.render

import it.unibo.core.Boid
import it.unibo.core.geometry.Vector2D
import it.unibo.core.geometry.Vector2D.Vector2D

/** The triangle drawn for a boid: its tip ahead along the velocity, its base behind and across it. */
object Glyph:
  /** From the boid to the tip, in pixels: small enough for thousands of boids on a screen. */
  val Size: Double = 6

  /** The tip, then the two corners of the base. */
  def vertices(boid: Boid): (Vector2D, Vector2D, Vector2D) =
    val forward = boid.velocity.normalize
    val side = Vector2D(-forward.y, forward.x)
    val base = boid.position - forward * (Size / 2)
    (boid.position + forward * Size, base + side * (Size / 2), base - side * (Size / 2))
