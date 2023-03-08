import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "national-duty-repayment-center-frontend"

PlayKeys.devSettings := Seq("play.server.http.port" -> "8450")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(DefaultBuildSettings.scalaSettings: _*)
  .settings(DefaultBuildSettings.defaultSettings(): _*)
  .settings(SbtDistributablesPlugin.publishingSettings: _*)
  .settings(inConfig(Test)(testSettings): _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(majorVersion := 0)
  .settings(
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)
  )
  .settings(
    name := appName,
    scalaVersion := "2.13.10",
    RoutesKeys.routesImport += "models._",
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "views.ViewUtils._",
      "controllers.routes._"
    ),
    PlayKeys.playDefaultPort := 9000,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*components.*;" +
      ".*BuildInfo.*;.*javascript.*;.*Routes.*;.*GuiceInjector;" +
      ".*ControllerConfiguration;.*LanguageSwitchController",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq("-feature"),
    libraryDependencies ++= AppDependencies(),
    libraryDependencies += "org.webjars.bower" % "bootstrap-sass" % "3.3.7",
    libraryDependencies += "org.webjars.bower" % "compass-mixins" % "1.0.2",
    retrieveManaged := true,
    update / evictionWarningOptions :=
      EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat,uglify),
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
  ).settings(scalafmtOnCompile := true)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork        := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf"
  )
)


lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it",
    baseDirectory.value / "test" / "generators"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  parallelExecution := false,
  fork := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=it.application.conf"
  )
)

