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

sealed trait ClaimReason

object ClaimReason extends Enumerable.Implicits {
  case object RetroActiveQuota extends WithName("01") with ClaimReason
  case object CPUChange extends WithName("02") with ClaimReason
  case object CurrencyChanges extends WithName("03") with ClaimReason
  case object CommodityCodeChange extends WithName("04") with ClaimReason
  case object CustomsSpecialProcedures extends WithName("05") with ClaimReason
  case object Preference extends WithName("06") with ClaimReason
  case object ReturnedGoodsRelief extends WithName("07") with ClaimReason
  case object ReturnOfUnwantedGoods extends WithName("08") with ClaimReason
  case object Other extends WithName("09") with ClaimReason

  val values: Seq[ClaimReason] = Seq(
    RetroActiveQuota,
    CPUChange,
    CurrencyChanges,
    CommodityCodeChange,
    CustomsSpecialProcedures,
    Preference,
    ReturnedGoodsRelief,
    ReturnOfUnwantedGoods,
    Other
  )

  implicit val enumerable: Enumerable[ClaimReason] =
    Enumerable(values.map(v => v.toString -> v): _*)


}
