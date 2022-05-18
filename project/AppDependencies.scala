import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"     % "5.20.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"             % "0.59.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.11.0-play-28",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"             % "3.3.0-play-28",
    "com.sun.mail"      % "javax.mail"                      % "1.6.2"
  )

  val test = Seq(
    "org.scalatest"               %% "scalatest"            % "3.0.9",
    "org.scalatestplus.play"      %% "scalatestplus-play"   % "3.1.3",
    "org.pegdown"                 %  "pegdown"              % "1.6.0",
    "org.jsoup"                   %  "jsoup"                % "1.13.1",
    "com.typesafe.play"           %% "play-test"            % PlayVersion.current,
    "org.mockito"                 %  "mockito-all"          % "1.10.19",
    "org.scalacheck"              %% "scalacheck"           % "1.14.0",
    "com.github.tomakehurst"      %  "wiremock-standalone"  % "2.26.3"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
