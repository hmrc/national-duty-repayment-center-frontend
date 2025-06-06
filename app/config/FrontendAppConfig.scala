/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import java.time.LocalDate

import com.google.inject.{ImplementedBy, Inject}
import controllers.routes
import javax.inject.Singleton
import play.api.Configuration
import play.api.i18n.{Lang, Langs}
import play.api.mvc.Call

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

object FrontendAppConfig {

  case class FileFormats(
    maxFileSizeMb: Int,
    approvedFileTypes: String,
    approvedFileExtensions: String,
    proofOfAuthorityExtensions: String,
    bulkExtensions: String
  )

  case class EoriIntegration(enabled: Boolean, enrolmentKey: String, enrolmentUrl: Option[String])

  case class Emails(customsAccountingRepayments: String)

  case class AllowCmaThresholds(reclaimTotal: BigDecimal, entryAgeDays: Int) {
    def earliestEntryDate: LocalDate = LocalDate.now().minusDays(entryAgeDays + 1)
  }

}

@ImplementedBy(classOf[FrontendAppConfigImpl])
trait FrontendAppConfig {

  val appName: String
  val contactHost: String
  val contactFormServiceIdentifier: String
  val analyticsToken: String
  val analyticsHost: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val timeout: Int
  val countdown: Int
  val authUrl: String
  val loginUrl: String
  val loginContinueUrl: String
  val languageTranslationEnabled: Boolean
  val signOutUrl: String
  val fileFormats: config.FrontendAppConfig.FileFormats
  val upscanInitiateBaseUrl: String
  val baseExternalCallbackUrl: String
  val baseInternalCallbackUrl: String

  val showResearchBanner: Boolean
  val researchBannerUrl: Option[String]

  val eoriIntegration: FrontendAppConfig.EoriIntegration
  val emails: FrontendAppConfig.Emails

  def languageMap: Map[String, Lang]

  val routeToSwitchLanguage: String => Call
  val locationCanonicalList: String
  val feedbackSurvey: String
  val addressLookupInitUrl: String
  val addressLookupConfirmedUrl: String
  val addressLookupPageHeadingStyle: String
  val showPhaseBanner: Boolean
  val barsBusinessAssessUrl: String
  val accessibilityReportUrl: Option[String]

  def selfUrl(url: String): String

  val fileUploadTimeout: FiniteDuration

  val allowCmaThresholds: FrontendAppConfig.AllowCmaThresholds
}

@Singleton
class FrontendAppConfigImpl @Inject() (configuration: Configuration, langs: Langs) extends FrontendAppConfig {

  override val appName: String              = configuration.get[String]("appName")
  override val contactHost                  = configuration.get[String]("contact-frontend.host")
  override val contactFormServiceIdentifier = configuration.get[String]("contact-frontend.serviceId")
  override val analyticsToken: String       = configuration.get[String](s"google-analytics.token")
  override val analyticsHost: String        = configuration.get[String](s"google-analytics.host")

  override val showPhaseBanner: Boolean = configuration.get[Boolean]("phaseBanner.display")

  override val betaFeedbackUrl                = s"$contactHost/contact/beta-feedback"
  override val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"

  private val addressLookupBaseUrl: String =
    configuration.get[Service]("microservice.services.address-lookup-frontend").baseUrl

  override val addressLookupInitUrl: String =
    s"$addressLookupBaseUrl${configuration.get[String]("microservice.services.address-lookup-frontend.init")}"

  override val addressLookupConfirmedUrl: String =
    s"$addressLookupBaseUrl${configuration.get[String]("microservice.services.address-lookup-frontend.confirmed")}"

  override val addressLookupPageHeadingStyle: String =
    configuration.get[String]("address-lookup-frontend.pageHeadingStyle")

  private val barsBaseUrl: String =
    configuration.get[Service]("microservice.services.bank-account-reputation").baseUrl

  override val barsBusinessAssessUrl: String =
    s"$barsBaseUrl${configuration.get[String]("microservice.services.bank-account-reputation.businessAssess")}"

