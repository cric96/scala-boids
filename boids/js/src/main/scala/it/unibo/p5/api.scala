package it.unibo.p5

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope
import scala.scalajs.js.|

/** The subset of p5.js global-mode functions used by the app (p5 itself is imported by `main.js`). */
@js.native
@JSGlobalScope
object api extends js.Object:
  val width: Double = js.native
  val height: Double = js.native
  val windowWidth: Double = js.native
  val windowHeight: Double = js.native
  val HSB: String = js.native
  val HALF_PI: Double = js.native
  def createCanvas(width: Double, height: Double): Element = js.native
  def createButton(label: String): Element = js.native
  def createSlider(min: Double, max: Double, value: Double = js.native, step: Double = js.native): Element = js.native
  def noLoop(): Unit = js.native
  def redraw(): Unit = js.native
  def background(gray: Double): Unit = js.native
  def colorMode(mode: String): Unit = js.native
  def fill(v1: Double, v2: Double = js.native, v3: Double = js.native, alpha: Double = js.native): Unit = js.native
  def noStroke(): Unit = js.native
  def push(): Unit = js.native
  def pop(): Unit = js.native
  def translate(x: Double, y: Double): Unit = js.native
  def rotate(angle: Double): Unit = js.native
  def rect(x: Double, y: Double, width: Double, height: Double): Unit = js.native
  def triangle(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double): Unit = js.native
  def textSize(size: Double): Unit = js.native
  def text(content: String, x: Double, y: Double): Unit = js.native

@js.native
trait Element extends js.Object:
  def position(x: Double, y: Double): Unit = js.native
  def style(property: String, value: String): Unit = js.native
  def value(): String | Double = js.native
  def mousePressed(callback: js.Function1[js.Any, Unit]): Unit = js.native
