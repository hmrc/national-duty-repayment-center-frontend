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
import uk.gov.hmrc.govukfrontend.views.Aliases.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait RepaymentType

object RepaymentType extends Enumerable.Implicits {

  case object BACS extends WithName("02") with RepaymentType
  case object CMA  extends WithName("01") with RepaymentType

  val values: Seq[RepaymentType] = Seq(BACS, CMA)

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map {
    value =>
      RadioItem(
        value = Some(value.toString),
        content = Text(messages(s"repaymentType.${value.toString}")),
        hint = value match {
          case RepaymentType.CMA => Option(Hint(content = Text(messages(s"repaymentType.hint.${value.toString}"))))
          case _                 => None
        },
        checked = form("value").value.contains(value.toString)
      )
  }

  implicit val enumerable: Enumerable[RepaymentType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
