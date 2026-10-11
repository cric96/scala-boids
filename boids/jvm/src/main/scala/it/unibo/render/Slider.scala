package it.unibo.render

import it.unibo.boundary.Parameter

import java.awt.BorderLayout
import javax.swing.{BorderFactory, JComponent, JLabel, JPanel, JSlider}

/** A labelled slider bound to a [[Parameter]]: the JSlider moves over integer ticks, one per parameter step. */
private class Slider(parameter: Parameter):
  private val slider = JSlider(0, tickOf(parameter.max), tickOf(parameter.default))
  private val output = JLabel()
  private val context = JPanel(BorderLayout())
  private val header = JPanel(BorderLayout())
  output.setForeground(Theme.accent)
  header.add(JLabel(parameter.label), BorderLayout.WEST)
  header.add(output, BorderLayout.EAST)
  context.add(header, BorderLayout.NORTH)
  context.add(slider, BorderLayout.CENTER)
  context.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0))
  Seq(context, header, slider).foreach(_.setOpaque(false))
  slider.addChangeListener(_ => refresh())
  refresh()

  def render: JComponent = context
  def value: Double = parameter.min + slider.getValue * parameter.step
  def reset(): Unit = slider.setValue(tickOf(parameter.default))
  private def refresh(): Unit = output.setText(parameter.format(value))
  private def tickOf(value: Double): Int = ((value - parameter.min) / parameter.step).round.toInt
