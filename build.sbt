

name := "ml-grpc-services"

version := "0.1"

scalaVersion := "2.13.6"


inThisBuild(

  Seq(
    libraryDependencies := Seq(
      "com.github.pureconfig" %% "pureconfig" % "0.14.0",
      "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,
      "io.grpc" % "grpc-services" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "org.scalatest" %% "scalatest" % "3.2.10",

    )
  )
)

lazy val root = Project(id = "risk-ident-test-task", base = file(".")).settings(
  Compile / PB.targets := Seq(
    scalapb.gen() -> (Compile / sourceManaged).value
  ),
  scalaVersion := "2.13.4",
  exportJars in Compile := true
)
