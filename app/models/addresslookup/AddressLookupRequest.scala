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

package models.addresslookup

import config.FrontendAppConfig
import models.addresslookup.AddressLookupRequest.Labels.Language.{
  AppLevelLabels,
  ConfirmPageLabels,
  EditPageLabels,
  LookupPageLabels,
  SelectPageLabels
}
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.{Json, OFormat}

case class AddressLookupRequest(
  version: Int = 2,
  options: AddressLookupRequest.Options,
  labels: AddressLookupRequest.Labels
)

object AddressLookupRequest {
  implicit val format: OFormat[AddressLookupRequest] = Json.format[AddressLookupRequest]

  def apply(
    continueUrl: String,
    homeUrl: String,
    signOutUrl: String,
    accessibilityFooterUrl: Option[String],
    keepAliveUrl: String,
    lookupPageHeadingKey: String,
    hintKey: String,
    editPageHeadingKey: String,
    confirmationHeadingKey: String
  )(implicit messagesApi: MessagesApi, config: FrontendAppConfig): AddressLookupRequest = {
    val englishMessages: Messages = MessagesImpl(Lang("en"), messagesApi)
    val welshMessages: Messages   = MessagesImpl(Lang("cy"), messagesApi)

    def labels(messages: Messages) = AddressLookupRequest.Labels.Language(
      AppLevelLabels(navTitle = Some(messages("site.service_name"))),
      SelectPageLabels(
        title = Some(messages("address.label.select.title")),
        heading = Some(messages("address.label.select.title"))
      ),
      LookupPageLabels(
        title = Some(messages(lookupPageHeadingKey)),
        heading = Some(messages(lookupPageHeadingKey)),
        afterHeadingText = Some(messages(hintKey))
      ),
      ConfirmPageLabels(
        title = Some(messages(confirmationHeadingKey)),
        heading = Some(messages(confirmationHeadingKey))
      ),
      EditPageLabels(
        postcodeLabel = Some(messages("address.label.edit.postcode")),
        title = Some(messages(editPageHeadingKey)),
        heading = Some(messages(editPageHeadingKey))
      )
    )

    new AddressLookupRequest(
      2,
      Options(
        continueUrl = continueUrl,
        serviceHref = homeUrl,
        signOutHref = signOutUrl,
        accessibilityFooterUrl = accessibilityFooterUrl,
        showPhaseBanner = config.showPhaseBanner,
        phaseFeedbackLink = config.betaFeedbackUrl,
        deskProServiceName = config.contactFormServiceIdentifier,
        ukMode = false,
        includeHMRCBranding = false,
        AddressLookupRequest.Options.TimeoutConfig(
          timeoutAmount = Some(config.timeout),
          timeoutUrl = Some(signOutUrl),
          timeoutKeepAliveUrl = Some(keepAliveUrl)
        ),
        pageHeadingStyle = config.addressLookupPageHeadingStyle
      ),
      Labels(
        en = labels(englishMessages),
        cy = labels(if (config.languageTranslationEnabled) welshMessages else englishMessages)
      )
    )
  }

  case class Options(
    continueUrl: String,
    serviceHref: String,
    signOutHref: String,
    accessibilityFooterUrl: Option[String],
    showPhaseBanner: Boolean,
    phaseFeedbackLink: String,
    deskProServiceName: String,
    ukMode: Boolean,
    includeHMRCBranding: Boolean,
    timeoutConfig: AddressLookupRequest.Options.TimeoutConfig,
    pageHeadingStyle: String
  )

  object Options {
    implicit val format: OFormat[Options] = Json.format[Options]

    case class TimeoutConfig(
      timeoutAmount: Option[Int] = None,
      timeoutUrl: Option[String] = None,
      timeoutKeepAliveUrl: Option[String] = None
    )

    object TimeoutConfig {
      implicit val format: OFormat[TimeoutConfig] = Json.format[TimeoutConfig]
    }

  }

  case class Labels(en: AddressLookupRequest.Labels.Language, cy: AddressLookupRequest.Labels.Language)

  object Labels {
    implicit val format: OFormat[Labels] = Json.format[Labels]

    case class Language(
      appLevelLabels: AddressLookupRequest.Labels.Language.AppLevelLabels,
      selectPageLabels: AddressLookupRequest.Labels.Language.SelectPageLabels,
      lookupPageLabels: AddressLookupRequest.Labels.Language.LookupPageLabels,
      confirmPageLabels: AddressLookupRequest.Labels.Language.ConfirmPageLabels,
      editPageLabels: AddressLookupRequest.Labels.Language.EditPageLabels
    )

    object Language {
      implicit val format: OFormat[Language] = Json.format[Language]

      case class AppLevelLabels(navTitle: Option[String] = None, phaseBannerHtml: Option[String] = None)

      object AppLevelLabels {
        implicit val format: OFormat[AppLevelLabels] = Json.format[AppLevelLabels]
      }

      case class SelectPageLabels(
        title: Option[String] = None,
        heading: Option[String] = None,
        submitLabel: Option[String] = None,
        editAddressLinkText: Option[String] = None
      )

      object SelectPageLabels {
        implicit val format: OFormat[SelectPageLabels] = Json.format[SelectPageLabels]
      }

      case class LookupPageLabels(
        title: Option[String] = None,
        heading: Option[String] = None,
        afterHeadingText: Option[String] = None,
        filterLabel: Option[String] = None,
        postcodeLabel: Option[String] = None,
        submitLabel: Option[String] = None
      )

      object LookupPageLabels {
        implicit val format: OFormat[LookupPageLabels] = Json.format[LookupPageLabels]
      }

      case class ConfirmPageLabels(
        title: Option[String] = None,
        heading: Option[String] = None,
        showConfirmChangeText: Option[Boolean] = None
      )

      object ConfirmPageLabels {
        implicit val format: OFormat[ConfirmPageLabels] = Json.format[ConfirmPageLabels]
      }

      case class EditPageLabels(
        title: Option[String] = None,
        heading: Option[String] = None,
        submitLabel: Option[String] = None,
        postcodeLabel: Option[String] = None
      )

      object EditPageLabels {
        implicit val format: OFormat[EditPageLabels] = Json.format[EditPageLabels]
      }

    }

  }

}
