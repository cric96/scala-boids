package it.unibo
import p5.api.*

import scala.scalajs.js
class Slider(
    name: String,
    minBound: Double,
    maxBound: Double,
    initialValue: Double,
    position: (Double, Double),
    ticks: Boolean = true
):
  private val slider =
    if (ticks)
      createSlider(minBound, maxBound, initialValue, (maxBound - minBound) / 100)
    else
      createSlider(minBound, maxBound, initialValue)
  private val size = 150
  private val textSizeFont = 15
  slider.position(position._1, position._2)
  def render(): Unit =
    push()
    textSize(textSizeFont);
    fill(255)
    text(s"$name : ${slider.value()}", position._1 + size, position._2 + textSizeFont);
    pop()
  def value: Double = slider.value() match
    case s: String => s.toDouble
    case other: Double => other
