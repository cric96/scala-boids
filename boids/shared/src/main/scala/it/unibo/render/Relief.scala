package it.unibo.render

import it.unibo.core.DensityMap

/** The density map as a landscape: a vertex above the centre of every cell, as high as the cell is dense.
  *
  * @param points
  *   x, y and height of each vertex, cell by cell in the order of the map
  * @param densities
  *   the density of each vertex, from zero to one, to colour it
  * @param faces
  *   the vertices of each triangle, counterclockwise seen from above the map
  */
case class Relief(points: IArray[Float], densities: IArray[Float], faces: IArray[Int])

object Relief:
  val CoordinatesPerPoint = 3
  val CornersPerFace = 3

  /** How high the densest cell rises, as a fraction of the shorter side of the map. */
  val HeightRatio: Double = 0.25

  def apply(densityMap: DensityMap): Relief =
    val height = HeightRatio * (densityMap.columns * densityMap.cell).min(densityMap.rows * densityMap.cell)
    val cells = densityMap.cells
    val points = cells.flatMap { (column, row) =>
      val centre = densityMap.centre(column, row)
      Seq(centre.x, centre.y, densityMap.density(column, row) * height)
    }
    def vertex(column: Int, row: Int): Int = row * densityMap.columns + column
    val faces = for {
      row <- 0 until densityMap.rows - 1
      column <- 0 until densityMap.columns - 1
      (a, b, c, d) = (
        vertex(column, row),
        vertex(column + 1, row),
        vertex(column + 1, row + 1),
        vertex(column, row + 1)
      )
      corner <- Seq(a, b, c, a, c, d)
    } yield corner
    Relief(
      IArray.from(points.map(_.toFloat)),
      IArray.from(cells.map(densityMap.density.tupled(_).toFloat)),
      IArray.from(faces)
    )
