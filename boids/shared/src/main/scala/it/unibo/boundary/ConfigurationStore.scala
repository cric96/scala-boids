package it.unibo.boundary

import it.unibo.core.dynamics.Flocking
import ConfigurationStore.*
import Parameter.*
trait ConfigurationStore:
  def getCurrentConfig(): Config

object ConfigurationStore:
  case class Config(flockingWeights: Flocking.Weight, visionRange: Double, separationRange: Double, maxForce: Double)

  object Config:
    def from(value: Parameter => Double): Config =
      Config(
        Flocking.Weight(value(Separation), value(Align), value(Cohesion)),
        value(VisionRange),
        value(SeparationRange),
        value(MaxForce)
      )
