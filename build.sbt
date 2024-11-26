val scala3Version = "3.5.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "DistributedCompute",
    version := "0.1.0",

    scalaVersion := scala3Version,
    Compile / run / mainClass := Some("client.ClientApp"),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test 
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  )
