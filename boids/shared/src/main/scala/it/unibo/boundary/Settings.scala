package it.unibo.boundary

import upickle.default.*

/** Everything tunable in the simulation, read from `parameters.json`. */
case class Settings(parameters: Map[String, Settings.Range], constants: Settings.Constants) derives ReadWriter

object Settings:
  /** The bounds, the default and the step of a [[Parameter]], with how the GUIs present it. */
  case class Range(label: String, section: String, min: Double, max: Double, default: Double, step: Double)
      derives ReadWriter

  /** @param maxSpeed
    *   speed cap of every boid, in pixels per step
    * @param neighbours
    *   how many mates a boid follows at most: like starlings only the nearest ones, which also bounds the work per boid
    */
  case class Constants(maxSpeed: Double, neighbours: Int) derives ReadWriter

  lazy val current: Settings = parse(ParametersJson.text)
  def constants: Constants = current.constants

  def parse(json: String): Settings =
    val settings = read[Settings](json)
    val expected = Parameter.values.map(_.toString).toSet
    require(settings.parameters.keySet == expected, s"parameters.json must define exactly ${expected.mkString(", ")}")
    settings
