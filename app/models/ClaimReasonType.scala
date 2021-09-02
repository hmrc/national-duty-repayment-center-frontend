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
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.RadioOptionHelper

sealed trait ClaimReasonType

object ClaimReasonType extends Enumerable.Implicits {

  case object Retroactivequota         extends WithName("01") with ClaimReasonType
  case object Cpuchange                extends WithName("02") with ClaimReasonType
  case object CurrencyChanges          extends WithName("03") with ClaimReasonType
  case object CommodityCodeChange      extends WithName("04") with ClaimReasonType
  case object CustomsSpecialProcedures extends WithName("05") with ClaimReasonType
  case object Preference               extends WithName("06") with ClaimReasonType
  case object ReturnedGoodsRelief      extends WithName("07") with ClaimReasonType
  case object ReturnOfUnwantedGoods    extends WithName("08") with ClaimReasonType
  case object Value                    extends WithName("09") with ClaimReasonType
  case object Other                    extends WithName("10") with ClaimReasonType

  val values: Seq[ClaimReasonType] = Seq(
    CommodityCodeChange,
    CurrencyChanges,
    Cpuchange,
    CustomsSpecialProcedures,
    Preference,
    Retroactivequota,
    ReturnOfUnwantedGoods,
    ReturnedGoodsRelief,
    Value,
    Other
  )

  def abbreviation(reason: ClaimReasonType): String = reason match {
    case CommodityCodeChange      => "Comm Code"
    case CurrencyChanges          => "Value"
    case Cpuchange                => "CPC"
    case CustomsSpecialProcedures => "CPC"
    case Preference               => "Preference"
    case Retroactivequota         => "Quota"
    case ReturnOfUnwantedGoods    => "Returned Goods"
    case ReturnedGoodsRelief      => "RGR"
    case Value                    => "Value"
    case Other                    => "Other"
  }

  def options(form: Form[_], reasons: Set[ClaimReasonType])(implicit messages: Messages): Seq[RadioItem] =
    new RadioOptionHelper(values.filter(v => reasons.contains(v))).options(form)

  def options(form: Form[_])(implicit messages: Messages): Seq[CheckboxItem] = values.map {
    value =>
      CheckboxItem(
        name = Some("value[]"),
        value = value.toString,
        content = Text(messages(s"claimReasonType.${value.toString}")),
        checked = form.data.values.exists(_ == value.toString)
      )
  }

  implicit val enumerable: Enumerable[ClaimReasonType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
