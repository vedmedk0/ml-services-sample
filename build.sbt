ThisBuild / name := "ml-grpc-services"

ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "2.13.6"

ThisBuild / scalacOptions ++= Seq("-Ymacro-annotations")

val Http4sVersion     = "1.0.0-M38"
val CirceVersion      = "0.14.3"
val PureconfigVersion = "0.17.2"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

lazy val root = project.in(file(".")).aggregate(legacyService, typelevelService)

lazy val legacyService = (project in file("legacy-service")).settings(
  name := "legacy-service",
  Compile / PB.targets := Seq(
    scalapb.gen() -> (Compile / sourceManaged).value
  ),
  libraryDependencies ++= Seq(
    "com.github.pureconfig" %% "pureconfig"           % "0.14.0",
    "io.grpc"                % "grpc-netty"           % scalapb.compiler.Version.grpcJavaVersion,
    "io.grpc"                % "grpc-netty-shaded"    % scalapb.compiler.Version.grpcJavaVersion,
    "io.grpc"                % "grpc-services"        % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb"  %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
    "com.thesamet.scalapb"  %% "scalapb-runtime"      % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "org.scalatest"         %% "scalatest"            % "3.2.10"
  )
)

lazy val typelevelService = (project in file("typelevel-service")).settings(
  name := "typelevel-service",
  libraryDependencies ++= Seq(
    "org.http4s"            %% "http4s-ember-server"    % Http4sVersion,
    "org.http4s"            %% "http4s-ember-client"    % Http4sVersion,
    "org.http4s"            %% "http4s-circe"           % Http4sVersion,
    "org.http4s"            %% "http4s-dsl"             % Http4sVersion,
    "com.github.fd4s"       %% "fs2-kafka"              % "3.1.0",
    "io.circe"              %% "circe-core"             % CirceVersion,
    "io.circe"              %% "circe-generic"          % CirceVersion,
    "io.circe"              %% "circe-parser"           % CirceVersion,
    "io.circe"              %% "circe-generic-extras"   % CirceVersion,
    "com.github.pureconfig" %% "pureconfig"             % PureconfigVersion,
    "com.github.pureconfig" %% "pureconfig-enumeratum"  % PureconfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % PureconfigVersion
  )
)
