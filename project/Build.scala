import sbt._
import Keys._

object Build extends sbt.Build {

  def standardSettings = Seq(
    exportJars    := true
  ) ++ Defaults.defaultSettings

  lazy val myProject = Project("queries", file("."),
    settings = Defaults.defaultSettings ++ Seq(
      organization      := "com.bullhorn",
      version           := "1.1",
      scalaVersion      := "2.9.2",
      scalacOptions     := Seq("-deprecation", "-encoding", "utf8"),
      javaOptions       := Seq("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"),
      resolvers         ++= Seq(Repositories.bhThirdParty, Repositories.bhBullhorn),
      publishMavenStyle := false,
      publishTo         := Some(Repositories.bhBullhorn),
      libraryDependencies ++= Seq(
        "org.slf4j" % "slf4j-log4j12" % "1.5.6"
      )))

  object Repositories {
      val bhThirdParty = Resolver.file("thirdparty",
        file("/home/ccollier/devel/bullhorn/gitsrc/core-services/lib")) (Patterns(
        Seq("ivy/ivy-[module]-[revision].[ext]"),
        Seq("[artifact]-[revision].[ext]"), isMavenCompatible = false))

      val bhBullhorn = Resolver.file("bullhorn",
        file("/home/ccollier/devel/bullhorn/bh-libs"))
          (Patterns(
              Seq("[organization]/[module]/[revision]/ivy-[revision].[ext]"),
              Seq("[organization]/[module]/[revision]/[artifact]-[revision].[ext]"), isMavenCompatible = false))
  }
}
