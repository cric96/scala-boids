package it.unibo
import it.unibo.boundary.Parameter
import p5.Element
import p5.api.*

/** A labelled range input bound to a [[Parameter]], showing its live value and the filled part of the track. */
class Slider(parameter: Parameter, container: Element):
  private val row = createDiv().addClass("control").parent(container)
  private val header = createDiv().addClass("control-header").parent(row)
  createSpan(parameter.label).parent(header)
  private val output = createSpan().addClass("control-value").parent(header)
  private val slider =
    createSlider(parameter.min, parameter.max, parameter.default, parameter.step).parent(row)
  slider.input(_ => refresh())
  refresh()

  def value: Double = slider.value() match
    case s: String => s.toDouble
    case other: Double => other

  def reset(): Unit =
    slider.value(parameter.default)
    refresh()

  private def refresh(): Unit =
    output.html(parameter.format(value))
    slider.elt.style.setProperty("--fill", s"${(value - parameter.min) / (parameter.max - parameter.min) * 100}%")
