/*
 * Copyright 2025 HM Revenue & Customs
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

sealed trait CustomsRegulationType

object CustomsRegulationType extends Enumerable.Implicits {

  case object UnionsCustomsCodeRegulation extends WithName("01") with CustomsRegulationType
  case object UKCustomsCodeRegulation     extends WithName("02") with CustomsRegulationType

  val values: Seq[CustomsRegulationType] = Seq(UKCustomsCodeRegulation, UnionsCustomsCodeRegulation)

  implicit val enumerable: Enumerable[CustomsRegulationType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
