name := "id-experiments"

version := "1.0"

scalaOrganization := "org.typelevel"
scalaVersion := "2.12.4-bin-typelevel-4"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-parser"
).map(_ % "0.9.3")

libraryDependencies += "com.beachape" %% "enumeratum" % "1.5.13"
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
