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

package models.addresslookup

import config.FrontendAppConfig
import models.addresslookup.AddressLookupRequest.Labels.Language.{
  AppLevelLabels,
  ConfirmPageLabels,
  EditPageLabels,
  LookupPageLabels,
  SelectPageLabels
}
import models.addresslookup.AddressLookupRequest.{Labels, Options}
import models.addresslookup.AddressLookupRequest.Options.TimeoutConfig
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import utils.Injector

class AddressLookupRequestSpec extends WordSpec with MustMatchers with MockitoSugar with Injector {

  val realMessagesApi: MessagesApi = instanceOf[MessagesApi]
  val appConfig: FrontendAppConfig = instanceOf[FrontendAppConfig]

  "AddressLookupRequest" should {
    "create properly for Address Lookup initialisation " in {

      val request = AddressLookupRequest(
        continueUrl = "http://continue",
        homeUrl = "http://start",
        signOutUrl = "http://leave",
        keepAliveUrl = "http://keepalive",
        lookupPageHeadingKey = "importerYourAddress.title",
        hintKey = "importerManualAddress.hint"
      )(realMessagesApi, appConfig)

      request mustBe expectedRequest
    }
  }

  val expectedRequest = new AddressLookupRequest(
    2,
    Options(
      continueUrl = "http://continue",
      serviceHref = "http://start",
      signOutHref = "http://leave",
      showPhaseBanner = true,
      phaseFeedbackLink = "http://localhost:9250/contact/beta-feedback",
      deskProServiceName = "NDRC",
      ukMode = false,
      includeHMRCBranding = false,
      timeoutConfig = TimeoutConfig(
        timeoutAmount = Some(appConfig.timeout),
        timeoutUrl = Some("http://leave"),
        timeoutKeepAliveUrl = Some("http://keepalive")
      )
    ),
    Labels(
      en =
        AddressLookupRequest.Labels.Language(
          AppLevelLabels(navTitle = Some("Apply for repayment of import duty and import VAT")),
          SelectPageLabels(title = Some("Select an address"), heading = Some("Select an address")),
          LookupPageLabels(
            title = Some("Enter your address"),
            heading = Some("Enter your address"),
            afterHeadingText = Some("We will use this to send letters about this application.")
          ),
          ConfirmPageLabels(title = Some("Confirm your address"), heading = Some("Confirm your address")),
          EditPageLabels(
            title = Some("Enter your address"),
            heading = Some("Enter your address"),
            postcodeLabel = Some("Postcode")
          )
        ),
      cy =
        AddressLookupRequest.Labels.Language(
          AppLevelLabels(navTitle = Some("TODO Welsh service name")),
          SelectPageLabels(),
          LookupPageLabels(heading = Some("Enter your address")),
          ConfirmPageLabels(),
          EditPageLabels()
        )
    )
  )

}
