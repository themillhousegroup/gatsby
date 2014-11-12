import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

name := "gatsby"

version := s"${sys.props.getOrElse("build.majorMinor", "0.1")}.${sys.props.getOrElse("build.version", "SNAPSHOT")}"

scalaVersion := "2.10.4"

organization := "com.themillhousegroup"

libraryDependencies ++= Seq(
    "io.gatling.highcharts" %   "gatling-charts-highcharts"   % "2.0.0",
    "com.dividezero"        %%  "stubby-standalone"           % "1.2",
    "ch.qos.logback"        %   "logback-classic"             % "1.1.2",
    "joda-time"             %   "joda-time"                   % "2.5",
    "org.mockito"           %   "mockito-all"                 % "1.9.0"       % "test",
    "org.specs2"            %%  "specs2"                      % "2.3.12"      % "test"
)

resolvers ++= Seq(  "oss-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
                    "oss-releases"  at "https://oss.sonatype.org/content/repositories/releases",
                    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")

jacoco.settings

scalariformSettings

net.virtualvoid.sbt.graph.Plugin.graphSettings

packageArchetype.java_application
