package it.unibo.core.dynamics

import it.unibo.boundary.ConfigurationStore
import it.unibo.core.Dynamics
import it.unibo.core.geometry.Rectangle2D

object DynamicFactory:
  def flocking(bound: Rectangle2D): ConfigurationStore.Config => Dynamics =
    config =>
      Dynamics.combine(
        Flocking(config.flockingWeights, config.visionRange, config.separationRange, config.maxForce),
        BorderForce(bound)
      )
