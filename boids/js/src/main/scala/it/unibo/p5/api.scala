package it.unibo.p5

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope
import scala.scalajs.js.|

/** The subset of p5.js global-mode functions and browser globals used by the app (p5 is imported by `main.js`). */
@js.native
@JSGlobalScope
object api extends js.Object:
  val width: Double = js.native
  val height: Double = js.native
  val windowWidth: Double = js.native
  val windowHeight: Double = js.native
  val drawingContext: js.Dynamic = js.native
  val document: js.Dynamic = js.native
  def createCanvas(width: Double, height: Double): Element = js.native
  def createButton(label: String): Element = js.native
  def createDiv(html: String = js.native): Element = js.native
  def createSpan(html: String = js.native): Element = js.native
  def createCheckbox(label: String = js.native, value: Boolean = js.native): Element = js.native
  def createSlider(min: Double, max: Double, value: Double = js.native, step: Double = js.native): Element = js.native
  def noLoop(): Unit = js.native
  def redraw(): Unit = js.native
  def background(gray: Double): Unit = js.native

@js.native
trait Element extends js.Object:
  val elt: js.Dynamic = js.native
  def position(x: Double, y: Double): Unit = js.native
  def style(property: String, value: String): Unit = js.native
  def parent(parent: Element): Element = js.native
  def addClass(name: String): Element = js.native
  def toggleClass(name: String): Element = js.native
  def html(content: String): Element = js.native
  def attribute(name: String, value: String): Element = js.native
  def value(): String | Double = js.native
  def value(value: Double): Element = js.native
  def checked(): Boolean = js.native
  def input(callback: js.Function1[js.Any, Unit]): Element = js.native
  def mousePressed(callback: js.Function1[js.Any, Unit]): Unit = js.native
