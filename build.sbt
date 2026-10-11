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
    )
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := true,
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