  override val accessibilityReportUrl: Option[String] =
    for {
      host <- configuration.getOptional[String]("platform.frontend.host").orElse(
        configuration.getOptional[String]("accessibility-statement.host")
      )
      path        <- configuration.getOptional[String]("accessibility-statement.path")
      servicePath <- configuration.getOptional[String]("accessibility-statement.service-path")
    } yield s"$host$path$servicePath"

  private val selfBaseUrl: String = configuration
    .getOptional[String]("platform.frontend.host")
    .getOrElse("http://localhost:8450")

  override def selfUrl(url: String): String = s"$selfBaseUrl$url"

  override val timeout: Int             = configuration.get[Int]("timeout.timeout")
  override val countdown: Int           = configuration.get[Int]("timeout.countdown")
  override val authUrl: String          = configuration.get[Service]("microservice.services.auth").baseUrl
  override val loginUrl: String         = configuration.get[String]("urls.login")
  override val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")

  override val languageTranslationEnabled: Boolean = langs.availables.exists(_.language == "cy")

  override val showResearchBanner: Boolean = configuration.get[Boolean]("researchBanner.display")

  override val researchBannerUrl: Option[String] =
    if (showResearchBanner) configuration.getOptional[String]("urls.researchBanner") else None

  override val signOutUrl: String       = configuration.get[String]("urls.logout")
  private lazy val feedbackHost: String = configuration.get[String]("feedback-frontend.host")
  private lazy val feedbackUrl: String  = configuration.get[String]("feedback-frontend.url")
  lazy val feedbackSurvey: String       = s"$feedbackHost$feedbackUrl"

  override val fileFormats: FrontendAppConfig.FileFormats = FrontendAppConfig.FileFormats(
    maxFileSizeMb = configuration.get[Int]("file-formats.max-file-size-mb"),
    approvedFileExtensions = configuration.get[String]("file-formats.approved-file-extensions"),
    approvedFileTypes = configuration.get[String]("file-formats.approved-file-types"),
    proofOfAuthorityExtensions = configuration.get[String]("file-formats.proof-of-authority-file-extensions"),
    bulkExtensions = configuration.get[String]("file-formats.bulk-file-extensions")
  )

  override val allowCmaThresholds: FrontendAppConfig.AllowCmaThresholds = FrontendAppConfig.AllowCmaThresholds(
    reclaimTotal = configuration.get[Int]("allow-cma-threshold.reclaim-total-amount"),
    entryAgeDays = configuration.get[Int]("allow-cma-threshold.entry-age-days")
  )

  override val eoriIntegration: FrontendAppConfig.EoriIntegration = {
    val enabled = configuration.get[Boolean]("eori-integration.enabled")
    FrontendAppConfig.EoriIntegration(
      enabled = enabled,
      enrolmentKey = configuration.get[String]("eori-integration.enrolment-key"),
      enrolmentUrl = if (enabled) configuration.getOptional[String]("eori-integration.enrolment-url") else None
    )
  }

  override val emails: FrontendAppConfig.Emails =
    FrontendAppConfig.Emails(customsAccountingRepayments =
      configuration.get[String]("emails.customs-accounting-repayment")
    )

  override val baseExternalCallbackUrl: String = configuration.get[String]("urls.callback.external")
  override val baseInternalCallbackUrl: String = configuration.get[String]("urls.callback.internal")

  override val fileUploadTimeout: FiniteDuration =
    FiniteDuration(configuration.getMillis("file-upload.timeout"), MILLISECONDS)

  override val upscanInitiateBaseUrl: String =
    configuration.get[Service]("microservice.services.upscan-initiate").baseUrl

  lazy val locationCanonicalList: String = configuration.getOptional[String]("location.canonical.list").getOrElse(
    throw new Exception(s"Missing configuration key: location.canonical.list")
  )

  override val routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  override def languageMap: Map[String, Lang] = if (languageTranslationEnabled)
    Map("english"    -> Lang("en"), "cymraeg" -> Lang("cy"))
  else Map("english" -> Lang("en"))

}
