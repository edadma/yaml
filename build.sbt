name := "yaml"

version := "0.1.0"

scalaVersion := "2.13.6"

scalacOptions ++= Seq( "-deprecation", "-feature", "-unchecked", "-language:postfixOps", "-language:implicitConversions", "-language:existentials", "-language:dynamics" )

organization := "io.github.edadma"

githubOwner := "edadma"

githubRepository := name.value

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"

libraryDependencies ++= Seq(
  "io.github.edadma" %% "pattern-matcher" % "0.1.0"
)

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "pprint" % "0.6.6"
)

mainClass := Some(s"${organization.value}.${name.value}.Main")

publishMavenStyle := true

Test / publishArtifact := false

pomIncludeRepository := { _ => false }

licenses := Seq("ISC" -> url("https://opensource.org/licenses/ISC"))

homepage := Some(url("https://github.com/edadma/" + name.value))

pomExtra :=
  <scm>
    <url>git@github.com:edadma/{name.value}.git</url>
    <connection>scm:git:git@github.com:edadma/{name.value}.git</connection>
  </scm>
  <developers>
    <developer>
      <id>edadma</id>
      <name>Edward A. Maxedon, Sr.</name>
      <url>https://github.com/edadma</url>
    </developer>
  </developers>
