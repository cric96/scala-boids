package it.unibo.render

import it.unibo.Fixtures.{population, samples, screen}
import it.unibo.boundary.Parameter
import it.unibo.core.{Environments, DensityMap}

import scala.util.Random

class PaletteSuite extends munit.CatsEffectSuite:
  private val densities = (0 to samples).map(_.toDouble / samples)

  /** ITU-R BT.709 weights of the red, green and blue channels in the luminance. */
  private val weights = (0.2126, 0.7152, 0.0722)
  private val white = Palette.rgb(Palette.ChannelMax, Palette.ChannelMax, Palette.ChannelMax)

  private def luminance(rgb: Int): Double =
    val (red, green, blue) = Palette.channels(rgb)
    weights._1 * red + weights._2 * green + weights._3 * blue

  test("a colour is made of the channels it is split into"):
    val channels = (Palette.ChannelMax, Palette.ChannelMax / 2, 0)
    assertEquals(Palette.channels(Palette.rgb.tupled(channels)), channels)

  test("a denser cell is never darker than a sparser one"):
    val luminances = densities.map(density => luminance(Palette.magma(density)))
    assertEquals(luminances, luminances.sorted)

  test("the empty and the densest cells differ by more than half the brightness range"):
    assert(luminance(Palette.magma(1)) - luminance(Palette.magma(0)) > luminance(white) / 2)

  test("densities out of range take the colour of the nearest end"):
    assertEquals(Palette.magma(Double.NegativeInfinity), Palette.magma(0))
    assertEquals(Palette.magma(Double.PositiveInfinity), Palette.magma(1))

  test("the heatmap has a pixel per cell, row by row, in the colour of the density of the cell"):
    val environment = Environments.setupBoids(screen, population)(using Random(0))
    val densityMap = DensityMap(screen, Parameter.BucketSize.default, Parameter.BucketRange.default, environment)
    val heatmap = Palette.heatmap(densityMap)
    densityMap.cells.foreach((column, row) =>
      assertEquals(heatmap(row * densityMap.columns + column), Palette.magma(densityMap.density(column, row)))
    )
