
name := "single-app-template"

version := "0.1"

scalaVersion := "2.13.6"


inThisBuild(
  Seq(
    libraryDependencies := Seq(
      "com.github.pureconfig" %% "pureconfig" % "0.14.0"
    )
  )
)

lazy val root = Project(id = "single-app-template", base = file(".")).settings(
  scalaVersion := "2.13.4",
  exportJars in Compile := true
)
