package it.unibo.core.simulation

import it.unibo.Fixtures.{population, screen}
import it.unibo.boundary.ConfigurationStore
import it.unibo.core.dynamics.DynamicFactory
import it.unibo.core.{Environment, Environments}

import scala.collection.mutable.ListBuffer
import scala.util.Random

class SimulationSuite extends munit.CatsEffectSuite:
  private val config = ConfigurationStore.Config.from(_.default)
  private val steps = 10

  test("loopFor renders one frame per step"):
    val frames = ListBuffer.empty[Environment]
    val environment = Environments.setupBoids(screen, population)(using Random(0))
    val simulation = Simulation(frames.addOne(_), environment, () => config, DynamicFactory.flocking(screen))
    simulation.loopFor(steps).map(_ => assertEquals(frames.map(_.all.size).toList, List.fill(steps)(population)))
