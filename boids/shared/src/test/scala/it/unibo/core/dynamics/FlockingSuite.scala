package it.unibo.core.dynamics

import it.unibo.Fixtures.{population, screen, tolerance}
import it.unibo.boundary.{ConfigurationStore, Parameter, Settings}
import it.unibo.core.geometry.Vector2D
import it.unibo.core.{Boid, GridEnvironment}

import cats.syntax.all.*

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

  test("flocking steers the same wherever the flock is on the torus"):
    val halfway = screen.center - screen.bottomLeft
    val shifted =
      GridEnvironment(start.all.map(boid => boid.copy(position = screen.wrap(boid.position + halfway))), screen)
    for {
      world <- dynamics(start)
      shiftedWorld <- dynamics(shifted)
    } yield world.all.zip(shiftedWorld.all).foreach { (boid, shiftedBoid) =>
      assertEqualsDouble(shiftedBoid.velocity.x, boid.velocity.x, tolerance)
      assertEqualsDouble(shiftedBoid.velocity.y, boid.velocity.y, tolerance)
    }

  test("a boid crossing a side of the screen enters from the opposite one"):
    val outwards = Seq(Vector2D(-1, 0), Vector2D(1, 0), Vector2D(0, -1), Vector2D(0, 1))
    outwards.traverse_ { side =>
      val acrossTheScreen = Vector2D(side.x * screen.width, side.y * screen.height)
      val boid = Boid(screen.center + acrossTheScreen / 2, side * Settings.constants.maxSpeed)
      dynamics(GridEnvironment(Seq(boid), screen)).map(world =>
        assertEquals(world.all.map(_.position), Seq(boid.position + boid.velocity - acrossTheScreen))
      )
    }

  test("no boid is faster than the speed cap"):
    dynamics(start).map(
      _.all.foreach(boid => assert(boid.velocity.norm <= Settings.constants.maxSpeed + tolerance, boid))
    )

  test("boids closer than the separation range drift apart with the default weights"):
    val heading = Vector2D(Settings.constants.maxSpeed, 0)
    val across = Vector2D(0, Parameter.SeparationRange.default / 2)
    val pair = Seq(Boid(screen.center, heading), Boid(screen.center + across, heading))
    dynamics(GridEnvironment(pair, screen)).map { world =>
      val Seq(first, second) = world.all: @unchecked
      assert(screen.offset(first.position, second.position).norm > across.norm, world.all)
    }

  test("boids sharing the same position stay finite"):
    val overlapping =
      GridEnvironment(
        Boid.generator(screen.center, Vector2D.randomUnitary).take(Settings.constants.neighbours).toVector,
        screen
      )
    dynamics(overlapping).map(
      _.all.foreach(boid => assert(boid.position.norm.isFinite && boid.velocity.norm.isFinite, boid))
    )
