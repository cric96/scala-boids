import org.scalajs.linker.interface.ModuleInitializer

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.9.0"
ThisBuild / name := "scala-boids"

lazy val boids = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .withoutSuffixFor(JVMPlatform)
  .settings(
    name := "scala-boids",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % "3.7.1",
      "dev.optics" %%% "monocle-core" % "3.3.0",
      "dev.optics" %%% "monocle-macro" % "3.3.0",
      "com.lihaoyi" %%% "upickle" % "4.4.3",
      "org.typelevel" %%% "munit-cats-effect" % "2.2.1" % Test
    ),
    // parameters.json is embedded at build time: the JVM, the browser and the tests all read the same file
    Compile / sourceGenerators += Def.task {
      val json = (ThisBuild / baseDirectory).value / "boids" / "shared" / "src" / "main" / "resources" / "parameters.json"
      val generated = (Compile / sourceManaged).value / "it" / "unibo" / "boundary" / "ParametersJson.scala"
      val quotes = "\"\"\""
      val source = Seq(
        "package it.unibo.boundary",
        "",
        "private[boundary] object ParametersJson:",
        s"  val text: String = $quotes${IO.read(json)}$quotes",
        ""
      ).mkString("\n")
      if (!generated.exists || IO.read(generated) != source) IO.write(generated, source)
      Seq(generated)
    }.taskValue
  )
  .jsSettings(
    // the page and the simulation worker, sharing their code through an internal module
    Compile / scalaJSModuleInitializers ++= Seq(
      ModuleInitializer.mainMethodWithArgs("it.unibo.render.Main", "main").withModuleID("main"),
      ModuleInitializer.mainMethodWithArgs("it.unibo.worker.SimulationWorker", "main").withModuleID("worker")
    ),
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.ESModule))
  )
  .jvmSettings(
    // in-process, sbt exits as soon as main returns, closing the Swing window with it
    run / fork := true,
    run / baseDirectory := (ThisBuild / baseDirectory).value,
    run / outputStrategy := Some(StdoutOutput),
    libraryDependencies ++= Seq(
      "org.rogach" %% "scallop" % "6.0.1",
      "com.lihaoyi" %% "os-lib" % "0.11.8",
      "com.formdev" % "flatlaf" % "3.7.2"
    )
  )
lazy val root = (project in file("."))
  .aggregate(boids.js, boids.jvm)
  .settings(
    publish := {},
    publishLocal := {}
  )
