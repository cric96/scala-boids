package it.unibo.boundary

class SettingsSuite extends munit.CatsEffectSuite:
  private val json = ujson.read(ParametersJson.text)

  private def without(parameter: Parameter): String =
    val parameters = ujson.Obj.from(json("parameters").obj.filterNot(_._1 == parameter.toString))
    ujson.write(ujson.Obj.from(json.obj.map((key, value) => key -> (if (key == "parameters") parameters else value))))

  test("settings missing a parameter are rejected"):
    Parameter.values.foreach(parameter => intercept[IllegalArgumentException](Settings.parse(without(parameter))))
