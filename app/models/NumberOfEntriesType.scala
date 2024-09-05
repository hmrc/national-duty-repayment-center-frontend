/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ErrorMessage, Hint, Label}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukErrorMessage, GovukHint, GovukInput, GovukLabel}
import uk.gov.hmrc.govukfrontend.views.html.helpers.{GovukFormGroup, GovukHintAndErrorMessage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.Input
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait NumberOfEntriesType

object NumberOfEntriesType extends Enumerable.Implicits {

  case object Single   extends WithName("01") with NumberOfEntriesType
  case object Multiple extends WithName("02") with NumberOfEntriesType

  private val govukHintAndErrorMessage: GovukHintAndErrorMessage =
    new GovukHintAndErrorMessage(new GovukHint(), new GovukErrorMessage())

  val values: Seq[NumberOfEntriesType] = Seq(Single, Multiple)

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map {
    value =>
      RadioItem(
        value = Some(value.toString),
        content = Text(messages(s"numberOfEntriesType.${value.toString}")),
        checked = form.value.isEmpty match {
          case true  => form("value").value.contains(value.toString)
          case false => form.value.head.asInstanceOf[Entries].numberOfEntriesType == value
        },
        hint =
          if (value.toString.equals("02")) Some(Hint(content = Text(messages("numberOfEntriesType.02.hint"))))
          else None,
        conditionalHtml =
          if (value.toString.equals("02"))
            Some(
              new GovukInput(new GovukLabel(), new GovukFormGroup(), govukHintAndErrorMessage)(
                Input(
                  id = "entries",
                  value = form("entries").value,
                  label = Label(content = Text(messages("numberOfEntriesType.02.label")), isPageHeading = false),
                  errorMessage =
                    if (form("entries").hasErrors)
                      Some(ErrorMessage(content = Text(messages(form("entries").errors.head.message))))
                    else None,
                  name = "entries",
                  classes = "govuk-input--width-4",
                  attributes = Map("autocomplete" -> "off", "inputmode" -> "numeric", "pattern" -> "[0-9]*")
                )
              )
            )
          else None
      )
  }

  implicit val enumerable: Enumerable[NumberOfEntriesType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
