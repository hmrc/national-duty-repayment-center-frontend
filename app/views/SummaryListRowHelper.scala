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

package views

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

trait SummaryListRowHelper {

  def summaryListRow(
    label: String,
    value: String,
    visuallyHiddenText: Option[String],
    action: (Call, String),
    keyClasses: Option[String] = None,
    valueClasses: Option[String] = None,
    url: Option[String] = None
  )(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = Text(messages(label)),
        classes = keyClasses.getOrElse("govuk-!-width-one-third")
      ),
      value = Value(
        content = HtmlContent(
          if (url.nonEmpty)
            s"<a class='govuk-link' href='${url.get}' target='_blank' rel='noopener noreferrer'>$value</a>"
          else value
        ),
        classes = valueClasses.getOrElse("govuk-!-width-two-thirds")
      ),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href = action._1.url,
              content = Text(messages(action._2)),
              visuallyHiddenText = visuallyHiddenText.map(messages.apply(_))
            )
          ),
          classes = "govuk-!-width-one-third"
        )
      )
    )

  def summaryListRowNoActions(
    label: String,
    value: String,
    visuallyHiddenText: Option[String],
    keyClasses: Option[String] = None,
    valueClasses: Option[String] = None
  )(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = Text(messages(label)),
        classes = keyClasses.getOrElse("govuk-!-width-one-third")
      ),
      value = Value(
        content = HtmlContent(value),
        classes = valueClasses.getOrElse("govuk-!-width-two-thirds")
      ),
      actions = None
    )
}
