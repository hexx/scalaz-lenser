import sbt._,Keys._

object Build extends Build {
  lazy val baseSettings = Seq(
    scalaVersion := "2.10.1",
    organization := "com.github.hexx",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
  )

  lazy val scalazLenser = Project(
    id = "scalaz-lenser",
    base = file(".")
  ).settings(
    baseSettings ++ seq(
      name := "scalaz-lenser",
      version := "0.0.1",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "1.9.1" % "test",
        "org.scalaz" %% "scalaz-core" % "7.0.0-RC1" % "test"
      ),
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _)
    ) : _*
  )
}
