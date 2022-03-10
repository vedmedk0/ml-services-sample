import sbt.addCompilerPlugin

name := "single-app-template"

version := "0.1"

scalaVersion := "2.13.6"


inThisBuild(

  Seq(
    libraryDependencies := Seq(
      "com.github.pureconfig" %% "pureconfig" % "0.14.0",
      "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion
    )
  )
)

lazy val root = Project(id = "single-app-template", base = file(".")).settings(
  Compile / PB.targets := Seq(
    scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
  ),
  scalaVersion := "2.13.4",
  exportJars in Compile := true
)
