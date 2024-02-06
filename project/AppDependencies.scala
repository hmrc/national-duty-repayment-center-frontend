import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val bootstrapVersion = "8.4.0"
  val mongoVersion     = "1.7.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"            % mongoVersion,
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"            % "7.0.0-play-28",
    "com.sun.mail"       % "javax.mail"                    % "1.6.2"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapVersion,
    "org.scalatest"          %% "scalatest"               % "3.2.14",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "org.jsoup"               % "jsoup"                   % "1.15.3",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % mongoVersion,
    "com.typesafe.play"      %% "play-test"               % PlayVersion.current,
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.12",
    "org.scalatestplus"      %% "scalacheck-1-15"         % "3.2.11.0",
    "com.github.tomakehurst"  % "wiremock-standalone"     % "2.27.2",
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.62.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test

}
