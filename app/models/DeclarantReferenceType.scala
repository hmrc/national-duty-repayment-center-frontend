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

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ErrorMessage, Hint, Label}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.Input
import uk.gov.hmrc.govukfrontend.views.html.components.{govukErrorMessage, govukHint, govukInput, govukLabel}

sealed trait DeclarantReferenceType

object DeclarantReferenceType extends Enumerable.Implicits {

  case object Yes extends WithName("01") with DeclarantReferenceType
  case object No extends WithName("02") with DeclarantReferenceType

  private val govukErrorMessage: govukErrorMessage = new govukErrorMessage()
  private val govukHint: govukHint = new govukHint()
  private val govukLabel: govukLabel = new govukLabel()

  val values: Seq[DeclarantReferenceType] = Seq(
    Yes, No
  )

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map {
    value =>
      RadioItem(
        value = Some(value.toString),
        content = Text(messages(s"declarantReferenceNumber.${value.toString}")),
        checked = form.value.isEmpty match {
          case true => form("value").value.contains(value.toString)
          case false => form.value.head.asInstanceOf[DeclarantReferenceNumber].declarantReferenceType == value
        },
        conditionalHtml = if(value.toString.equals("01")) Some(new govukInput(govukErrorMessage, govukHint, govukLabel)
        (Input(id="declarantReferenceNumber", value = form("declarantReferenceNumber").value,label= Label(
          content=Text(messages("declarantReferenceNumber.declarantReferenceNumber")),
          isPageHeading = false
        ),
          errorMessage = if(form("declarantReferenceNumber").hasErrors){
            Some(ErrorMessage(
              content = Text(messages(form("declarantReferenceNumber").errors.head.message))
            ))
          } else { None },
          name="declarantReferenceNumber", classes = "",attributes = Map(
            "autocomplete" -> "off",
            "inputmode" -> "numeric",
            "pattern" -> "[0-9]*"
          ))))
        else None
      )
  }


  implicit val enumerable: Enumerable[DeclarantReferenceType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}