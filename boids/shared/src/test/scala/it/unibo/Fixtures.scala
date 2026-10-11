package it.unibo

import it.unibo.boundary.Parameter
import it.unibo.core.geometry.{Rectangle2D, Vector2D}

/** Test fixtures shared by every suite, derived from the domain instead of picked per test. */
object Fixtures:
  /** The canvas of a full-HD screen, where the simulation usually runs. */
  val screen: Rectangle2D = Rectangle2D(Vector2D.zero, Vector2D(1920, 1080))

  val population: Int = Parameter.BoidsCount.default.toInt

  /** How many random cases each property is checked on. */
  val samples: Int = 100

  /** Slack for floating-point comparisons. */
  val tolerance: Double = 1e-9
