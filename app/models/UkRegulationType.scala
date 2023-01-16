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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait UkRegulationType

object UkRegulationType extends Enumerable.Implicits {

  case object ErrorByCustoms                 extends WithName("048") with UkRegulationType
  case object LowerRateWasApplicable         extends WithName("049") with UkRegulationType
  case object OverPaymentOfDutyOrVAT         extends WithName("050") with UkRegulationType
  case object Rejected                       extends WithName("051") with UkRegulationType
  case object SpecialCircumstances           extends WithName("052") with UkRegulationType
  case object WithdrawalOfCustomsDeclaration extends WithName("053") with UkRegulationType

  val values: Seq[UkRegulationType] = Seq(
    ErrorByCustoms,
    LowerRateWasApplicable,
    OverPaymentOfDutyOrVAT,
    Rejected,
    SpecialCircumstances,
    WithdrawalOfCustomsDeclaration
  )

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map {
    value =>
      RadioItem(
        value = Some(value.toString),
        content = Text(messages(s"ukRegulationType.${value.toString}")),
        checked = form("value").value.contains(value.toString)
      )
  }

  implicit val enumerable: Enumerable[UkRegulationType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
