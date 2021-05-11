import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "org.reactivemongo" %% "play2-reactivemongo"            % "0.20.3-play27",
    "uk.gov.hmrc"       %% "play-ui"                        % "9.0.0-play-27",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.6.0-play-27",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-27"     % "4.1.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"             % "0.59.0-play-27",
    "com.typesafe.akka" %% "akka-actor"                     % "12.6.12"
  )

  val test = Seq(
    "org.scalatest"               %% "scalatest"            % "3.0.9",
    "org.scalatestplus.play"      %% "scalatestplus-play"   % "4.0.3",
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
