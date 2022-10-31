import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"     % "6.4.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"             % "0.68.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.12.0-play-28",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"             % "3.22.0-play-28",
    "com.sun.mail"      % "javax.mail"                      % "1.6.2"
  )

  val test = Seq(
    "org.scalatest"               %% "scalatest"            % "3.0.9",
    "org.scalatestplus.play"      %% "scalatestplus-play"   % "5.1.0",
    "org.pegdown"                 %  "pegdown"              % "1.6.0",
    "org.jsoup"                   %  "jsoup"                % "1.13.1",
    "com.typesafe.play"           %% "play-test"            % PlayVersion.current,
    "org.scalatestplus"           %% "mockito-4-6"          % "3.2.14.0",
    "org.scalatestplus"           %% "scalacheck-1-15"      % "3.2.11.0",
    "com.github.tomakehurst"      %  "wiremock-standalone"  % "2.27.2",
    "com.vladsch.flexmark"        % "flexmark-all"          % "0.35.10"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
