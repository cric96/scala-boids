package it.unibo.core

import it.unibo.core.geometry.Vector2D.Vector2D

trait Environment:
  /** The `count` boids closest to `center` (excluded), closer than `range`. */
  def nearTo(center: Boid, range: Double, count: Int): Seq[Boid]
  def getAllIn(topLeft: Vector2D, bottomRight: Vector2D): Seq[Boid]
  def all: Seq[Boid]
  def replaceBoidsWith(boids: Seq[Boid]): Environment
