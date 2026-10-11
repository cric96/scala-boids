package it.unibo.core

import it.unibo.core.geometry.Vector2D.Vector2D

trait Environment:
  /** Up to `count` boids closer than `range` to `center` (excluded), preferring the nearest ones. */
  def nearTo(center: Boid, range: Double, count: Int): Seq[Boid]

  /** The shortest displacement from `from` to `to` in the space of this environment. */
  def offset(from: Vector2D, to: Vector2D): Vector2D
  def all: Seq[Boid]
  def replaceBoidsWith(boids: Seq[Boid]): Environment
