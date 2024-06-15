ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "WorkshopAIAssistant"
  )

libraryDependencies += "io.cequence" %% "openai-scala-client" % "1.0.0"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.10"

