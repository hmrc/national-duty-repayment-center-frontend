import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.6.0"
  private val playVersion      = "play-30"
  private val mongoVersion     = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% s"bootstrap-frontend-$playVersion"            % bootstrapVersion,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc-$playVersion"            % "12.2.0",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"                    % mongoVersion,
    "uk.gov.hmrc"       %% s"play-conditional-form-mapping-$playVersion" % "2.0.0",
    "com.sun.mail"       % "javax.mail"                                  % "1.6.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"    %% s"hmrc-mongo-test-$playVersion" % mongoVersion,
    "org.mockito"          %% "mockito-scala-scalatest"       % "1.17.37",
    "org.scalatestplus"    %% "scalacheck-1-17"               % "3.2.18.0",
    "io.github.wolfendale" %% "scalacheck-gen-regexp"         % "1.1.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
