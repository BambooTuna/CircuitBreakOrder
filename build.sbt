import Settings._

lazy val root: Project = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "CircuitBreakOrder",
    libraryDependencies ++= Seq(
      Slick.slick,
      Slick.hikaricp,
      MySQL.connector
    ) ++ Kamon.all
  )