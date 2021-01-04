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

package views.components

import play.api.i18n.Messages
import play.api.mvc.Request
import uk.gov.hmrc.govukfrontend.views.viewmodels.footer.FooterItem

object FooterLinks {

  val cookies = "/help/cookies"
  val privacy = "/help/privacy"
  val termsConditions = "/help/terms-and-conditions"
  val govukHelp = "https://www.gov.uk/help"

  def cookieLink(implicit messages: Messages) =
    FooterItem(
      Some(messages("footer.cookies")),
      Some(cookies)
    )

  def privacyLink(implicit messages: Messages) =
    FooterItem(
      Some(messages("footer.privacy")),
      Some(privacy)
    )

  def termsConditionsLink(implicit messages: Messages) =
    FooterItem(
      Some(messages("footer.termsConditions")),
      Some(termsConditions)
    )

  def govukHelpLink(implicit messages: Messages) =
    FooterItem(
      Some(messages("footer.govukHelp")),
      Some(govukHelp)
    )

  def items(implicit messages: Messages, request: Request[_]) =
    Seq(
      cookieLink,
      privacyLink,
      termsConditionsLink,
      govukHelpLink
    )
}
