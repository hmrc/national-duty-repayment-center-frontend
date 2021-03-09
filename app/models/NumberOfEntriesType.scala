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

package models

import pages.HowManyEntriesPage
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.Hint
import uk.gov.hmrc.govukfrontend.views.html.components
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.Input
import uk.gov.hmrc.govukfrontend.views.html.components.{govukErrorMessage, govukFieldset, govukHint, govukInput, govukLabel}

sealed trait NumberOfEntriesType

object NumberOfEntriesType extends Enumerable.Implicits {

  case object Single extends WithName("01") with NumberOfEntriesType
  case object Multiple extends WithName("02") with NumberOfEntriesType

  private val govukErrorMessage: govukErrorMessage = new govukErrorMessage()
  private val govukHint: govukHint = new govukHint()
  private val govukLabel: govukLabel = new govukLabel()

  val values: Seq[NumberOfEntriesType] = Seq(
    Single, Multiple
  )

  def options(form: Form[_], entriesForm: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map {
    val entries = entriesForm.data.contains("value") match {
      case true => entriesForm.data.get("value").head
      case false => ""
    }
    value =>
      RadioItem(
        value = Some(value.toString),
        content = Text(messages(s"numberOfEntriesType.${value.toString}")),
        checked = form("value").value.contains(value.toString),
        conditionalHtml = if(value.toString.equals("02")) Some(new govukInput(govukErrorMessage, govukHint, govukLabel)
        (Input(id="entries", value = Some(entries), name="entries", classes = "govuk-input--width-4",attributes = Map(
          "autocomplete" -> "off",
          "inputmode" -> "numeric",
          "pattern" -> "[0-9]*"
        ), hint=Some(Hint(classes = "govuk-label",content=Text(messages("numberOfEntriesType.02.hint")))))))
        else None
      )
  }


  implicit val enumerable: Enumerable[NumberOfEntriesType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}