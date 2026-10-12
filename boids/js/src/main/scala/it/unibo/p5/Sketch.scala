package it.unibo.p5

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/** A p5 sketch in instance mode, for a canvas of its own next to the global one (`main.js` exposes `p5`). */
@js.native
trait Sketch extends js.Object:
  var setup: js.Function0[Unit] = js.native
  var draw: js.Function0[Unit] = js.native
  var windowResized: js.Function0[Unit] = js.native
  val WEBGL: String = js.native
  val windowWidth: Double = js.native
  val windowHeight: Double = js.native
  val width: Double = js.native
  val height: Double = js.native
  def createCanvas(width: Double, height: Double, renderer: String): Element = js.native
  def resizeCanvas(width: Double, height: Double): Unit = js.native
  def background(color: String): Unit = js.native
  def orbitControl(): Unit = js.native
  def camera(
      x: Double,
      y: Double,
      z: Double,
      atX: Double,
      atY: Double,
      atZ: Double,
      upX: Double,
      upY: Double,
      upZ: Double
  ): Unit = js.native
  def ambientLight(gray: Double): Unit = js.native
  // with fewer arguments p5 takes the numbers as the colour channels before the direction
  def directionalLight(red: Double, green: Double, blue: Double, x: Double, y: Double, z: Double): Unit = js.native
  def noStroke(): Unit = js.native
  def rotateX(angle: Double): Unit = js.native
  def translate(x: Double, y: Double): Unit = js.native
  def createVector(x: Double, y: Double, z: Double): js.Object = js.native
  def model(geometry: Geometry): Unit = js.native
  def freeGeometry(geometry: Geometry): Unit = js.native

object Sketch:
  def apply(logic: js.Function1[Sketch, Unit], container: Element): Sketch =
    js.Dynamic.newInstance(js.Dynamic.global.p5)(logic, container.elt).asInstanceOf[Sketch]

/** A mesh that WEBGL sketches draw with `model`, uploaded to the GPU once and freed with `freeGeometry`. */
@js.native
@JSGlobal("p5.Geometry")
class Geometry extends js.Object:
  var vertices: js.Array[js.Object] = js.native
  var faces: js.Array[js.Array[Int]] = js.native

  /** Red, green, blue and alpha of each vertex in turn, from zero to one. */
  var vertexColors: js.Array[Double] = js.native
  def computeNormals(): Unit = js.native
