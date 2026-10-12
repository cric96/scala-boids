package it.unibo.render

import it.unibo.core.DensityMap

/** The colours of the rendering as packed rgb integers, shared so that the JVM and the browser look the same. */
object Palette:
  val ChannelMax: Int = 0xff
  private val ChannelBits = 8

  /** Matplotlib's magma at even steps: perceptually uniform, from the black of empty space to light yellow. */
  val Magma: IndexedSeq[Int] =
    IndexedSeq(0x000004, 0x1d1147, 0x51127c, 0x822681, 0xb63679, 0xe65164, 0xfb8861, 0xfec287, 0xfcfdbf)

  /** Empty space, as dark as the emptiest cell so that the heatmap fades into it. */
  val Background: Int = Magma.head

  /** The colour of a density between zero and one, interpolated between the nearest stops of [[Magma]]. */
  def magma(density: Double): Int =
    val position = density.max(0).min(1) * (Magma.size - 1)
    val below = position.floor.toInt.min(Magma.size - 2)
    mix(Magma(below), Magma(below + 1), position - below)

  /** One pixel per cell of the map, row by row. */
  def heatmap(densityMap: DensityMap): IndexedSeq[Int] =
    densityMap.cells.map((column, row) => magma(densityMap.density(column, row))).toIndexedSeq

  def rgb(red: Int, green: Int, blue: Int): Int = (red << 2 * ChannelBits) | (green << ChannelBits) | blue
  def channels(rgb: Int): (Int, Int, Int) =
    ((rgb >> 2 * ChannelBits) & ChannelMax, (rgb >> ChannelBits) & ChannelMax, rgb & ChannelMax)

  private def mix(from: Int, to: Int, weight: Double): Int =
    val ((fromRed, fromGreen, fromBlue), (toRed, toGreen, toBlue)) = (channels(from), channels(to))
    def channel(from: Int, to: Int): Int = (from + (to - from) * weight).round.toInt
    rgb(channel(fromRed, toRed), channel(fromGreen, toGreen), channel(fromBlue, toBlue))
