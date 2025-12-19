import sbt.Keys.test

// Supported versions
val scala212 = "2.12.18"
val scala213 = "2.13.18"
val scala3 = "3.2.2"

ThisBuild / organization := "io.cequence"
ThisBuild / scalaVersion := scala213
ThisBuild / version := "1.3.0.RC.1"
ThisBuild / isSnapshot := false

lazy val commonSettings = Seq(
  libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.16",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % Test,
  libraryDependencies += "org.scalatestplus" %% "mockito-4-11" % "3.2.16.0" % Test,
  libraryDependencies ++= extraTestDependencies(scalaVersion.value),
  crossScalaVersions := List(scala212, scala213, scala3)
)

def extraTestDependencies(scalaVersion: String) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 12)) =>
      Seq(
        "org.apache.pekko" %% "pekko-actor-testkit-typed" % "1.4.0" % Test
      )

    case Some((2, 13)) =>
      Seq(
        "org.apache.pekko" %% "pekko-actor-testkit-typed" % "1.4.0" % Test
      )

    case Some((3, _)) =>
      Seq(
        // because of conflicting cross-version suffixes 2.13 vs 3 - scala-java8-compat, etc
        "org.apache.pekko" % "pekko-actor-testkit-typed_3" % "1.4.0" % Test
      )

    case _ =>
      Nil
  }

// lazy val ws_client_git_deps = RootProject(
//   uri("https://github.com/Driox/open-ai-ws-client.git#0.7.3")
// )

lazy val ws_client_git_deps = uri("https://github.com/Driox/open-ai-ws-client.git#0.7.3")

lazy val jsonRepair = ProjectRef(ws_client_git_deps, "json-repair")
lazy val wsClientCore = ProjectRef(ws_client_git_deps, "ws-client-core")
lazy val wsClientPlay = ProjectRef(ws_client_git_deps, "ws-client-play")
lazy val wsClientPlayStreaming = ProjectRef(ws_client_git_deps, "ws-client-play-streaming")

lazy val core =
  (project in file("openai-core"))
    .settings(commonSettings *)
    .dependsOn(jsonRepair, wsClientCore)

lazy val client =
  (project in file("openai-client"))
    .settings(commonSettings *)
    .dependsOn(core, wsClientCore)
    .aggregate(core)

lazy val client_stream = (project in file("openai-client-stream"))
  .settings(commonSettings *)
  .dependsOn(client, wsClientCore)
  .aggregate(client)

// note that for anthropic_client we provide a streaming extension within the module as well
lazy val anthropic_client = (project in file("anthropic-client"))
  .settings(commonSettings *)
  .dependsOn(core, wsClientCore)
  .aggregate(core, client, client_stream)

lazy val google_vertexai_client = (project in file("google-vertexai-client"))
  .settings(commonSettings *)
  .dependsOn(core)
  .aggregate(core, client, client_stream)

lazy val google_gemini_client = (project in file("google-gemini-client"))
  .settings(commonSettings *)
  .dependsOn(core, wsClientCore)
  .aggregate(core, client, client_stream)

// note that for perplexity_client we provide a streaming extension within the module as well
lazy val perplexity_sonar_client = (project in file("perplexity-sonar-client"))
  .settings(commonSettings *)
  .dependsOn(core, wsClientCore)
  .aggregate(core, client, client_stream)

lazy val count_tokens = (project in file("openai-count-tokens"))
  .settings(
    (commonSettings ++ Seq(definedTestNames in Test := Nil)) *
  )
  .dependsOn(client)
  .aggregate(
    anthropic_client,
    google_vertexai_client,
    perplexity_sonar_client,
    google_gemini_client
  )

lazy val guice = (project in file("openai-guice"))
  .settings(commonSettings *)
  .dependsOn(client)
  .aggregate(count_tokens)

lazy val examples = (project in file("openai-examples"))
  .settings(commonSettings *)
  .dependsOn(
    client_stream,
    anthropic_client,
    google_vertexai_client,
    perplexity_sonar_client,
    google_gemini_client
  )
  .aggregate(
    client_stream,
    anthropic_client,
    google_vertexai_client,
    perplexity_sonar_client,
    google_gemini_client
  )

// POM settings for Sonatype
ThisBuild / homepage := Some(
  url("https://github.com/cequence-io/openai-scala-client")
)

ThisBuild / sonatypeProfileName := "io.cequence"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/cequence-io/openai-scala-client"),
    "scm:git@github.com:cequence-io/openai-scala-client.git"
  )
)

ThisBuild / developers := List(
  Developer(
    "bnd",
    "Peter Banda",
    "peter.banda@protonmail.com",
    url("https://peterbanda.net")
  )
)

ThisBuild / licenses += "MIT" -> url("https://opensource.org/licenses/MIT")

ThisBuild / publishMavenStyle := true

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

ThisBuild / publishTo := sonatypePublishToBundle.value

addCommandAlias(
  "validateCode",
  List(
    "scalafix",
    "scalafmtSbtCheck",
    "scalafmtCheckAll",
    "test:scalafix",
    "test:scalafmtCheckAll"
  ).mkString(";")
)

addCommandAlias(
  "formatCode",
  List(
    "scalafmt",
    "scalafmtSbt",
    "Test/scalafmt"
  ).mkString(";")
)

addCommandAlias(
  "testWithCoverage",
  List(
    "coverage",
    "test",
    "coverageReport"
  ).mkString(";")
)

inThisBuild(
  List(
    scalacOptions += "-Ywarn-unused",
//    scalaVersion := scala3,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)
