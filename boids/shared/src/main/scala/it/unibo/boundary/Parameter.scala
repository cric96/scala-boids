package it.unibo.boundary

/** The tunable parameters of the simulation, shared by every GUI so that they all expose the same controls. */
enum Parameter(
    val label: String,
    val section: String,
    val min: Double,
    val max: Double,
    val default: Double,
    val step: Double
):
  case Separation extends Parameter("Separation", "Flocking", 0, 3, 2, 0.01)
  case Align extends Parameter("Alignment", "Flocking", 0, 3, 1, 0.01)
  case Cohesion extends Parameter("Cohesion", "Flocking", 0, 3, 1, 0.01)
  case MaxForce extends Parameter("Max force", "Flocking", 0.01, 0.1, 0.03, 0.001)
  case VisionRange extends Parameter("Vision range", "Perception", 20, 200, 50, 1)
  case SeparationRange extends Parameter("Separation range", "Perception", 5, 100, 20, 1)
  case BucketSize extends Parameter("Cell size", "Density map", 5, 40, 20, 1)
  case BucketRange extends Parameter("Density radius", "Density map", 20, 200, 100, 1)
  case BoidsCount extends Parameter("Boids", "Population", 100, 2000, 500, 10)

  def format(value: Double): String = BigDecimal(value).setScale(decimals, BigDecimal.RoundingMode.HALF_UP).toString
  private def decimals: Int = BigDecimal(step).underlying.stripTrailingZeros.scale max 0

object Parameter:
  def sections: List[(String, List[Parameter])] =
    values.toList.map(_.section).distinct.map(section => section -> values.toList.filter(_.section == section))
