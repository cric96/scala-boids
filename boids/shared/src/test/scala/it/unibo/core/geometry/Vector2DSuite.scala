package it.unibo.core.geometry

import it.unibo.core.geometry.Vector2D.*

class Vector2DSuite extends munit.CatsEffectSuite:
  test("limit caps the norm of longer vectors"):
    assertEqualsDouble(Vector2D(3, 4).limit(1).norm, 1, 1e-9)

  test("limit leaves shorter vectors untouched"):
    assertEquals(Vector2D(0.3, 0.4).limit(1), Vector2D(0.3, 0.4))

  test("normalizing the zero vector gives the zero vector"):
    assertEquals(zero.normalize, zero)
