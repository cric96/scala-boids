package it.unibo.worker

import it.unibo.boundary.Parameter
import it.unibo.core.Boid
import it.unibo.core.geometry.{Rectangle2D, Vector2D}
import it.unibo.core.geometry.Vector2D.Vector2D

import scala.scalajs.js.typedarray.*

/** What the page and the simulation worker tell each other, as flat arrays of numbers that cross threads cheaply. */
object Protocol:
  /** The parameters of the simulation and the bounds of the canvas it runs on. */
  case class Settings(values: Map[Parameter, Double], bounds: Rectangle2D)

  def encodeBoids(boids: Seq[Boid]): Float64Array = numbers(boids.flatMap(boid => Seq(boid.position, boid.velocity)))
  def decodeBoids(data: Float64Array): Seq[Boid] =
    vectors(data.toArray).grouped(2).map(pair => Boid(pair.head, pair.last)).toVector

  def encodeSettings(settings: Settings): Float64Array =
    val bounds = numbers(Seq(settings.bounds.bottomLeft, settings.bounds.topRight)).toArray
    (Parameter.values.map(settings.values) ++ bounds).toTypedArray
  def decodeSettings(data: Float64Array): Settings =
    val (values, bounds) = data.toArray.splitAt(Parameter.values.length)
    val corners = vectors(bounds)
    Settings(Parameter.values.zip(values).toMap, Rectangle2D(corners.head, corners.last))

  private def numbers(vectors: Seq[Vector2D]): Float64Array = vectors.flatMap(v => Seq(v.x, v.y)).toArray.toTypedArray
  private def vectors(numbers: Array[Double]): Seq[Vector2D] =
    numbers.grouped(2).map(pair => Vector2D(pair.head, pair.last)).toVector
