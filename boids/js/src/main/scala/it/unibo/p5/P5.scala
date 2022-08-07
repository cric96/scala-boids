package it.unibo.p5

import it.unibo.p5.P5Logic

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js
object P5:
  def apply(logic: P5Logic): Unit =
    js.Dynamic.global.window.draw = () => logic.draw()
    js.Dynamic.global.window.setup = () => logic.setup()
