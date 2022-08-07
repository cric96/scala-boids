package it.unibo.boundary

import it.unibo.core.dynamics.Flocking
import ConfigurationStore.*
trait ConfigurationStore:
  def getCurrentConfig(): Config

object ConfigurationStore:
  case class Config(flockingWeights: Flocking.Weight, visionRange: Double, separationRange: Double)
