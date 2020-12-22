import Dependencies._
import sbt._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "zio-sample",
    libraryDependencies ++= dependencies ++ plugins,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val dependencies =
  Cats.all ++
  Http4s.all ++
  Config.all ++
  Streaming.all ++
  ZIO.all ++
  Tapir.all ++
  Doobie.all ++
  Enum.all ++
  STTP.all ++
  Circe.all ++
  Logging.all ++
  Flyway.all ++
  Testing.all

lazy val plugins = Seq(
  compilerPlugin("io.tryp"    % "splain"              % "0.5.7" cross CrossVersion.patch), // https://github.com/tek/splain
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")                           // これがないとcompileできない(implicit0のところ)
)
