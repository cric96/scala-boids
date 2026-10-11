ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.9.0"
ThisBuild / name := "scala-boids"

lazy val boids = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .withoutSuffixFor(JVMPlatform)
  .settings(
    name := "scala-boids",
    libraryDependencies ++= Seq(
      "com.github.plokhotnyuk.rtree2d" %%% "rtree2d-core" % "0.11.15",
      "org.typelevel" %%% "cats-effect" % "3.7.1",
      "dev.optics" %%% "monocle-core" % "3.3.0",
      "dev.optics" %%% "monocle-macro" % "3.3.0",
      "com.lihaoyi" %%% "upickle" % "4.4.3",
      "org.typelevel" %%% "munit-cats-effect" % "2.2.1" % Test
    )
  )
  .jsConfigure(p => p.enablePlugins(ScalablyTypedConverterGenSourcePlugin))
  .jsSettings(
    Compile / npmDependencies ++=
      Seq(
        "@types/p5" -> "1.4.2",
        "p5" -> "1.4.2"
      ),
    stOutputPackage := "it.unibo.typings",
    stMinimize := Selection.AllExcept("p5"),
    scalaJSUseMainModuleInitializer := true,
    webpackEmitSourceMaps := false
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.rogach" %% "scallop" % "6.0.1",
      "com.lihaoyi" %% "os-lib" % "0.11.8"
    )
  )
lazy val root = (project in file("."))
  .aggregate(boids.js, boids.jvm)
  .settings(
    publish := {},
    publishLocal := {}
  )
