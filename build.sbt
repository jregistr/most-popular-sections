name := """Most Popular Sections"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DebianPlugin)

resolvers += Resolver.sonatypeRepo("snapshots")
maintainer in Linux := "Jeff Registre <jeffreyregistre@gmail.com>"

packageSummary in Linux := "Most Popular sections api server."

packageDescription := "Api server exposing endpoint(s) for querying the most popular sections from NY times API."

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += ws

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.8.47" % Test
libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.6.0" % Test
libraryDependencies += "com.github.javafaker" % "javafaker" % "0.13" % Test

