name := """Most Popular Sections"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DebianPlugin)

resolvers += Resolver.sonatypeRepo("snapshots")

maintainer in Linux := "Jeff Registre <jeffreyregistre@gmail.com>"

packageSummary in Linux := "Most Popular sections api server."

packageDescription := "Api server exposing endpoint(s) for quering the most popular sections from NY times API."

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.194"


