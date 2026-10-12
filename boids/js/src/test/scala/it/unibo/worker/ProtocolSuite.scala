package it.unibo.worker

import it.unibo.Fixtures.{population, screen}
import it.unibo.boundary.Parameter
import it.unibo.core.{Boid, Environments}

import scala.util.Random

class ProtocolSuite extends munit.CatsEffectSuite:
  test("boids reach the page as the worker sent them"):
    val boids = Environments.setupBoids(screen, population)(using Random(0)).all
    assertEquals(Protocol.decodeBoids(Protocol.encodeBoids(boids)), boids)

  test("no boids reach the page as no boids"):
    assertEquals(Protocol.decodeBoids(Protocol.encodeBoids(Seq.empty)), Seq.empty[Boid])

  test("settings reach the worker as the page sent them"):
    val sent = Protocol.Settings(Parameter.values.map(parameter => parameter -> parameter.max).toMap, screen)
    assertEquals(Protocol.decodeSettings(Protocol.encodeSettings(sent)), sent)
