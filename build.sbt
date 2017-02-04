name := "OpenSky"
version := "1.0"
scalaVersion := "2.11.8"
scalacOptions := Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-Xlint:_",
  "-Xfatal-warnings",
  "-encoding",
  "utf8",
  "-target:jvm-1.8"
)

scalafmtConfig := Some(file(".scalafmt.conf"))
reformatOnCompileSettings

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-stream-kafka" % "0.13",
                            "com.typesafe.akka" %% "akka-slf4j"        % "2.4.16",
                            "com.typesafe.akka" %% "akka-http"         % "10.0.3",
                            "ch.qos.logback"    % "logback-classic"    % "1.1.8",
                            "com.typesafe.play" %% "play-json"         % "2.5.10",
                            "com.outworkers"    %% "phantom-dsl"       % "2.1.2",
                            "com.outworkers"    %% "phantom-streams"   % "2.1.2")

enablePlugins(JavaAppPackaging, UniversalPlugin)
JavaAppSettings()
