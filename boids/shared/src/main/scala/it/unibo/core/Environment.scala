package it.unibo.core

trait Environment:
  /** Up to `count` boids closer than `range` to `center` (excluded), preferring the nearest ones. */
  def nearTo(center: Boid, range: Double, count: Int): Seq[Boid]
  def all: Seq[Boid]
  def replaceBoidsWith(boids: Seq[Boid]): Environment
