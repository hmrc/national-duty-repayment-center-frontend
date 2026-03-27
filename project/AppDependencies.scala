import sbt._

object AppDependencies {

  private val bootstrapVersion = "10.7.0"
  private val playVersion      = "play-30"
  private val mongoVersion     = "2.12.0"

  val mailDependencies: Seq[ModuleID] = Seq(
    "jakarta.mail" % "jakarta.mail-api" % "2.1.5",
    "org.eclipse.angus" % "angus-mail" % "2.0.5"
  )

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% s"bootstrap-frontend-$playVersion"            % bootstrapVersion,
    "uk.gov.hmrc"       %% s"play-frontend-hmrc-$playVersion"            % "13.2.0",
    "uk.gov.hmrc"       %% s"play-conditional-form-mapping-$playVersion" % "3.5.0",
    "io.github.openhtmltopdf" % "openhtmltopdf-pdfbox"       % "2.0.24"
  ) ++ mailDependencies

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"    %% s"hmrc-mongo-test-$playVersion" % mongoVersion,
    "org.scalatestplus"    %% "scalacheck-1-17"               % "3.2.18.0",
    "io.github.wolfendale" %% "scalacheck-gen-regexp"         % "1.1.0"
  ) ++ mailDependencies.map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
