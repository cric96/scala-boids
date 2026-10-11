package it.unibo.boundary

/** The tunable parameters of the simulation, shared by every GUI so that they all expose the same controls. */
enum Parameter:
  case Separation, Align, Cohesion, MaxForce, VisionRange, SeparationRange, BucketSize, BucketRange, BoidsCount

  def label: String = range.label
  def section: String = range.section
  def min: Double = range.min
  def max: Double = range.max
  def default: Double = range.default
  def step: Double = range.step

  def format(value: Double): String = BigDecimal(value).setScale(decimals, BigDecimal.RoundingMode.HALF_UP).toString
  private def decimals: Int = BigDecimal(step).underlying.stripTrailingZeros.scale max 0
  private def range: Settings.Range = Settings.current.parameters(toString)

object Parameter:
  def sections: List[(String, List[Parameter])] =
    values.toList.map(_.section).distinct.map(section => section -> values.toList.filter(_.section == section))
