import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.4.0"
  private val playVersion      = "30"
  private val mongoVersion     = "1.7.0"
  private val pekkoVersion = "1.0.2"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% s"bootstrap-frontend-play-$playVersion"              % bootstrapVersion,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc-play-$playVersion"              % "8.5.0",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-play-$playVersion"                      % mongoVersion,
    "uk.gov.hmrc"       %% s"play-conditional-form-mapping-play-$playVersion"   % "2.0.0",
    "com.sun.mail"       % "javax.mail"                                         % "1.6.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-play-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-play-$playVersion" % mongoVersion,
    "org.mockito"            %% "mockito-scala-scalatest"            % "1.17.29",
    "org.scalatestplus"      %% "scalacheck-1-17"                    % "3.2.17.0",
    "io.github.wolfendale"   %% "scalacheck-gen-regexp"              % "1.1.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
