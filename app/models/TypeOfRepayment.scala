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

import viewmodels.RadioOption

sealed trait TypeOfRepayment

object TypeOfRepayment extends Enumerable.Implicits {

  case object CustomsDuty extends WithName("01") with TypeOfRepayment
  case object Vat extends WithName("02") with TypeOfRepayment
  case object Other extends WithName("03") with TypeOfRepayment

  val values: Seq[TypeOfRepayment] = Seq(
    CustomsDuty,
    Vat,
    Other
  )

  val options: Seq[RadioOption] = values.map {
    value =>
      RadioOption("typeOfRepayment", value.toString)
  }

  implicit val enumerable: Enumerable[TypeOfRepayment] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
