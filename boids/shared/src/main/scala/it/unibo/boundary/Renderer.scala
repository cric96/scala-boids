package it.unibo.boundary

import it.unibo.core.Environment

trait Renderer:
  def render(environment: Environment): Unit
