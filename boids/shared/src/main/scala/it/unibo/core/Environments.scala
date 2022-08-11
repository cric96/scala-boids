package it.unibo.core

import it.unibo.core.geometry.{Rectangle2D, Vector2D}

import scala.util.Random

object Environments:
  def setupBoids(boundingBox: Rectangle2D, boidsCount: Int)(using Random): Environment =
    val centeringFactor = boundingBox.width / 10
    val center = boundingBox.center
    val delta = Vector2D(centeringFactor, centeringFactor)
    val generatorBox = Rectangle2D(center - delta, center + delta)
    val boids = Boid
      .generator(Vector2D.randomPositionIn(generatorBox), Vector2D.randomUnitary)
      .take(boidsCount)
    UnboundedRTreeEnvironment(boids)
