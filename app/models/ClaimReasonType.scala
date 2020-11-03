/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json._
import viewmodels.RadioOption

sealed trait ClaimReasonType

object ClaimReasonType extends Enumerable.Implicits {

  case object Retroactivequota extends WithName("01") with ClaimReasonType
  case object Cpuchange extends WithName("02") with ClaimReasonType
  case object CurrencyChanges extends WithName("03") with ClaimReasonType
  case object CommodityCodeChange extends WithName("04") with ClaimReasonType
  case object CustomsSpecialProcedures extends WithName("05") with ClaimReasonType
  case object Preference extends WithName("06") with ClaimReasonType
  case object ReturnedGoodsRelief extends WithName("07") with ClaimReasonType
  case object ReturnOfUnwantedGoods extends WithName("08") with ClaimReasonType
  case object Value extends WithName("09") with ClaimReasonType
  case object Other extends WithName("10") with ClaimReasonType

  val values: Seq[ClaimReasonType] = Seq(
    Retroactivequota, Cpuchange, CurrencyChanges, CommodityCodeChange, CustomsSpecialProcedures, Preference, ReturnedGoodsRelief,
    ReturnOfUnwantedGoods, Value, Other
  )

  val options: Seq[RadioOption] = values.map {
    value =>
      RadioOption("claimReasonType", value.toString)
  }

  implicit val enumerable: Enumerable[ClaimReasonType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
