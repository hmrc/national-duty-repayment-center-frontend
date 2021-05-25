import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "org.reactivemongo" %% "play2-reactivemongo"            % "0.18.6-play26",
    "uk.gov.hmrc"       %% "govuk-template"                 % "5.66.0-play-26",
    "uk.gov.hmrc"       %% "play-ui"                        % "9.4.0-play-26",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.9.0-play-26",
    "uk.gov.hmrc"       %% "bootstrap-play-26"              % "4.0.0",
    "uk.gov.hmrc"       %% "play-whitelist-filter"          % "3.4.0-play-26",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"             % "0.58.0-play-26",
    "com.sun.mail"      % "javax.mail"                      % "1.6.2",
    "com.typesafe.akka" %% "akka-actor"                     % "12.6.12"
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

  val akkaVersion = "2.5.23"
  val akkaHttpVersion = "10.0.15"

  val overrides = Seq(
    "com.typesafe.akka" %% "akka-stream"    % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf"  % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,
    "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
  )
}
