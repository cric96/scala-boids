package it.unibo.core.dynamics

import it.unibo.Fixtures.{population, screen, tolerance}
import it.unibo.boundary.{ConfigurationStore, Settings}
import it.unibo.core.geometry.Vector2D
import it.unibo.core.{Boid, GridEnvironment}

import scala.util.Random

class FlockingSuite extends munit.CatsEffectSuite:
  private given random: Random = Random(0)
  private val dynamics = DynamicFactory.flocking(screen)(ConfigurationStore.Config.from(_.default))
  // up to twice the cap, so that the steering has to slow some boids down
  private val start = GridEnvironment(
    Boid
      .generator(
        Vector2D.randomPositionIn(screen),
        Vector2D.randomUnitary * (random.nextDouble() * 2 * Settings.constants.maxSpeed)
      )
      .take(population)
      .toVector,
    screen
  )

  test("a step moves every boid by its new velocity exactly once"):
    dynamics(start).map(world =>
      start.all
        .zip(world.all)
        .foreach((before, after) => assertEquals(after.position, screen.wrap(before.position + after.velocity)))
    )

  test("boids crossing a side of the screen enter from the opposite one"):
    val outwards = Seq(Vector2D(-1, 0), Vector2D(1, 0), Vector2D(0, -1), Vector2D(0, 1))
    val crossing = outwards.map(side =>
      val middleOfSide = screen.center + Vector2D(side.x * screen.width, side.y * screen.height) / 2
      Boid(middleOfSide, side * Settings.constants.maxSpeed)
    )
    dynamics(GridEnvironment(crossing, screen)).map(world =>
      outwards.zip(crossing).zip(world.all).foreach { case ((side, before), after) =>
        val acrossTheScreen = Vector2D(side.x * screen.width, side.y * screen.height)
        assertEquals(after.position, before.position + before.velocity - acrossTheScreen)
      }
    )

  test("no boid is faster than the speed cap"):
    dynamics(start).map(
      _.all.foreach(boid => assert(boid.velocity.norm <= Settings.constants.maxSpeed + tolerance, boid))
    )

  test("boids sharing the same position stay finite"):
    val overlapping =
      GridEnvironment(
        Boid.generator(screen.center, Vector2D.randomUnitary).take(Settings.constants.neighbours).toVector,
        screen
      )
    dynamics(overlapping).map(
      _.all.foreach(boid => assert(boid.position.norm.isFinite && boid.velocity.norm.isFinite, boid))
    )
