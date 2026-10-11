package it.unibo.boundary

import it.unibo.boundary.Parameter.*
import it.unibo.core.dynamics.Flocking

class ParameterSuite extends munit.CatsEffectSuite:
  test("every default lies within its bounds"):
    Parameter.values.foreach(p => assert(p.min <= p.default && p.default <= p.max, p))

  test("format rounds the value to the precision of the step"):
    assertEquals(Separation.format(2.3600000000000003), "2.36")
    assertEquals(BoidsCount.format(400), "400")
    assertEquals(MaxForce.format(0.03), "0.030")

  test("sections group the parameters keeping the declaration order"):
    assertEquals(Parameter.sections.map(_._1), List("Flocking", "Perception", "Density map", "Population"))
    assertEquals(Parameter.sections.flatMap(_._2), Parameter.values.toList)

  test("Config.from reads the flocking configuration from the parameter values"):
    val expected = ConfigurationStore.Config(Flocking.Weight(2, 1, 1), 50, 20, 0.03)
    assertEquals(ConfigurationStore.Config.from(_.default), expected)
