package it.unibo.render

import it.unibo.p5.{Element, Geometry, Sketch}

import scala.compiletime.uninitialized
import scala.scalajs.js

/** The density map as a lit relief, on a WEBGL canvas of its own that the mouse orbits and zooms. */
class ReliefView(container: Element):
  private var sketch: Sketch = uninitialized
  private var geometry: Option[Geometry] = None
  private var shown = false
  // the control panel lies above the canvas: dragging a slider must not turn the relief
  private var pointed = false

  // p5 may run setup before the constructor returns, so the instance is kept from the callback
  Sketch(
    instance => {
      sketch = instance
      instance.setup = () => setup()
      instance.draw = () => if (shown) draw()
      instance.windowResized = () => instance.resizeCanvas(instance.windowWidth, instance.windowHeight)
    },
    container
  )

  def show(visible: Boolean): Unit =
    shown = visible
    container.style("display", if (visible) "block" else "none")

  /** Replaces the relief on the GPU, freeing the previous one. */
  def update(relief: Relief): Unit =
    val next = Geometry()
    next.vertices = js.Array(
      relief.points
        .grouped(Relief.CoordinatesPerPoint)
        .map(point => sketch.createVector(point(0), point(1), point(2)))
        .toSeq*
    )
    next.faces = js.Array(relief.faces.grouped(Relief.CornersPerFace).map(face => js.Array(face.toSeq*)).toSeq*)
    next.vertexColors = js.Array(relief.densities.toSeq.flatMap(density => rgba(Palette.magma(density)))*)
    next.computeNormals()
    geometry.foreach(sketch.freeGeometry)
    geometry = Some(next)

  private def setup(): Unit =
    val canvas = sketch.createCanvas(sketch.windowWidth, sketch.windowHeight, sketch.WEBGL)
    canvas.elt.addEventListener("pointerenter", (_ => pointed = true): js.Function1[js.Any, Unit])
    canvas.elt.addEventListener("pointerleave", (_ => pointed = false): js.Function1[js.Any, Unit])
    // above the bottom side of the map, looking down at its centre, with the screen down as down
    val distance = ReliefView.Distance * sketch.width
    val (height, depth) = (distance * math.sin(ReliefView.Elevation), distance * math.cos(ReliefView.Elevation))
    sketch.camera(0, -height, depth, 0, 0, 0, 0, 1, 0)
    show(false)

  private def draw(): Unit =
    sketch.background(css(Palette.Sky))
    if (pointed) sketch.orbitControl()
    sketch.ambientLight(ReliefView.Ambient * Palette.ChannelMax)
    val (x, y, z) = ReliefView.LightDirection
    val gray = (1 - ReliefView.Ambient) / ReliefView.P5Diffuse * Palette.ChannelMax
    sketch.directionalLight(gray, gray, gray, x, y, z)
    sketch.noStroke()
    // lays the map on the floor, its hills rising, so that the mouse turns it around its own axis
    sketch.rotateX(math.Pi / 2)
    sketch.translate(-sketch.width / 2, -sketch.height / 2)
    geometry.foreach(sketch.model)

  private def rgba(rgb: Int): Seq[Double] =
    val (red, green, blue) = Palette.channels(rgb)
    Seq(red, green, blue, Palette.ChannelMax).map(_.toDouble / Palette.ChannelMax)

object ReliefView:
  /** How far above the floor the camera looks down at the map. */
  private val Elevation = math.Pi / 4

  /** Away from the centre of the map, in widths of the canvas: the whole map in sight. */
  private val Distance = 0.7

  /** The share of the light that reaches every face, the rest comes from [[LightDirection]]. */
  private val Ambient = 0.45

  /** The share of each lamp's light that p5 keeps: undone, so that the relief is as bright as on the JVM. */
  private val P5Diffuse = 0.73

  /** Where the light travels to: down and away from the viewer's left shoulder, lighting the slopes that face them. */
  private val LightDirection = (0.4, 1.0, -0.6)
