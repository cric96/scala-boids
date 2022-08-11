package it.unibo.render

import java.awt.{Color, Dimension}
import javax.swing.{JComponent, JLabel, JPanel, JSlider}

private class Slider(name: String, start: Double, end: Double, initialValue: Double):
  private val context = JPanel()
  private val slider = JSlider(0, 100, reverseValue(initialValue))
  private val label = JLabel(s"$name $initialValue")
  context.setPreferredSize(Dimension(200, 10))
  context.setBackground(Color(0, 0, 0, 0))
  slider.setBackground(Color(0, 0, 0, 0))
  label.setForeground(Color(255, 255, 255))
  context.add(slider)
  context.add(label)
  def render: JComponent = context
  def update: Unit = label.setText(s"$name $value")
  def value: Double = (slider.getValue / 100.0) * (end - start) + start
  private def reverseValue(double: Double): Int = ((double - start) / (end - start) * 100).toInt
