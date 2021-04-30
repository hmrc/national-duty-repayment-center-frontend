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

package config
import com.google.inject.{ImplementedBy, Inject}
import controllers.routes
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call

import javax.inject.Singleton

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

  case class FileFormats(maxFileSizeMb: Int, approvedFileTypes: String, approvedFileExtensions: String)
}

@ImplementedBy(classOf[FrontendAppConfigImpl])
trait FrontendAppConfig {

  val appName: String
  val contactHost: String
  val contactFormServiceIdentifier: String
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val addressLookupServiceUrl: String
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
  def languageMap: Map[String, Lang]
  val routeToSwitchLanguage: String => Call
  val locationCanonicalList: String
  val feedbackSurvey : String
}
@Singleton
class FrontendAppConfigImpl @Inject()(configuration: Configuration) extends FrontendAppConfig {

  override val appName: String = configuration.get[String]("appName")
  override val contactHost = configuration.get[String]("contact-frontend.host")
  override val contactFormServiceIdentifier = "play26frontend"
  override val analyticsToken: String = configuration.get[String](s"google-analytics.token")
  override val analyticsHost: String = configuration.get[String](s"google-analytics.host")
  override val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  override val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"
  override val addressLookupServiceUrl: String = configuration.get[Service]("microservice.services.address-lookup").baseUrl
  override val timeout: Int = configuration.get[Int]("timeout.timeout")
  override val countdown: Int = configuration.get[Int]("timeout.countdown")
  override val authUrl: String = configuration.get[Service]("microservice.services.auth").baseUrl
  override val loginUrl: String = configuration.get[String]("urls.login")
  override val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  override val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")
  override val signOutUrl: String = configuration.get[String]("urls.logout")
  override val feedbackSurvey : String = configuration.get[Service]("feedback-frontend").baseUrl+"/feedback/NATIONAL_DUTY_REPAYMENT_CENTER"
  override val fileFormats: FrontendAppConfig.FileFormats = FrontendAppConfig.FileFormats(
    maxFileSizeMb = configuration.get[Int]("file-formats.max-file-size-mb"),
    approvedFileExtensions = configuration.get[String]("file-formats.approved-file-extensions"),
    approvedFileTypes = configuration.get[String]("file-formats.approved-file-types")
  )
  override val baseExternalCallbackUrl: String = configuration.get[String]("urls.callback.external")
  override val baseInternalCallbackUrl: String = configuration.get[String]("urls.callback.internal")
  override val upscanInitiateBaseUrl: String = configuration.get[Service]("microservice.services.upscan-initiate").baseUrl
  lazy val locationCanonicalList: String = loadConfig("location.canonical.list")

  override def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  override val routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  private def loadConfig(key: String): String = configuration.getOptional[String](key).getOrElse(throw new Exception
  (s"Missing configuration key: $key"))
}