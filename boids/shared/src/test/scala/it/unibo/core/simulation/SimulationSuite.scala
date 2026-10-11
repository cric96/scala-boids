package it.unibo.core.simulation

import it.unibo.boundary.ConfigurationStore
import it.unibo.core.dynamics.{DynamicFactory, Flocking}
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.{Environment, Environments}

import scala.collection.mutable.ListBuffer
import scala.util.Random

class SimulationSuite extends munit.CatsEffectSuite:
  private val bounds = Rectangle2D(Vector2D(0, 0), Vector2D(100, 100))
  private val config = ConfigurationStore.Config(Flocking.Weight(1, 1, 1), 50, 10, 0.03)

  test("loopFor renders one frame per step"):
    val frames = ListBuffer.empty[Environment]
    val environment = Environments.setupBoids(bounds, 10)(using Random(0))
    val simulation = Simulation(frames.addOne(_), environment, () => config, DynamicFactory.flocking(bounds))
    simulation.loopFor(3).map(_ => assertEquals(frames.map(_.all.size).toList, List(10, 10, 10)))
